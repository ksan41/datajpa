package study.datajpa.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import study.datajpa.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Member save(Member member) {
        entityManager.persist(member);
        return member;
    }

    public Member find(Long id) {
        return entityManager.find(Member.class, id);
    }

    public void delete(Member member) {
        entityManager.remove(member);
    }

    public List<Member> findAll() {
        return entityManager.createQuery("select m from Member m", Member.class).getResultList();
    }

    public Optional<Member> findById(Long id) {
        Member member = entityManager.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public long count() {
        return entityManager.createQuery("select count(m) from Member m", Long.class).getSingleResult();
    }
}