package study.datajpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.entity.Hello;
import study.datajpa.entity.QHello;

@SpringBootTest
@Transactional
class DatajpaApplicationTests {

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoads() {
        Hello hello = new Hello();
        em.persist(hello);

        JPAQueryFactory query = new JPAQueryFactory(em);
        QHello qHello = new QHello("h");

        Hello result = query
                    .selectFrom(qHello)
                    .fetchOne();
        Assertions.assertEquals(result, hello);
        Assertions.assertEquals(result.getId(), hello.getId());
    }

}
