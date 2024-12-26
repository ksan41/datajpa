package study.datajpa.repository;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import java.util.Objects;

public class MemberSpec {

    public static Specification<Member> teamName(final String teamName) {
        return new Specification<>() {
            @Override
            public Predicate toPredicate(Root<Member> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {

                if (!StringUtils.hasText(teamName)) {
                    return null;
                }

                Join<Member, Team> t = root.join("team", JoinType.INNER); // 회원과 조인
                return criteriaBuilder.equal(t.get("name"), teamName);

            }
        };
    }

    public static Specification<Member> username(final String username) {
        return (Specification<Member>) (root, query, builder) -> {
            return builder.equal(root.get("username"), username);
        };
    }
}
