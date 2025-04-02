package plato.common.feign;

import com.google.protobuf.Message;
import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Protobuf编码器
 * 用于OpenFeign请求中的Protobuf消息序列化
 */
@Slf4j
public class ProtobufEncoder implements Encoder {

    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        if (!(object instanceof Message)) {
            log.warn("对象不是Protobuf消息类型，将使用默认编码器");
            return;
        }

        Message message = (Message) object;
        template.body(Arrays.toString(message.toByteArray()));
        template.header("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }
} 