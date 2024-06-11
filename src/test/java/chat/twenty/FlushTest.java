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
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.PersistenceUtil;
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
    private PersistenceUnitUtil persistenceUnitUtil;
    private ChatRoom testRoom;
    private RoomMember testMember;
    private RoomMember testMember2;

    @BeforeEach
    public void before() {
        persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
        testRoom = new ChatRoom("TestRoom");
        roomRepository.save(testRoom);
        testMember = new RoomMember(testRoom, 99999L, "test");
        testMember2 = new RoomMember(testRoom, 99998L, "test2");
        memberRepository.save(testMember);
        memberRepository.save(testMember2);
        em.flush(); // 테스트와 쿼리 분리를 위해 flush 후 재조회
        em.clear();
        testMember = memberRepository.findById(testMember.getId()).get();
        testMember2 = memberRepository.findById(testMember2.getId()).get();
        testRoom = roomRepository.findById(testRoom.getId()).get();
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
        memberRepository.findByRoomAndUserId(testRoom, testMember.getUserId());
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
        log.info("식별자로 연관관계 있는 엔티티 조회"); // select jpql 실행 x, update 실행 x
        testMember.setUsername("newTest");
        roomRepository.findById(testRoom.getId());

        log.info("jqpl 직접 조회 (room 테이블만)");
        roomRepository.findByName(testRoom.getName()); // select o update x

        log.info("jqpl 직접 조회 (room 테이블과 member 테이블 join)");
        roomRepository.findRoomWithMembers(testRoom.getId()); // select o update o
    }

    @Test
    public void relationNotOwnerIdSelect() {
        log.info("식별자로 연관관계 있는 엔티티 조회"); // select jpql 실행, update 실행
        testRoom.setName("newRoom");
        memberRepository.findById(testMember.getId());

        log.info("jqpl 직접 조회 (member 테이블만)");
        memberRepository.findByUsername(testMember.getUsername()); // select o update x

        log.info("jqpl 직접 조회 (member 테이블과 room 테이블 join)");
        memberRepository.findByRoomIdAndUserId(testRoom.getId(), testMember.getUserId()); // select o update o
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
        log.info("연관관계 있는 엔티티 조회 후 lazyLoading"); // lazyLoading select 실행, update 실행 x
        testMember.setUsername("newTest");
        log.info("members is loaded = {}", persistenceUnitUtil.isLoaded(testRoom.getMembers()));
        testRoom.getMembers().forEach(
                member -> log.info("member: {}", member.getUsername())); // username = newTest, test2
    }

    @Test
    public void relationLazyLoadingByOwner() {
        log.info("연관관계 있는 엔티티 조회 후 lazyLoading"); // lazyLoading select 실행 x, update 실행 x
        testRoom.setName("newRoom");
        log.info("room is loaded = {}", persistenceUnitUtil.isLoaded(testMember.getRoom())); // true
        log.info("room = {}", testMember.getRoom());
        // One 쪽인 member.room 은 @BeforeEach 에서 조회한 순간 이미 member에 추가되어 lazyLoading 자체가 발생하지 않는다.
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
//        memberRepository.countByUserId(testMember.getUserId() + 1);
        memberRepository.countByRoomId(testMember.getRoom().getId() + 1);
    }

    @Test
    public void otherCountQuery() {
        log.info("다른 테이블 count 쿼리"); // count jpql 실행, update 실행 x
        testMember.setUsername("newTest");
        roomRepository.count(); // update X
        memberRepository.count(); // 이거 실행 직전에 update 발생 (flush)
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

        log.info("memberAll.filter.getRoom === "); // sql X, update X / member.getRoom() 은 lazyLoading 발생X
        String roomName = findAll.stream()
                .filter(member -> member.getRoom().getId().equals(testRoom.getId()))
                .findFirst().get().getRoom().getName();
//            log.info("roomName = {}", roomName); // newRoom

        log.info("findMemberWithRoomId === "); // sql O, update O
        memberRepository.findByRoomIdAndUserId(testRoom.getId(), testMember.getUserId()); // update, select
        // 위는 member 테이블과 room 테이블을 조인해서 결과를 내기떄문에 update 발생

        // doc: prior to executing a JPQL/HQL query that overlaps with the queued entity actions
    }

    @Test
    public void modifyingQuery() {
        log.info("Modifying Query"); // update O

        // user 엔티티 저장 및 수정
        User user = new User("username");
        userRepository.save(user); // @Id @GeneratedValue(strategy = IDENTITY) 인경우 persist 시 insert 쿼리 실행
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
//        memberRepository.setTwentyReadyByRoomIdAndUserId(
//                testRoom.getId() + 1, testMember.getUserId() + 1, true); // 결과동일
        memberRepository.setTwentyReadyByRoomIdAndUserId(
                testRoom.getId(), testMember.getUserId(), true);
    }

    @Test
    public void manyVsOne() {
        log.info("One 변경 후 Many 조회"); // update X

        log.info("before set, testMember.getRoom().getName() = {}", testMember.getRoom().getName());

        testRoom.setName("newRoom"); // 변경 감지
        log.info("testMember.getRoom().getName() = {}", testMember.getRoom().getName());

        RoomMember findMember = memberRepository.findByUsername("test");
        log.info("findMember.getRoom().getName() = {}", findMember.getRoom().getName());
        em.flush(); // 아래의 roomRepository.findByName("newRoom") 에서 flush 방지를 위해 미리 flush

        log.info("=================================");

        log.info("Many 변경 후 One 조회"); // update X

        testRoom.getMembers().forEach(
                member -> log.info("before set testRoom.member: {}", member.getUsername()) // lazyLoading
        );

        testMember.setUsername("newTest"); // 변경 감지
        testRoom.getMembers().forEach(
                member -> log.info("testRoom.member: {}", member.getUsername())
        );

        ChatRoom findRoom = roomRepository.findByName("newRoom");
        findRoom.getMembers().forEach(
                member -> log.info("findRoom.member: {}", member.getUsername())
        );

        // 결론: Many 든 One 이든 어디가 변하던 간에 update 쿼리는 발생하지 않는다.
    }


    // lazyLoading 은 flush 를 발생시키지 않는다.

    // @Query 메서드는 영속성 컨텍스트를 통하지 않고 sql(jpql) 을 실행하기 때문에, flush 를 발생시키지 않는다.
    //  ㄴ 단, @Modifying 을 사용한 @Query 는 동종의 Entity 일 경우 flush 를 발생시킨다.
    // flush() 발생 시 쓰기 지연 저장소의 모든 쿼리가 한번에 실행된다.

        /*
         추가 1: 다 대 일 또는 일 대 다 관계의 lazyLoading 시

         연관관계의 주인인 '다' 는 '일' 쪽이 미리 로드되어 있으면, lazyLoading sql 이 발생하지 않는다.
          ㄴ member1 과 room1 이 다 대 일 관계고, 둘다 findById 로 로딩하면
              member1.getRoom() 시 lazyLoading 은 발생하지 않는다 (1차캐시에서 불러옴)

         연관관계의 주인이 아닌 '일' 은 '다' 쪽이 전부 로딩되어있어도, lazyLoading sql 이 발생한다.
          ㄴ member1, member2 과 room1 이 다 대 일 관계고, room1 은 member1, member2 만 가질때
              셋다 findById 로 로딩하면
              room1.getMembers() 시 lazyLoading 이 발생한다
              당연하겟지만, 영속성컨텍스트 입장에서 room1 이 Many 로 몇개나 가지고 있는지 모르니깐...
         */

        /*
        추가 2: save 시 insert 쿼리 발생 시점
        @Id @GeneratedValue(strategy = IDENTITY) 인경우 persist(save) 시 insert 쿼리 실행
        원래는 insert 문도 트잭 종료시에 쓰기지연에서 실행되어야 하나,
        위의 경우 식별자 생성을 DB 에 위임하기 떄문에 식별자를 반환하여 영속성 컨텍스트에서 관리해야하는
        입장상 어쩔수 없이 즉시 insert 쿼리 실행해서 id 를 db 로 부터 받아와야함.
         */

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
