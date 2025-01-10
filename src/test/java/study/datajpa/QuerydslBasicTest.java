package study.datajpa;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Member;
import study.datajpa.entity.QTeam;
import study.datajpa.entity.Team;

import java.util.List;

import static study.datajpa.entity.QMember.*;
import static study.datajpa.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        Member findByJPQL = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        Assertions.assertEquals(findByJPQL.getUsername(), "member1");
    }

    @Test
    public void startQuerydsl() {
        Member findMember = queryFactory
                        .select(member)
                        .from(member)
                        .where(member.username.eq("member1"))
                        .fetchFirst();
        Assertions.assertEquals(findMember.getUsername(), "member1");
    }

    @Test
    public void search() {
        Member findMember =
                queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.between(10, 30))
                ).fetchOne();
        Assertions.assertEquals(findMember.getUsername(), "member1");
        Assertions.assertEquals(findMember.getAge(), 10);
    }

    @Test
    public void searchAndParam() {
        Member findMember =
                queryFactory
                        .selectFrom(member)
                        .where(
                                member.username.eq("member1")
                                ,member.age.eq(10)
                        ).fetchOne();
        Assertions.assertEquals(findMember.getUsername(), "member1");
        Assertions.assertEquals(findMember.getAge(), 10);
    }

    @Test
    public void resultFetch() {
//        List<Member> fetch = queryFactory
//                    .selectFrom(member)
//                    .fetch();
//        Member fetchOne = queryFactory
//                .selectFrom(member)
//                .fetchOne();
//        Member fetchFirst = queryFactory
//                .selectFrom(member)
//                .fetchFirst();
        QueryResults<Member> memberQueryResults =
                queryFactory
                .selectFrom(member).fetchResults();
        memberQueryResults.getTotal();
        List<Member> content = memberQueryResults.getResults();

        long total = queryFactory
                .selectFrom(member)
                .stream().count();
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순
     * 2. 회원 이름 올림차순
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        Assertions.assertEquals(member5.getUsername(), "member5");
        Assertions.assertEquals(member6.getUsername(), "member6");
        Assertions.assertNull(memberNull.getUsername());
    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
        Assertions.assertEquals(result.size(), 2);
    }

    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();
        Tuple tuple = result.get(0);
        Assertions.assertEquals(tuple.get(member.count()), 4);
        Assertions.assertEquals(tuple.get(member.age.sum()), 100);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령
     * @throws Exception
     */
    @Test
    public void group() throws Exception {
        // given
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        // then
        Assertions.assertEquals(teamA.get(team.name), "teamA");
        Assertions.assertEquals(teamA.get(member.age.avg()), 15);

        Assertions.assertEquals(teamB.get(team.name), "teamB");
        Assertions.assertEquals(teamB.get(member.age.avg()), 35);
    }
}
