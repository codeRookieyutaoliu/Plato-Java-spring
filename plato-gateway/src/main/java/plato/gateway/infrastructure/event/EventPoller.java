package plato.gateway.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import plato.gateway.infrastructure.connection.NIOConnection;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 事件轮询器
 * 对应Go版本的epoller结构体
 */
@Slf4j
public class EventPoller {
    
    // 轮询器ID，对应Go版本的id
    private final int id;
    
    // 选择器，对应Go版本的selector
    private Selector selector;
    
    // 运行标志，对应Go版本的running
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // 待添加的连接队列，对应Go版本的addQueue
    private final ConcurrentLinkedQueue<NIOConnection> addQueue = new ConcurrentLinkedQueue<>();
    
    // 待删除的连接队列，对应Go版本的removeQueue
    private final ConcurrentLinkedQueue<NIOConnection> removeQueue = new ConcurrentLinkedQueue<>();
    
    // 轮询线程，对应Go版本的pollerThread
    private Thread pollerThread;
    
    // 缓冲区大小，对应Go版本的bufferSize
    private static final int BUFFER_SIZE = 4096;
    
    /**
     * 构造函数
     *
     * @param id 轮询器ID
     */
    public EventPoller(int id) {
        this.id = id;
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            log.error("创建选择器失败", e);
        }
    }
    
    /**
     * 启动轮询器
     * 对应Go版本的start方法
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            log.info("启动事件轮询器: id={}", id);
            
            pollerThread = new Thread(() -> {
                log.info("事件轮询线程启动: id={}", id);
                
                while (running.get()) {
                    try {
                        // 处理待添加的连接
                        processAddQueue();
                        
                        // 处理待删除的连接
                        processRemoveQueue();
                        
                        // 等待事件
                        if (selector.select(100) > 0) {
                            // 处理事件
                            processEvents();
                        }
                    } catch (IOException e) {
                        log.error("事件轮询失败", e);
                    }
                }
                
                log.info("事件轮询线程停止: id={}", id);
            });
            
            pollerThread.setName("plato-gateway-poller-" + id);
            pollerThread.start();
            
            log.info("事件轮询器启动完成: id={}", id);
        }
    }
    
    /**
     * 处理待添加的连接
     * 对应Go版本的processAddQueue方法
     *
     * @throws IOException 如果处理失败
     */
    private void processAddQueue() throws IOException {
        NIOConnection NIOConnection;
        while ((NIOConnection = addQueue.poll()) != null) {
            try {
                SocketChannel socketChannel = NIOConnection.getSocketChannel();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ, NIOConnection);
                log.debug("添加连接到事件轮询器: id={}, connection={}", id, NIOConnection.getId());
            } catch (ClosedChannelException e) {
                log.error("添加连接到事件轮询器失败: id={}, connection={}", id, NIOConnection.getId(), e);
            }
        }
    }
    
    /**
     * 处理待删除的连接
     * 对应Go版本的processRemoveQueue方法
     */
    private void processRemoveQueue() {
        NIOConnection NIOConnection;
        while ((NIOConnection = removeQueue.poll()) != null) {
            try {
                SocketChannel socketChannel = NIOConnection.getSocketChannel();
                SelectionKey key = socketChannel.keyFor(selector);
                if (key != null) {
                    key.cancel();
                }
                socketChannel.close();
                log.debug("从事件轮询器中删除连接: id={}, connection={}", id, NIOConnection.getId());
            } catch (IOException e) {
                log.error("从事件轮询器中删除连接失败: id={}, connection={}", id, NIOConnection.getId(), e);
            }
        }
    }
    
    /**
     * 处理事件
     * 对应Go版本的processEvents方法
     *
     * @throws IOException 如果处理失败
     */
    private void processEvents() throws IOException {
        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            
            if (!key.isValid()) {
                continue;
            }
            
            if (key.isReadable()) {
                NIOConnection NIOConnection = (NIOConnection) key.attachment();
                SocketChannel socketChannel = (SocketChannel) key.channel();
                
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                int readBytes = socketChannel.read(buffer);
                
                if (readBytes > 0) {
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);
                    
                    // 处理接收到的数据
                    NIOConnection.onReceive(data);
                } else if (readBytes < 0) {
                    // 连接已关闭
                    remove(NIOConnection);
                }
            }
        }
    }
    
    /**
     * 添加连接
     * 对应Go版本的add方法
     *
     * @param NIOConnection 连接对象
     */
    public void add(NIOConnection NIOConnection) {
        addQueue.offer(NIOConnection);
        selector.wakeup();
    }
    
    /**
     * 删除连接
     * 对应Go版本的remove方法
     *
     * @param NIOConnection 连接对象
     */
    public void remove(NIOConnection NIOConnection) {
        removeQueue.offer(NIOConnection);
        selector.wakeup();
    }
    
    /**
     * 等待事件
     * 对应Go版本的wait方法
     *
     * @param timeout 超时时间（毫秒）
     * @return 有事件的连接列表
     * @throws IOException 如果等待失败
     */
    public List<NIOConnection> waitEvents(int timeout) throws IOException {
        List<NIOConnection> NIOConnections = new ArrayList<>();
        
        if (selector.select(timeout) > 0) {
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                
                if (key.isValid() && key.isReadable()) {
                    NIOConnection NIOConnection = (NIOConnection) key.attachment();
                    NIOConnections.add(NIOConnection);
                }
            }
        }
        
        return NIOConnections;
    }
    
    /**
     * 停止轮询器
     * 对应Go版本的stop方法
     */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            log.info("停止事件轮询器: id={}", id);
            
            selector.wakeup();
            
            if (pollerThread != null) {
                try {
                    pollerThread.join(5000);
                } catch (InterruptedException e) {
                    log.error("等待事件轮询线程停止失败", e);
                    Thread.currentThread().interrupt();
                }
            }
            
            try {
                if (selector != null) {
                    selector.close();
                }
            } catch (IOException e) {
                log.error("关闭选择器失败", e);
            }
            
            log.info("事件轮询器已停止: id={}", id);
        }
    }
    
    /**
     * 获取轮询器ID
     * 对应Go版本的getId方法
     *
     * @return 轮询器ID
     */
    public int getId() {
        return id;
    }
} 