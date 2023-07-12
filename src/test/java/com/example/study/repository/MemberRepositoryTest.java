package com.example.study.repository;

import com.example.study.entity.Member;
import com.example.study.entity.QMember;
import com.example.study.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.example.study.entity.QMember.member;
import static com.example.study.entity.QTeam.team;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    EntityManager em;   //JPA 관리 핵심 객체

    // queryDsl 로 쿼리문을 작성하기 위한 핵심 객체
    JPAQueryFactory  factory;

    @BeforeEach
    void settingObject() {
        factory = new JPAQueryFactory(em);
    }


    void testInsertData() {

//        Team teamA = Team.builder().name("team1").build();
//        Team teamB = Team.builder().name("team2").build();
//
//        teamRepository.save(teamA);
//        teamRepository.save(teamB);

        Member member1 = Member.builder()
                .userName("member5").age(50).build();
        Member member2 = Member.builder()
                .userName("member6").age(60).build();
        Member member3 = Member.builder()
                .userName("member7").age(70).build();
        Member member4 = Member.builder()
                .userName("member8").age(80).build();

        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);
        memberRepository.save(member4);
    }

    @Test
    @DisplayName("testJPA")
    void testJPA() {
        List<Member> members = memberRepository.findAll();
        members.forEach(System.out::println);
    }
    
    @Test
    @DisplayName("JPQL")
    void testJPQL() {
        //given
        String jpqlQuery = "SELECT m FROM Member m WHERE m.userName = :userName";
        //when
        Member foundMember = em.createQuery(jpqlQuery, Member.class)
                .setParameter("userName", "member2")
                .getSingleResult();
        //then
        assertEquals("team1", foundMember.getTeam().getName());
        System.out.println("\n\n\n");
        System.out.println("\n\n\n");
        System.out.println("===========================================================");
        System.out.println("foundMember = " + foundMember);
        System.out.println("foundMember.getTeam() = " + foundMember.getTeam());
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("QueryDSL")
    void testQueryDSL() {
        //given
//        QMember m = new QMember("m1");
//        QMember m = QMember.member;

        //when
        Member foundMember = factory
//                .select(member)
//                .from(member)
                .selectFrom(member)     //한 번에
                .where(member.userName.eq("member3"))
                .fetchOne();

        //then
        assertNotNull(foundMember);
        assertEquals("team2", foundMember.getTeam().getName());

        System.out.println("\n\n\n");
        System.out.println("foundMember = " + foundMember);
        System.out.println("foundMember.getTeam() = " + foundMember.getTeam());
        System.out.println("\n\n\n");
    }

    @Test
    @DisplayName("search")
    void search() {
        //given
        String searchName = "member2";
        int searchAge = 20;
        //when
        Member foundMember = factory.selectFrom(member)
                .where(member.userName.eq(searchName)
//                        .and(member.age.eq(searchAge))
                        ,member.age.eq(searchAge)
                )
                .fetchOne();
        //then
        assertNotNull(foundMember);
        assertEquals("team1", foundMember.getTeam().getName());

        /*

        JPAQueryFactory 를 이용해서 쿼리문을 조립한 후 반환 인자를 결정한다.
        - fetchOne(): 단일 건 조회. 여러 건 조회시 예외 발생
        - fetchFirst(): 단일 건 조회. 여러 개가 조회돼도 첫 번째 값만 반환
        - fetch(): List 형태로 반환

        JPQL 이 제공하는 모든 검색 조건을 queryDsl 에서도 사용 가능
        member.userName.eq("member1")   // userName = 'member1'
        member.userName.nq("member1")   // userName != 'member1'
        member.userName.eq("member1").not()   // userName != 'member1'
        member.userName.isNotNull()   // 이름이 is not null
        member.age.in(10, 20)   // age in(10,20)
        member.age.notIn(10, 20)   // age not in(10,20)
        member.age.between(10, 30)   // age between 10 and 30
        member.age.goe(30)   // age >= 30
        member.age.gt(30)   // age > 30
        member.age.loe(30)   // age =< 30
        member.age.lt(30)   // age < 30
        member.userName.like("_김%")   // userName LIKE '_김%'
        member.userName.contains("김")   // userName LIKE '%김%'
        member.userName.startsWith("김")   // userName LIKE '김%'
        member.userName.endsWith("김")   // userName LIKE '김%'
         */
    }

    @Test
    @DisplayName("결과 반환하기")
    void testRetchResult() {
        //fetch
        List<Member> fetch1
                = factory.selectFrom(member).fetch();

        System.out.println("\n\n======== fetch ========");
        fetch1.forEach(System.out::println);

        //fetchOne
        Member fetch2 = factory.selectFrom(member)
                .where(member.id.eq(3L))
                .fetchOne();

        System.out.println("\n\n======== fetch2 ========");
        System.out.println("fetch2 = " + fetch2);
                //limit 1


        //fetchFirst
        Member fetch3
                = factory.selectFrom(member)
                .fetchFirst();

        System.out.println("\n\n======== fetchFirst ========");
        System.out.println("fetch3 = " + fetch3);
    }
    
    @Test
    @DisplayName("QueryDsl custom 설정 확인")
    void queryDslCustom() {
        //given
        String name = "member4";
        //when
        List<Member> result = memberRepository.findByName(name);
        //then
        assertEquals(1, result.size());
        assertEquals("team2", result.get(0).getTeam().getName());
    }
    
    @Test
    @DisplayName("회원 정렬 조회")
    void sort() {
        //given

        //when
        List<Member> result = factory.selectFrom(member)
                .orderBy(member.age.desc())
                .fetch();
        //then
        assertEquals(result.size(), 20);
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");

    }

    @Test
    @DisplayName("queryDsl paging")
    void paging() {
        //given

        //when
        List<Member> result = factory.selectFrom(member)
                .orderBy(member.userName.desc())
                .offset(0)
                .limit(3)
                .fetch();
        //then
        assertEquals(result.size(), 3);
        assertEquals(result.get(2).getUserName(), "member8");   //쌤은 6 나는 8
    }

    @Test
    @DisplayName("그룹 함수의 종류")
    void aggregation() {
        //given

        //when
        List<Tuple> result = factory.select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                ).from(member)
                .fetch();

        //then
        Tuple tuple = result.get(0);
        assertEquals(tuple.get(member.count()), 20);
        assertEquals(tuple.get(member.age.sum()), 360);
        assertEquals(tuple.get(member.age.avg()), 45);
        assertEquals(tuple.get(member.age.max()), 80);
        assertEquals(tuple.get(member.age.min()), 10);

        System.out.println("\n\n\n");
        System.out.println("tuple = " + tuple.toString());
        System.out.println("\n\n\n");
    }
    
    @Test
    @DisplayName("Group By, Having")
    void testGroupBy() {
        //given

        //when
        List<Long> result = factory.select(member.age.count())
                .from(member)
                .orderBy(member.age.asc())
                .groupBy(member.age)
                .having(member.age.count().goe(2))
                .fetch();

        //then
        assertEquals(result.size(), 2);

        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }
    
    @Test
    @DisplayName("join 해보기")
    void join() {
        //given

        //when
        List<Member> result = factory.selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("team1"))
                .fetch();

        //then
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }

    /**
     * ex) 회원과 팀을 조인하면서, 팀 이름이 team1인 팀만 조회, 회원은 모두 조회
     * SQL: SELECT m.*, t.* FROM tbl_member m LEFT JOIN tbl_team t ON m.team_id = t.id AND t.name = 'team1'
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t ON t.name = 'team1'
     */
    @Test
    @DisplayName("left outer join 테스트")
    void leftJoinTest() {
        //given

        //when
        List<Tuple> result = factory.select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("team1"))
                .fetch();
        //then
        result.forEach(tuple -> System.out.println("tuple = " + tuple));
    }
    
    @Test
    @DisplayName("sub query 사용하기(나이가 가장 많은 회원을 조회")
    void subQueryTest() {
        //given
        //같은 테이블에서 서브쿼리를 적용하려면 별도로 QClass 의 객체를 생성해야 한다.
        QMember memberSub = QMember.member;
        //when
        List<Member> result = factory.selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions  //서브쿼리를 사용할 수 있게 해주는 클래스
                                .select(memberSub.age.max())
                                .from(memberSub)
                )).fetch();
        //then
        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }
    
    @Test
    @DisplayName("나이가 평균 나이 이상인 회원을 조회")
    void subQueryGoe() {
        //given
        QMember m2 = new QMember("m2");
        //when
        List<Member> result = factory.selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions.select(m2.age.avg())
                                .from(m2)
                ))    //goe: 이상
                .fetch();
        //then
        System.out.println("\n\n\n");
        assertEquals(result.size(), 5);
        System.out.println("\n\n\n");
    }
    
    @Test
    @DisplayName("동적 sql 테스트")
    void dynamicQueryTest() {
        //given
        String name = null;
        int age = 80;
        //when
        List<Member> result = memberRepository.findUser(name, age);
        //then
        assertEquals(result.size(), 4);

        System.out.println("\n\n\n");
        result.forEach(System.out::println);
        System.out.println("\n\n\n");
    }
}