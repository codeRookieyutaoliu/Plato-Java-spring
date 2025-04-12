package plato.protocol.serializer;

/**
 * 序列化器类型枚举
 * <p>
 * 定义支持的序列化类型，对应Go项目中的编码类型。
 */
public enum SerializerType {
    /**
     * Protocol Buffers序列化
     */
    PROTOBUF((byte) 1),
    
    /**
     * JSON序列化
     */
    JSON((byte) 2),
    
    /**
     * Java原生序列化
     */
    JAVA((byte) 3);
    
    private final byte code;
    
    SerializerType(byte code) {
        this.code = code;
    }
    
    /**
     * 获取序列化类型编码
     *
     * @return 类型编码
     */
    public byte getCode() {
        return code;
    }
    
    /**
     * 根据编码获取序列化类型
     *
     * @param code 类型编码
     * @return 对应的序列化类型，如果不存在则返回null
     */
    public static SerializerType valueOf(byte code) {
        for (SerializerType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
} 