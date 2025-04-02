package plato.state.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import plato.state.config.MessageConfig;
import plato.state.infrastructure.timer.TimerManager;
import plato.state.infrastructure.timer.TimingWheel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 消息状态机测试
 * 测试消息状态机的各种功能
 */
public class MessageStateMachineTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private TimerManager timerManager;

    @Mock
    private CacheStateManager cacheStateManager;

    private MessageConfig messageConfig;

    private MessageStateMachine messageStateMachine;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // 配置RedisTemplate模拟
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // 创建消息配置
        messageConfig = new MessageConfig();
        messageConfig.setRetryInterval(1000);
        messageConfig.setMaxRetryTimes(3);
        messageConfig.setExpireTime(86400);
        
        // 创建消息状态机
        messageStateMachine = new MessageStateMachine(
                redisTemplate, timerManager, cacheStateManager, messageConfig);
    }

    /**
     * 测试生成消息ID
     */
    @Test
    public void testGenerateMsgId() {
        // 生成两个消息ID
        long msgId1 = messageStateMachine.generateMsgId();
        long msgId2 = messageStateMachine.generateMsgId();
        
        // 验证第二个消息ID比第一个大1
        assertEquals(msgId1 + 1, msgId2);
    }

    /**
     * 测试记录和获取客户端ID
     */
    @Test
    public void testRecordAndGetClientId() {
        // 记录客户端ID
        messageStateMachine.recordClientId(1001, 2001, "session1");
        
        // 验证客户端ID被正确记录到Redis
        verify(valueOperations).set(eq("client_id:1001:session1"), eq(2001L), 
                eq(messageConfig.getExpireTime()), any());
        
        // 获取客户端ID
        long clientId = messageStateMachine.getClientId(1001);
        
        // 验证获取的客户端ID正确
        assertEquals(2001, clientId);
    }

    /**
     * 测试比较并递增客户端ID
     */
    @Test
    public void testCompareAndIncrClientId() {
        // 记录客户端ID
        messageStateMachine.recordClientId(1001, 2001, "session1");
        
        // 测试客户端ID小于当前值的情况
        boolean result1 = messageStateMachine.compareAndIncrClientId(1001, 2000, "session1");
        assertFalse(result1);
        
        // 测试客户端ID等于当前值的情况
        boolean result2 = messageStateMachine.compareAndIncrClientId(1001, 2001, "session1");
        assertFalse(result2);
        
        // 测试客户端ID大于当前值的情况
        boolean result3 = messageStateMachine.compareAndIncrClientId(1001, 2002, "session1");
        assertTrue(result3);
        
        // 验证客户端ID被更新
        assertEquals(2002, messageStateMachine.getClientId(1001));
    }

    /**
     * 测试删除客户端ID
     */
    @Test
    public void testDeleteConnClientId() {
        // 记录客户端ID
        messageStateMachine.recordClientId(1001, 2001, "session1");
        
        // 删除客户端ID
        messageStateMachine.deleteConnClientId(1001);
        
        // 验证Redis中的记录被删除
        verify(redisTemplate).delete("client_id:1001:session1");
        
        // 验证内存中的记录被删除
        assertEquals(0, messageStateMachine.getClientId(1001));
        assertEquals("", messageStateMachine.getSessionId(1001));
    }

    /**
     * 测试处理上行消息
     */
    @Test
    public void testHandleUpMsg() {
        // 模拟消息负载
        byte[] payload = "test message".getBytes();
        
        // 模拟缓存状态管理器的appendLastMsg方法
        when(cacheStateManager.appendLastMsg(anyLong(), any())).thenReturn(true);
        
        // 处理上行消息
        boolean result = messageStateMachine.handleUpMsg(1001, 2001, "session1", payload);
        
        // 验证处理成功
        assertTrue(result);
        
        // 验证客户端ID被记录
        assertEquals(2001, messageStateMachine.getClientId(1001));
        
        // 验证下行消息被处理
        verify(cacheStateManager).appendLastMsg(eq(1001), any());
        
        // 验证定时器被设置
        verify(timerManager).afterFunc(eq(messageConfig.getRetryInterval()), any());
    }

    /**
     * 测试处理下行消息
     */
    @Test
    public void testHandleDownMsg() {
        // 模拟消息负载
        byte[] payload = "test message".getBytes();
        
        // 模拟缓存状态管理器的appendLastMsg方法
        when(cacheStateManager.appendLastMsg(anyLong(), any())).thenReturn(true);
        
        // 处理下行消息
        boolean result = messageStateMachine.handleDownMsg(1001, 3001, "0", payload);
        
        // 验证处理成功
        assertTrue(result);
        
        // 验证消息被添加到缓存
        verify(cacheStateManager).appendLastMsg(eq(1001), any());
        
        // 验证定时器被设置
        verify(timerManager).afterFunc(eq(messageConfig.getRetryInterval()), any());
    }

    /**
     * 测试确认消息
     */
    @Test
    public void testAckMessage() {
        // 确认消息
        boolean result = messageStateMachine.ackMessage(1001, "0", 3001);
        
        // 验证确认成功
        assertTrue(result);
        
        // 验证缓存状态管理器的ackLastMsg方法被调用
        verify(cacheStateManager).ackLastMsg(eq(1001), eq(0L), eq(3001L));
    }

    /**
     * 测试消息重传
     */
    @Test
    public void testMessageRetry() {
        // 模拟消息负载
        byte[] payload = "test message".getBytes();
        
        // 模拟缓存状态管理器的appendLastMsg方法
        when(cacheStateManager.appendLastMsg(anyLong(), any())).thenReturn(true);
        
        // 模拟定时器管理器的afterFunc方法，立即执行任务
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(1);
            task.run();
            return mock(TimingWheel.Timer.class);
        }).when(timerManager).afterFunc(anyLong(), any());
        
        // 模拟消息未被确认
        when(cacheStateManager.getLastMsg(anyLong())).thenReturn(null);
        
        // 处理下行消息
        messageStateMachine.handleDownMsg(1001, 3001, "0", payload);
        
        // 验证重传被触发了3次（初始发送 + 3次重传）
        verify(cacheStateManager, times(4)).rePush(eq(1001));
    }
} 