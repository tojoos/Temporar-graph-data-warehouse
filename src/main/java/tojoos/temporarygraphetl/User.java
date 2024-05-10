package tojoos.temporarygraphetl;

import com.fasterxml.jackson.annotation.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import tojoos.temporarygraphetl.Dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Node
public class User {

  @Id
  private Long id;
  private String nickname;
  private String sex;
  private String nationality;
  private LocalDateTime creationTimestamp;

  @JsonIgnoreProperties("user")
  @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
  public Set<FollowsRelationship> following = new HashSet<>();

  public User() {

  }

  public User(Long id, String nickname, String sex, String nationality) {
    this.id = id;
    this.nickname = nickname;
    this.sex = sex;
    this.nationality = nationality;
    this.creationTimestamp = LocalDateTime.now();
  }

  public User(Long id, String nickname, String sex, String nationality, LocalDateTime creationTimestamp) {
    this.id = id;
    this.nickname = nickname;
    this.sex = sex;
    this.nationality = nationality;
    this.creationTimestamp = creationTimestamp == null ? LocalDateTime.now() : creationTimestamp;
  }

  public User(UserDto userDto) {
    this(userDto.id(), userDto.nickname(), userDto.sex(), userDto.nationality(), userDto.creationTimestamp());
  }

  public void follow(User user) {
    if (following == null) {
      following = new HashSet<>();
    }
    following.add(new FollowsRelationship(user));
  }

  public void followsSince(User user, LocalDateTime followingSince) {
    if (following == null) {
      following = new HashSet<>();
    }
    following.add(new FollowsRelationship(user, followingSince));
  }

  public void unfollow(User user) {
    if (following == null) {
      following = new HashSet<>();
    } else {
      following.remove(new FollowsRelationship(user));
    }
  }

  public String toString() {
    return this.nickname + "'s followers => "
        + Optional.ofNullable(this.following).orElse(
            Collections.emptySet()).stream()
        .map(FollowsRelationship::getUser)
        .map(User::getNickname)
        .toList();
  }

  public Set<UserDto> getFollowingUsersAsDtos() {
    Set<UserDto> followingDtos = new HashSet<>();
    if (this.getFollowing() != null && !this.getFollowing().isEmpty()) {
      this.getFollowing()
          .forEach(relationship -> followingDtos.add(
              new UserDto(
                  relationship.getUser(),
                  relationship.getFollowStartDate(),
                  relationship.getFollowEndDate())));
    }
    return followingDtos;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSex() {
    return sex;
  }

  public void setSex(String sex) {
    this.sex = sex;
  }

  public String getNationality() {
    return nationality;
  }

  public void setNationality(String nationality) {
    this.nationality = nationality;
  }

  public Set<FollowsRelationship> getFollowing() {
    return following;
  }

  public void setFollowing(Set<FollowsRelationship> following) {
    this.following = following;
  }

  public LocalDateTime getCreationTimestamp() {
    return creationTimestamp;
  }

  public void setCreationTimestamp(LocalDateTime creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
  }
}
