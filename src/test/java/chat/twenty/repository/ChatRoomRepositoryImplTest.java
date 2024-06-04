package chat.twenty.repository;

import chat.twenty.dto.ChatRoomDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
@Slf4j
class ChatRoomRepositoryImplTest {

    @Autowired  ChatRoomRepositoryImpl chatRoomRepositoryImpl;

    @Test
    void findRoomWithMembers() {

        List<ChatRoomDto> chatRoomDtoList = chatRoomRepositoryImpl.findRoomWithConnectedCount();
        log.info("sql result out");

        chatRoomDtoList.forEach(chatRoomDto -> {
            log.info("chatRoomDto = " + chatRoomDto);
        });
    }

}