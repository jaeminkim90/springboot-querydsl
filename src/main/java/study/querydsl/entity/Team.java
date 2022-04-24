package study.querydsl.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자가 필요하다. 기본 생성자는 Protected level까지 가능하다
@ToString(of = {"id", "name", })
public class Team {

	@Id
	@GeneratedValue
	private Long Id;
	private String name;

	// 연관관계 거울(주인은 mamber)
	@OneToMany(mappedBy = "team") // 연관관계 주인을 설정한다
	private List<Member> members = new ArrayList<>();

	public Team(String name) {
		this.name = name;
	}
}
