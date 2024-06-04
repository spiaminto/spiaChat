package chat.twenty.repository;

import chat.twenty.domain.ChatRoom;
import chat.twenty.domain.QRoomMember;
import chat.twenty.domain.RoomMember;
import chat.twenty.dto.ChatRoomDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static chat.twenty.domain.QChatRoom.chatRoom;
import static chat.twenty.domain.QRoomMember.*;

@RequiredArgsConstructor
@Slf4j
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public ChatRoom findRoomWithMembers(Long Id) {
        return queryFactory.selectFrom(chatRoom)
                .leftJoin(chatRoom.members, roomMember).fetchJoin()
                .where(chatRoom.id.eq(Id))
                .fetchOne();
    }

    /**
     * index 페이지에서 방 목록 + 접송중 유저 count 한꺼번에 조회 후 Dto 반환 (뷰 종속)
     * @return List<ChatRoomDto>
     */
    public List<ChatRoomDto> findRoomWithConnectedCount() {
        List<Tuple> tuple = queryFactory.select(chatRoom, roomMember.count())
                .from(chatRoom)
                .leftJoin(chatRoom.members, roomMember)
                .on(roomMember.roomConnected.eq(true))
                .groupBy(chatRoom.id)
                .fetch();
        List<ChatRoomDto> chatRoomDtoList = new ArrayList<>();
        tuple.forEach(t -> {
            chatRoomDtoList.add(ChatRoomDto.from(t.get(chatRoom), t.get(roomMember.count())));
                }
        );
        return chatRoomDtoList;
    }

    @Override
    public String findTwentyAnswerById(Long id) {
        return queryFactory.select(chatRoom.twentyAnswer)
                .from(chatRoom)
                .where(chatRoom.id.eq(id))
                .fetchOne();
    }
}
