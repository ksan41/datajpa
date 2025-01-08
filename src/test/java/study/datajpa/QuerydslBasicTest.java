package study.datajpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import static study.datajpa.entity.QMember.*;

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
}
