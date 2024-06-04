package chat.twenty.dto;

import chat.twenty.domain.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MessageDtoMapper {
    MessageDtoMapper INSTANCE = Mappers.getMapper(MessageDtoMapper.class);

    ChatMessage toChatMessage(ChatMessageDto chatMessageDto);
    ChatMessage toChatMessageFromTwenty(TwentyMessageDto twentyMessageDto);


}
