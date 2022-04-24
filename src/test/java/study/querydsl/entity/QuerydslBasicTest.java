package study.querydsl.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.types.QMap;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@Autowired
	EntityManager em;

	// Querydsl에서 사용하는 QueryFactory는 필드 변수로 빼줄 수 있다
	// 초기화는 before()에서 진행한다
	JPAQueryFactory queryFactory; // querydsl은 QueryFactory로 시작한다

	@BeforeEach // 각 테스트 실행 전에 데이터를 넣기 위해 사용
	public void before() {
		queryFactory = new JPAQueryFactory(em); // QueryFactory 초기화
		Team teamA = new Team("TeamA");
		Team teamB = new Team("TeamB");
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

	// JPQL와 querydsl을 비교해보자
	@Test
	public void startJPQL() {
		// JPQL 방식으로 member1을 찾는다.
		String qlString =
			"select m from Member m " +
			"where m.username = :username";

		Member findMember = em.createQuery( qlString, Member.class)
			.setParameter("username", "member1")
			.getSingleResult();

		assertThat(findMember.getUsername()).isEqualTo("member1"); // 이름으로 검증
	}

	// JPQL을 Querydsl로 바꾸기
	// JPQL과의 차이_1: 쿼리를 작성할 때 문자열을 사용하지 않고, 자바 코드처럼 쿼리를 작성하기 때문에 컴파일 시점에서 에러를 발견할 수 있다
	// JPQL과의 차이_2: 쿼리에 파라미터 값을 직접 넣어주지 않아도 자동으로 파라미터 바인딩을 해서 쿼리를 생성한다
	@Test
	public void startQuerydsl() {
		QMember m = new QMember("m"); // m은 Qmember를 구분하는 별칭같은 거지만, 크게 중요하지 않다.
		// QMember에 이미 만들어져 있는 member 객체를 사용한다

		Member findMember = queryFactory
			.select(m)
			.from(m)
			.where(m.username.eq("member1")) // 파라미터 바인딩을 하지 않고 eq을 사용한다. 자동으로 바인딩 된다.
			.fetchOne(); //

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

}
