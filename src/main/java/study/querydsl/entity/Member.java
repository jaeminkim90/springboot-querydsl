package study.querydsl.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자가 필요하다. 기본 생성자는 Protected level까지 가능하다
@ToString(of = {"id", "username", "age"}) // 편의를 위해 ToString을 만든다. 자동으로 만들어 준다
public class Member {

	@Id
	@GeneratedValue
	@Column(name = "member_Id")
	private Long Id;
	private String username;
	private int age;

	// 연관 관계의 주인
	@ManyToOne(fetch = FetchType.LAZY) // XToOne의 관계는 LAZY 처리를 필수로 해주어야 한다
	@JoinColumn(name = "team_id") // 외례키 이름을 명시하여 연관관계를 맺어줄 수 있다
	private Team team;

	public Member(String username) {
		this(username, 0);
	}

	public Member(String username, int age) {
		this(username, age, null);
	}

	public Member(String username, int age, Team team) {
		this.username = username;
		this.age = age;
		if (team != null) {
			changeTeam(team);
		}
	}

	public void changeTeam(Team team) {
		this.team = team; // member 필드에 team을 넣어주고
		team.getMembers().add(this); // team 필드에 Member를 넣어 양방향 참조 관계를 만들어 준다.
	}

	// @ToString(of = {"id", "username", "age"})와 같은 기능을 한다
	// 주의할 점은 Team 처럼 양방향 연관 관계를 참조하고 있는 엔티티를 넣으면 안된다. 데이터 조회 과정에서 무한 루프에 빠질 수 있다.
//	@Override
//	public String toString() {
//		return "Member{" +
//			"Id=" + Id +
//			", username='" + username + '\'' +
//			", age=" + age +
//			'}';
//	}
}
