package study.querydsl.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

		Member findMember = em.createQuery(qlString, Member.class)
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

	@Test
	public void startQuerydsl2() {

		QMember m1 = new QMember("m1");

		Member findMember = queryFactory
			.select(m1)
			.from(m1)
			.where(m1.username.eq("member1")) // 파라미터 바인딩을 하지 않고 eq을 사용한다. 자동으로 바인딩 된다.
			.fetchOne(); //

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void search() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(member.username.eq("member1").and(member.age.eq(10)))
			.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void searchAndParam() {
		Member findMember = queryFactory
			.selectFrom(member)
			.where(
				//member.username.eq("member1").and(member.age.eq(10))
				member.username.eq("member1"),
				(member.age.eq(10)), null
			)
			.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void resultFetch() {
		List<Member> fetch = queryFactory
			.selectFrom(member)
			.fetch(); // List를 조회, 조회 결과 없을 경우 빈 리스트 반환

//		Member fetchOne = queryFactory
//			.selectFrom(QMember.member)
//			.fetchOne(); // 단건을 조회할 때 사용

		queryFactory
			.selectFrom(member)
			//.limit(1).fetchOne();
			.fetchFirst(); // fetchFirst와 FetchOne은 동일한 기능이다

		QueryResults<Member> results = queryFactory
			.selectFrom(member)
			.fetchResults();

		// fetchResults()는 getTotal()을 제공한다. 쿼리가 2번 실행된다. select count 쿼리를 한 번 더 날린다.
		results.getTotal();
		// fetchResults()로 조회된 실제 데이터를 꺼내올 때는 getResults()를 사용한다.
		List<Member> content = results.getResults();
		// 그 외에 페이징에 사용할 수 있는getLimit()와 getOffset()도 사용이 가능하다.

		queryFactory
			.selectFrom(member)
			.fetchCount(); // count만 가져오는 쿼리. select 조건을 지우고 count 쿼리가 나간다
	/**
	 * fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
	 * fetchOne() : 단 건 조회
	 * 		결과가 없으면 : null
	 * 		결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
	 * fetchFirst() : limit(1).fetchOne()
	 * fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
	 * fetchCount() : count 쿼리로 변경해서 count 수 조회
	 */

	/**
	 * JPQL이 제공하는 모든 검색 조건 제공
	 *     member.username.eq("member1") // username = 'member1'
	 *     member.username.ne("member1") //username != 'member1'
	 *     member.username.eq("member1").not() // username != 'member1'
	 *
	 * 	   member.username.isNotNull() //이름이 is not null
	 *
	 *     member.age.in(10, 20) // age in (10,20)
	 *     member.age.notIn(10, 20) // age not in (10, 20)
	 *     member.age.between(10,30) //between 10, 30
	 *
	 *     member.age.goe(30) // age >= 30
	 *     member.age.gt(30) // age > 30
	 *     member.age.loe(30) // age <= 30
	 *     member.age.lt(30) // age < 30
	 *
	 * 	    member.username.like("member%") //like 검색
	 * 	    member.username.contains("member") // like ‘%member%’ 검색
	 * 	    member.username.startsWith("member") //like ‘member%’ 검색
	 */
	}

	/**
	 * 회원 정렬 순서
	 * 1. 회원 나이 내림차순(desc)
	 * 2. 회원 이름 올림차순(asc)
	 * 단, 2에서 회원 이름이 없으면 마지막에 출력(null is last)
	 */
	@Test
	public void sort() {
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));

		List<Member> result = queryFactory
			.selectFrom(member)
			.where(member.age.eq(100))
			.orderBy(member.age.desc(),
				member.username.asc().nullsFirst()) // null을 가장 먼저 가져오는 null first 조건도 있다
				//member.username.asc().nullsLast()) // 나이는 내림차순, 이름은 오름차순 정렬한다. null이 있을 경우 마지막에 넣는다
			.fetch();// List를 뽑을 떄는 fetch를 사용한다

		// 조회시 예상되는 객체 -> 나이 조건이 같음으로 이름을 기준으로 오름차순되는 것이 기본 조건이다.
		// null이 있을 경우 last로 빠지기 때문에 아래와 같은 조건으로 객체가 조회될 것을 예상
		Member member5 = result.get(0);
		Member member6 = result.get(1);
		Member memberNull = result.get(2);

		// 검증
		assertThat(member5.getUsername()).isEqualTo("member5");
		assertThat(member6.getUsername()).isEqualTo("member6");
		assertThat(memberNull.getUsername()).isNull();
	}

	// paging
	@Test
	public void paging1() {
		List<Member> result = queryFactory
			.selectFrom(member)
			.orderBy(member.username.desc())
			.offset(1) // 시작하는 row를 설정(시작 페이지가 아니다)
			.limit(2) // 한 화면에 보여줄 데이터의 갯수
			.fetch();// list를 조회

		assertThat(result.size()).isEqualTo(2);
		for (Member member1 : result) {
			System.out.println("member1 = " + member1);
		}

	}
}


