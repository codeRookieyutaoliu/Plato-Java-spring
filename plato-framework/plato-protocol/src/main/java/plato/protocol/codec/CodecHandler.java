package plato.protocol.codec;

import plato.protocol.ProtocolType;

/**
 * 编解码处理器接口
 * <p>
 * 定义编解码器的基本行为，实现类负责特定协议的编解码功能
 * </p>
 */
public interface CodecHandler {
    
    /**
     * 编码消息
     *
     * @param data 原始消息数据
     * @return 编码后的字节数组
     */
    byte[] encode(byte[] data);
    
    /**
     * 解码消息
     *
     * @param data 接收到的字节数组
     * @return 解码后的消息数据，如果数据不完整则返回null
     */
    byte[] decode(byte[] data);
    
    /**
     * 获取协议类型
     *
     * @return 协议类型
     */
    ProtocolType getProtocolType();
    
    /**
     * 设置编解码监听器
     *
     * @param listener 监听器
     */
    void setCodecListener(CodecListener listener);
    
    /**
     * 获取编解码监听器
     *
     * @return 监听器
     */
    CodecListener getCodecListener();
    
    /**
     * 编解码监听器接口
     * <p>
     * 用于监听编解码过程中的事件
     * </p>
     */
    interface CodecListener {
        /**
         * 当消息被编码时调用
         *
         * @param originalData 原始数据
         * @param encodedLength 编码后的长度
         */
        default void onEncode(byte[] originalData, int encodedLength) {
        }
        
        /**
         * 当消息被解码时调用
         *
         * @param decodedData 解码后的数据
         * @param originalLength 原始数据长度
         */
        default void onDecode(byte[] decodedData, int originalLength) {
        }
        
        /**
         * 当发生编解码错误时调用
         *
         * @param e 异常
         */
        default void onError(Exception e) {
        }
    }
} 