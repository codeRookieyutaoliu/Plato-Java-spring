package plato.protocol.codec.tcp;

import java.nio.ByteBuffer;

/**
 * 数据包结构体
 * <p>
 * 对应Go项目中的DataPgk结构体，用于封装长度和数据
 * 参考自: common/tcp/coder.go
 * </p>
 */
public class DataPacket {
    
    /**
     * 数据包长度
     */
    private final int length;
    
    /**
     * 数据包内容
     */
    private final byte[] data;
    
    /**
     * 构造函数
     *
     * @param data 数据包内容
     */
    public DataPacket(byte[] data) {
        this.data = data;
        this.length = data.length;
    }
    
    /**
     * 构造函数
     *
     * @param length 数据包长度
     * @param data 数据包内容
     */
    public DataPacket(int length, byte[] data) {
        this.length = length;
        this.data = data;
    }
    
    /**
     * 获取数据包长度
     *
     * @return 数据包长度
     */
    public int getLength() {
        return length;
    }
    
    /**
     * 获取数据包内容
     *
     * @return 数据包内容
     */
    public byte[] getData() {
        return data;
    }
    
    /**
     * 将数据包序列化为字节数组
     * <p>
     * 对应Go项目中的Marshal方法
     * 参考自: common/tcp/coder.go
     * </p>
     *
     * @return 序列化后的字节数组
     */
    public byte[] marshal() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
        buffer.putInt(length);  // 使用大端序写入长度
        buffer.put(data);       // 写入数据
        return buffer.array();
    }
    
    /**
     * 从字节数组反序列化数据包
     * <p>
     * 在Go项目中没有对应的方法，这是Java版本的补充
     * </p>
     *
     * @param bytes 序列化的字节数组
     * @return 数据包对象
     */
    public static DataPacket unmarshal(byte[] bytes) {
        if (bytes.length < 4) {
            throw new IllegalArgumentException("数据包字节数组长度不能小于4");
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int length = buffer.getInt();  // 读取长度
        
        byte[] data = new byte[bytes.length - 4];
        buffer.get(data);  // 读取数据
        
        return new DataPacket(length, data);
    }
} 