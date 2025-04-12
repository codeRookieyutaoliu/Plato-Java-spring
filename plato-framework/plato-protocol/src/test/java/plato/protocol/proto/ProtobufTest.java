package plato.protocol.proto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Protobuf消息测试类
 * <p>
 * 用于测试Protobuf消息的序列化和反序列化
 * </p>
 */
public class ProtobufTest {
    
    /**
     * 检查Proto生成的类是否可用
     */
    private static boolean isProtobufClassesAvailable() {
        try {
            // 尝试加载一个protobuf生成的类
            Class.forName("plato.protocol.proto.CmdType");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * 测试消息命令序列化与反序列化
     */
    @Test
    @EnabledIf("isProtobufClassesAvailable")
    public void testMsgCmdSerialization() {
        // 创建消息内容
        byte[] payload = "Test Message".getBytes();
        
        // 创建MsgCmd
        MsgCmd msgCmd = MsgCmd.newBuilder()
                .setType(CmdType.LOGIN)
                .setPayload(com.google.protobuf.ByteString.copyFrom(payload))
                .build();
        
        // 序列化
        byte[] serialized = msgCmd.toByteArray();
        assertNotNull(serialized);
        
        try {
            // 反序列化
            MsgCmd deserialized = MsgCmd.parseFrom(serialized);
            
            // 验证反序列化结果
            assertEquals(CmdType.LOGIN, deserialized.getType());
            assertArrayEquals(payload, deserialized.getPayload().toByteArray());
        } catch (Exception e) {
            fail("反序列化失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试登录消息序列化与反序列化
     */
    @Test
    @EnabledIf("isProtobufClassesAvailable")
    public void testLoginMsgSerialization() {
        // 创建登录头
        LoginMsgHead head = LoginMsgHead.newBuilder()
                .setDeviceId(12345L)
                .build();
        
        // 创建登录消息
        LoginMsg loginMsg = LoginMsg.newBuilder()
                .setHead(head)
                .setBody(com.google.protobuf.ByteString.copyFrom("Login Body".getBytes()))
                .build();
        
        // 序列化
        byte[] serialized = loginMsg.toByteArray();
        assertNotNull(serialized);
        
        try {
            // 反序列化
            LoginMsg deserialized = LoginMsg.parseFrom(serialized);
            
            // 验证反序列化结果
            assertEquals(12345L, deserialized.getHead().getDeviceId());
            assertEquals("Login Body", new String(deserialized.getBody().toByteArray()));
        } catch (Exception e) {
            fail("反序列化失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试上行消息序列化与反序列化
     */
    @Test
    @EnabledIf("isProtobufClassesAvailable")
    public void testUpMsgSerialization() {
        // 创建上行消息头
        UpMsgHead head = UpMsgHead.newBuilder()
                .setClientId(123L)
                .setConnId(456L)
                .setSessionId("test-session")
                .build();
        
        // 创建上行消息
        UpMsg upMsg = UpMsg.newBuilder()
                .setHead(head)
                .setBody(com.google.protobuf.ByteString.copyFrom("Up Message Body".getBytes()))
                .build();
        
        // 序列化
        byte[] serialized = upMsg.toByteArray();
        assertNotNull(serialized);
        
        try {
            // 反序列化
            UpMsg deserialized = UpMsg.parseFrom(serialized);
            
            // 验证反序列化结果
            UpMsgHead deserializedHead = deserialized.getHead();
            assertEquals(123L, deserializedHead.getClientId());
            assertEquals(456L, deserializedHead.getConnId());
            assertEquals("test-session", deserializedHead.getSessionId());
            assertEquals("Up Message Body", new String(deserialized.getBody().toByteArray()));
        } catch (Exception e) {
            fail("反序列化失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试确认消息序列化与反序列化
     */
    @Test
    @EnabledIf("isProtobufClassesAvailable")
    public void testAckMsgSerialization() {
        // 创建确认消息
        AckMsg ackMsg = AckMsg.newBuilder()
                .setCode(200)
                .setMsg("OK")
                .setType(CmdType.LOGIN)
                .setConnId(12345L)
                .setClientId(67890L)
                .setSessionId(98765L)
                .setMsgId(54321L)
                .build();
        
        // 序列化
        byte[] serialized = ackMsg.toByteArray();
        assertNotNull(serialized);
        
        try {
            // 反序列化
            AckMsg deserialized = AckMsg.parseFrom(serialized);
            
            // 验证反序列化结果
            assertEquals(200, deserialized.getCode());
            assertEquals("OK", deserialized.getMsg());
            assertEquals(CmdType.LOGIN, deserialized.getType());
            assertEquals(12345L, deserialized.getConnId());
            assertEquals(67890L, deserialized.getClientId());
            assertEquals(98765L, deserialized.getSessionId());
            assertEquals(54321L, deserialized.getMsgId());
        } catch (Exception e) {
            fail("反序列化失败: " + e.getMessage());
        }
    }
} 