package plato.protocol.codec;

import plato.protocol.ProtocolType;

/**
 * 编解码处理器接口
 * <p>
 * 定义通用的编解码行为，统一TCP和UDP实现
 * </p>
 */
public interface CodecHandler {
    
    /**
     * 编码消息
     *
     * @param data 原始消息数据
     * @return 编码后的数据
     */
    byte[] encode(byte[] data);
    
    /**
     * 解码消息
     *
     * @param data 收到的数据
     * @return 解码后的消息数据
     */
    byte[] decode(byte[] data);
    
    /**
     * 获取协议类型
     *
     * @return 协议类型
     */
    ProtocolType getProtocolType();
    
    /**
     * 编解码监听器接口
     * <p>
     * 用于监听编解码过程，可以收集统计信息或者进行日志记录
     * </p>
     */
    interface CodecListener {
        /**
         * 编码完成回调
         *
         * @param data 原始数据
         * @param encodedLength 编码后长度
         */
        void onEncode(byte[] data, int encodedLength);
        
        /**
         * 解码完成回调
         *
         * @param data 解码后数据
         * @param messageLength 消息长度
         */
        void onDecode(byte[] data, int messageLength);
    }
    
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
} 