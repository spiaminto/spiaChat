package chat.twenty.service;

import chat.twenty.domain.ChatMessage;
import chat.twenty.dto.ChatMessageDto;
import chat.twenty.dto.MessageDtoMapper;
import chat.twenty.enums.ChatMessageType;
import chat.twenty.service.lower.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/*
 * 구조 : Controller - DtoProcessor - ChattingService - lowerServices
 * 처리:
 *  Controller
 *  -> DtoProcessor.processMessage(dto) - 하위 서비스 처리및 DB 저장후 Dto 반환
 *  -> Controller.convertAndSend(dto)
 *  -> DtoProcessor.processGpt(dto) - 하위 서비스 처리후 resultDto(gpt) 생성 및 DB 저장 후 반환
 *  -> Controller.convertAndSend(resultDto)
 */

/**
 * 일반 채팅방  메시지 타입별 처리를 ChattingService 에 위임
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ChatMessageDtoProcessor {

    private final ChattingService chattingService;
    private final ChatMessageService messageService; // DB 저장용

    public ChatMessageDto processMessage(ChatMessageDto chatMessageDto) {
        Long userId = chatMessageDto.getUserId();
        Long roomId = chatMessageDto.getRoomId();

        ChatMessageType messageType = chatMessageDto.getType();
        log.info("START processMessage() ChatMessageDto = {}", chatMessageDto);

        switch (messageType) {
            case ACTIVATE_GPT: // GPT 활성화
                String gptUuid = chattingService.activateGpt(roomId, userId);
                chatMessageDto.setGptUuid(gptUuid);
                break;
            case DEACTIVATE_GPT: // GPT 비활성화
                chatMessageDto = chattingService.deActivateGpt(chatMessageDto);
                break;
            default:
                log.info("processMessage, default case. case(message.type) = {}", chatMessageDto.getType());
                break;
        }

        // DB 저장 전 시간작성
        chatMessageDto.setCreatedAt(LocalDateTime.now().withNano(0));

        // DB 에 메시지 저장
        ChatMessage chatMessage = MessageDtoMapper.INSTANCE.toChatMessage(chatMessageDto);
        messageService.saveMessage(chatMessage);

        log.info("FINISH processMessage() ChatMessageDto = {}", chatMessageDto);

        return chatMessageDto; // default case 일경우 false, 그 외 true
    }

    public ChatMessageDto processGpt(ChatMessageDto chatMessageDto) {
        ChatMessageDto resultMessageDto = null;
        ChatMessageType messageType = chatMessageDto.getType();
        log.info("START processGPT() ChatMessageDto = {}", chatMessageDto);

        switch (messageType) {
            case ACTIVATE_GPT:
                resultMessageDto = chattingService.chatToGpt(chatMessageDto);
                resultMessageDto.setType(ChatMessageType.GPT_ENTER);
                break;
            case CHAT_TO_GPT:
                resultMessageDto = chattingService.chatToGpt(chatMessageDto);
                break;
            default:
                log.info("ChatMessage processGpt default case. case(message.type) = {}", messageType);
                break;
        }

        // DB 저장 전 시간작성
        resultMessageDto.setCreatedAt(LocalDateTime.now().withNano(0));

        // DB 에 메시지 저장
        ChatMessage chatMessage = MessageDtoMapper.INSTANCE.toChatMessage(resultMessageDto);
        messageService.saveMessage(chatMessage);

        log.info("FINISH processGpt() ChatMessageDto = {}", chatMessageDto);
        return resultMessageDto;
    }


}
