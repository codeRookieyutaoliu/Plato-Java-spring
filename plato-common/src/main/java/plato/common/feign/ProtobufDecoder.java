package plato.common.feign;

import com.google.protobuf.Message;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Protobuf解码器
 * 用于OpenFeign响应中的Protobuf消息反序列化
 */
@Slf4j
public class ProtobufDecoder implements Decoder {

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        if (response.body() == null) {
            return null;
        }

        // 检查是否为Protobuf消息类型
        if (!(type instanceof Class<?> messageClass) || !Message.class.isAssignableFrom((Class<?>) type)) {
            log.warn("类型不是Protobuf消息类型，将使用默认解码器");
            throw new DecodeException(response.status(), "类型不是Protobuf消息类型", response.request());
        }

        // 检查Content-Type
        String contentType = response.headers().getOrDefault("Content-Type", null)
                .stream()
                .findFirst()
                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        if (!contentType.contains(MediaType.APPLICATION_OCTET_STREAM_VALUE) && 
            !contentType.contains("application/x-protobuf")) {
            log.warn("Content-Type不是Protobuf类型，将使用默认解码器");
            throw new DecodeException(response.status(), "Content-Type不是Protobuf类型", response.request());
        }

        // 解析Protobuf消息
        try (InputStream inputStream = response.body().asInputStream()) {
            // 使用parseFrom方法解析Protobuf消息
            Method parseFrom = messageClass.getMethod("parseFrom", InputStream.class);
            return parseFrom.invoke(null, inputStream);
        } catch (Exception e) {
            log.error("解析Protobuf消息失败", e);
            throw new DecodeException(response.status(), "解析Protobuf消息失败", response.request(), e);
        }
    }
} 