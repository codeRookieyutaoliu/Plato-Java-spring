package plato.message.infrastructure.converter;

import plato.message.application.dto.AckMessageDTO;
import plato.message.application.dto.HeartbeatMessageDTO;
import plato.message.application.dto.LoginMessageDTO;
import plato.message.application.dto.MessageDTO;
import plato.message.application.dto.PushMessageDTO;
import plato.message.application.dto.ReconnectMessageDTO;
import plato.message.application.dto.UpMessageDTO;
import plato.message.domain.entity.AckMessage;
import plato.message.domain.entity.HeartbeatMessage;
import plato.message.domain.entity.LoginMessage;
import plato.message.domain.entity.Message;
import plato.message.domain.entity.PushMessage;
import plato.message.domain.entity.ReconnectMessage;
import plato.message.domain.entity.UpMessage;
import plato.message.domain.valueobject.MessageType;

/**
 * 消息转换器
 * <p>
 * 用于领域模型消息与数据传输对象(DTO)之间的转换
 * </p>
 */
public class MessageConverter {
    
    /**
     * 私有构造函数，防止实例化
     */
    private MessageConverter() {
    }
    
    /**
     * 将领域模型消息转换为DTO
     *
     * @param message 领域模型消息
     * @return 消息DTO
     */
    public static MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }
        
        return switch (message.getType()) {
            case LOGIN -> toLoginMessageDTO((LoginMessage) message);
            case HEARTBEAT -> toHeartbeatMessageDTO((HeartbeatMessage) message);
            case RECONNECT -> toReconnectMessageDTO((ReconnectMessage) message);
            case ACK -> toAckMessageDTO((AckMessage) message);
            case UP -> toUpMessageDTO((UpMessage) message);
            case PUSH -> toPushMessageDTO((PushMessage) message);
        };
    }
    
    /**
     * 将DTO转换为领域模型消息
     *
     * @param dto 消息DTO
     * @return 领域模型消息
     */
    public static Message toDomain(MessageDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return switch (dto.getType()) {
            case LOGIN -> toLoginMessage((LoginMessageDTO) dto);
            case HEARTBEAT -> toHeartbeatMessage((HeartbeatMessageDTO) dto);
            case RECONNECT -> toReconnectMessage((ReconnectMessageDTO) dto);
            case ACK -> toAckMessage((AckMessageDTO) dto);
            case UP -> toUpMessage((UpMessageDTO) dto);
            case PUSH -> toPushMessage((PushMessageDTO) dto);
        };
    }
    
    // 以下是各种具体消息类型的转换方法
    
    private static LoginMessageDTO toLoginMessageDTO(LoginMessage message) {
        return new LoginMessageDTO(
                message.getId(),
                message.getTimestamp(),
                MessageType.LOGIN,
                message.getUid(),
                message.getDeviceId(),
                message.getToken()
        );
    }
    
    private static LoginMessage toLoginMessage(LoginMessageDTO dto) {
        return new LoginMessage(
                dto.getId(),
                dto.getTimestamp(),
                dto.getUid(),
                dto.getDeviceId(),
                dto.getToken()
        );
    }
    
    private static HeartbeatMessageDTO toHeartbeatMessageDTO(HeartbeatMessage message) {
        return new HeartbeatMessageDTO(
                message.getId(),
                message.getTimestamp(),
                MessageType.HEARTBEAT,
                message.getUid(),
                message.getDeviceId()
        );
    }
    
    private static HeartbeatMessage toHeartbeatMessage(HeartbeatMessageDTO dto) {
        return new HeartbeatMessage(
                dto.getId(),
                dto.getTimestamp(),
                dto.getUid(),
                dto.getDeviceId()
        );
    }
    
    private static ReconnectMessageDTO toReconnectMessageDTO(ReconnectMessage message) {
        return new ReconnectMessageDTO(
                message.getId(),
                message.getTimestamp(),
                MessageType.RECONNECT,
                message.getUid(),
                message.getDeviceId(),
                message.getConnId()
        );
    }
    
    private static ReconnectMessage toReconnectMessage(ReconnectMessageDTO dto) {
        return new ReconnectMessage(
                dto.getId(),
                dto.getTimestamp(),
                dto.getUid(),
                dto.getDeviceId(),
                dto.getConnId()
        );
    }
    
    private static AckMessageDTO toAckMessageDTO(AckMessage message) {
        return new AckMessageDTO(
                message.getId(),
                message.getTimestamp(),
                MessageType.ACK,
                message.getCode(),
                message.getMsg(),
                message.getAckType(),
                message.getConnId(),
                message.getClientId(),
                message.getSessionId(),
                message.getMsgId()
        );
    }
    
    private static AckMessage toAckMessage(AckMessageDTO dto) {
        return new AckMessage(
                dto.getId(),
                dto.getTimestamp(),
                dto.getCode(),
                dto.getMsg(),
                dto.getAckType(),
                dto.getConnId(),
                dto.getClientId(),
                dto.getSessionId(),
                dto.getMsgId()
        );
    }
    
    private static UpMessageDTO toUpMessageDTO(UpMessage message) {
        return new UpMessageDTO(
                message.getId(),
                message.getTimestamp(),
                MessageType.UP,
                message.getClientId(),
                message.getConnId(),
                message.getSessionId(),
                message.getContent()
        );
    }
    
    private static UpMessage toUpMessage(UpMessageDTO dto) {
        return new UpMessage(
                dto.getId(),
                dto.getTimestamp(),
                dto.getClientId(),
                dto.getConnId(),
                dto.getSessionId(),
                dto.getContent()
        );
    }
    
    private static PushMessageDTO toPushMessageDTO(PushMessage message) {
        return new PushMessageDTO(
                message.getId(),
                message.getTimestamp(),
                MessageType.PUSH,
                message.getMsgId(),
                message.getSessionId(),
                message.getContent()
        );
    }
    
    private static PushMessage toPushMessage(PushMessageDTO dto) {
        return new PushMessage(
                dto.getId(),
                dto.getTimestamp(),
                dto.getMsgId(),
                dto.getSessionId(),
                dto.getContent()
        );
    }
}