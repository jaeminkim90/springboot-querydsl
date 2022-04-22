package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.QHello;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

	// @Autowired // 스프리스프링 최신 버전에서는 Autowired 사용 가능
	@PersistenceContext // 다른 프레임워크 사용 가능성이 있다면 이걸 사용하자
		EntityManager em;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello); // 엔티티 영속성 컨텍스트 저장

		JPAQueryFactory query = new JPAQueryFactory(em); // JPAQueryFactory 사용 권장
		QHello qHello = new QHello("h");

		// QueryDSL 사용 예시
		// 쿼리와 관련된 것은 모두 Q타입을 사용한다.
		Hello result = query
			.selectFrom(qHello)
			.fetchOne();

		// 엔티티 동일성 검증
		assertThat(result).isEqualTo(hello);

		// lombok 테스트
		assertThat(result.getId()).isEqualTo(hello.getId());
	}
}
