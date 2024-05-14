package chat.twenty.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static chat.twenty.domain.QChatRoom.chatRoom;

@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    @Override
    public String findTwentyAnswerById(Long id) {
        return queryFactory.select(chatRoom.twentyAnswer)
                .from(chatRoom)
                .where(chatRoom.id.eq(id))
                .fetchOne();
    }
}
