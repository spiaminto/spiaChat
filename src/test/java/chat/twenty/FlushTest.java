package chat.twenty;

import chat.twenty.domain.ChatRoom;
import chat.twenty.domain.RoomMember;
import chat.twenty.domain.User;
import chat.twenty.repository.ChatRoomRepository;
import chat.twenty.repository.RoomMemberRepository;
import chat.twenty.repository.TwentyMemberInfoRepository;
import chat.twenty.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
@Slf4j
public class FlushTest {

    @Autowired
    private TwentyMemberInfoRepository infoRepository;
    @Autowired
    private ChatRoomRepository roomRepository;
    @Autowired
    private RoomMemberRepository memberRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager em;
    private ChatRoom testRoom;
    private RoomMember testMember;
    private RoomMember testMember2;

    @BeforeEach
    public void before() {
        testRoom = new ChatRoom("TestRoom");
        roomRepository.save(testRoom);
        testMember = new RoomMember(testRoom, 99999L, "test");
        testMember2 = new RoomMember(testRoom, 99998L, "test2");
        memberRepository.save(testMember);
        memberRepository.save(testMember2);
        em.flush();
        em.clear();
        testRoom = roomRepository.findById(testRoom.getId()).get();
        testMember = memberRepository.findById(testMember.getId()).get();
        testMember2 = memberRepository.findById(testMember2.getId()).get();
        log.info("===before with cleared ===");
    }

    @Test
    public void idSelect() {
        log.info("식별자로 자신 조회");
        testMember.setUsername("newTest");
        memberRepository.findById(testMember.getId());
    }

    @Test
    public void idSelect2() {
        log.info("식별자로 동류 엔티티 조회");
        testMember.setUsername("newTest");
        memberRepository.findById(testMember2.getId());
    }

    @Test
    public void noneIdSelectByFk() {
        log.info("식별자가 아닌 자신조회"); // select jpql 실행, update 실행
        testMember.setUsername("newTest");
        memberRepository.findByRoomIdAndUserId(testRoom.getId(), testMember.getUserId());
    }

    @Test
    public void noneIdSelectByUsername() {
        log.info("식별자가 아닌 자신조회"); // select jpql 실행, update 실행
        testMember.setUsername("newTest");
        memberRepository.findByUsername("newTest");
    }

    @Test
    public void noneIdSelectOtherByFk() {
        log.info("식별자가 아닌 다른 엔티티 조회"); // select jpql 실행, update 실행
        testMember.setUsername("newTest");
        memberRepository.findByRoomIdAndUserId(testRoom.getId() + 1, testMember2.getUserId() + 1);
    }

    @Test
    public void noneIdSelectOtherByUsername() {
        log.info("식별자가 아닌 다른 엔티티 조회"); // select jpql 실행, update 실행
        testMember.setUsername("newTest");
        memberRepository.findByUsername("newTest2");
    }

    @Test
    public void selectAll() {
        log.info("동류 엔티티 전체조회"); // select jpql 실행, update 실행
        testMember.setUsername("newTest");
        memberRepository.findAll();
    }

    @Test
    public void noneRelationSelect() {
        log.info("연관관계 없는 엔티티 조회"); // select jpql 실행, update 실행 X
        testMember.setUsername("newTest");
        infoRepository.findByUserId(testMember.getUserId());
    }

    @Test
    public void relationIdSelect() {
        log.info("식별자로 연관관계 있는 엔티티 조회"); // select jpql 실행, update 실행
        testMember.setUsername("newTest");
        roomRepository.findById(testRoom.getId());
    }

    @Test
    public void relationAllSelect() {
        log.info("연관관계 있는 엔티티 전체 조회"); // select jpql 실행, update 실행 X
        testMember.setUsername("newTest");
        roomRepository.findAll();
    }

    @Test
    public void relationPartialSelect() {
        log.info("연관관계 있는 엔티티 일부 조회"); // select jpql 실행, update 실행 X
        testMember.setUsername("newTest");
        roomRepository.findTwentyAnswerById(testRoom.getId());
    }

    @Test
    public void relationFetchJoinSelect() {
        log.info("연관관계 있는 엔티티 자신포함 queryDsl(fetchJoin) 조회"); // select jpql 실행, update 실행
        testMember.setUsername("newTest");
        roomRepository.findRoomWithMembers(testRoom.getId());
    }

    @Test
    public void relationLazyLoading() {
        log.info("연관관계 있는 엔티티 조회 후 lazyLoading"); // select jpql 2회 실행, update 실행 X
        testMember.setUsername("newTest");
        roomRepository.findByName(testRoom.getName()).getMembers().forEach(
                member -> log.info("member: {}", member.getUsername()) // username = newTest
        );
    }

    @Test
    public void relationLazyLoading2() {
        log.info("연관관계 있는 엔티티 조회 후 lazyLoading");
        testMember.setUsername("newTest");
        testRoom.getMembers().forEach(
                member -> log.info("member: {}", member.getUsername())
        );
    }

    @Test
    public void selfIncludedCountQuery() {
        log.info("자신을 포함하는 count 쿼리"); // count jpql 실행, update 실행
        testMember.setUsername("newTest");
//            memberRepository.countByUserId(testMember.getUserId());
        memberRepository.countByRoomId(testRoom.getId());
    }

    @Test
    public void selfIncludedCountQueryNormal() {
        log.info("자신을 포함하는 일반 count 쿼리"); // count jpql 실행, update 실행
        testMember.setUsername("newTest");
        memberRepository.count();
    }

    @Test
    public void selfNotIncludedCountQuery() {
        log.info("자신을 포함하지 않는 count 쿼리"); // count jpql 실행, update 실행
        testMember.setUsername("newTest");
        memberRepository.countByUserId(testMember.getUserId() + 1);
        memberRepository.countByRoomId(testRoom.getId() + 1);
    }

    @Test
    public void specificRelationSelect() {
        log.info("특정 연관관계 조회 - 프로젝트 사용"); // select jpql 실행, update 실행
        ChatRoom findRoom = roomRepository.findById(testRoom.getId()).get();
        findRoom.setName("newRoom");

        log.info("member.findById==="); // sql X
        memberRepository.findById(testMember.getId());

        log.info("member.findAll==="); // sql O, update X
        List<RoomMember> findAll = memberRepository.findAll();

//            log.info("room.findAll === "); // sql O, update O
//            List<ChatRoom> findAllRoom = roomRepository.findAll();

        log.info("memberAll.filter.getRoom === "); // sql X, update X
        String roomName = findAll.stream()
                .filter(member -> member.getRoom().getId().equals(testRoom.getId()))
                .findFirst().get().getRoom().getName();
//            log.info("roomName = {}", roomName); // newRoom

        log.info("findMemberWithRoomId === "); // sql O, update O
        memberRepository.findByRoomIdAndUserId(testRoom.getId(), testMember.getUserId()); // update, select
        // doc: prior to executing a JPQL/HQL query that overlaps with the queued entity actions
    }

    @Test
    public void modifyingQuery() {
        log.info("Modifying Query"); // update O

        // user 엔티티 저장 및 수정
        User user = new User("username");
        userRepository.save(user);
        User findUser = userRepository.findById(user.getId()).get();
        findUser.setUsername("newUsername");

        // member 엔티티 수정
        testMember.setUsername("newTest");

        // infoRepository @Modifying 실행 -> flush X
        infoRepository.updateAliveByUserId(testMember.getUserId(), false);
        log.info("===");

        // roomRepository @Modifying 실행 -> flush X
        roomRepository.setGptUuidById(testRoom.getId(), "gptUuid");
        log.info("===");

        // memberRepository @Modifying 실행 -> flush O, 이때 user 와 member 모두 update 쿼리 실행
        memberRepository.setTwentyReadyByRoomIdAndUserId(
                testRoom.getId() + 1, testMember.getUserId() + 1, true);
    }

    @Test
    public void manyVsOne() {
        log.info("One 변경 후 Many 조회"); // update X

        log.info("before set, testMember.getRoom().getName() = {}", testMember.getRoom().getName());

        testRoom.setName("newRoom"); // 변경 감지
        log.info("testMember.getRoom().getName() = {}", testMember.getRoom().getName());

        RoomMember findMember = memberRepository.findByUsername("test");
        log.info("findMember.getRoom().getName() = {}", findMember.getRoom().getName());


        log.info("=================================");

        log.info("Many 변경 후 One 조회"); // update O

        testRoom.getMembers().forEach(
                member -> log.info("before set testRoom.member: {}", member.getUsername()) // lazyLoading
        );

        testMember.setUsername("newTest"); // 변경 감지
        testRoom.getMembers().forEach(
                member -> log.info("testRoom.member: {}", member.getUsername())
        );

        ChatRoom findRoom = roomRepository.findByName("newRoom");// 여기서 update room, member 모두 발생 후 select room
        findRoom.getMembers().forEach(
                member -> log.info("findRoom.member: {}", member.getUsername())
        );


        // 다 대 일 또는 일 대 다 의 관계에서,
        // 다 쪽의 변경은 일 쪽의 조회시 update(flush) 를 발생시킨다.
        // 일 쪽의 변경은 다 쪽의 조회시 update(flush) 를 발생시키지 않는다.
        // lazyLoading 은 flush 를 발생시키지 않는다.

        // @Query 메서드는 영속성 컨텍스트를 통하지 않고 sql(jpql) 을 실행하기 때문에, flush 를 발생시키지 않는다.
        //  ㄴ 단, @Modifying 을 사용한 @Query 는 동종의 Entity 일 경우 flush 를 발생시킨다.
        // flush() 발생 시 쓰기 지연 저장소의 모든 쿼리가 한번에 실행된다.

        /**
         * Auto Flush from hibernate doc
         *
         * prior to committing a Transaction
         *
         * prior to executing a JPQL/HQL query that overlaps with the queued entity actions
         *
         * before executing any native SQL query that has no registered synchronization
         */
    }

}
