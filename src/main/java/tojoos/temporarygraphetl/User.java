package tojoos.temporarygraphetl;

import com.fasterxml.jackson.annotation.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import tojoos.temporarygraphetl.Dto.UserDto;

import java.time.LocalDateTime;
import java.util.*;

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
  private Set<FollowsRelationship> following = new HashSet<>();

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
    // check if user is already not followed
    FollowsRelationship followsRelationshipFound = this.findFollowedRelationshipByUserId(user.id);
    if (followsRelationshipFound == null) {
      //todo handle situation where follow had already been ended (by followEndDate) and somebody re-followed
      following.add(new FollowsRelationship(user));
    }
  }

  public void follow(User user, LocalDateTime followingSince) {
    if (following == null) {
      following = new HashSet<>();
    }
    // if user is already followed don't do anything
    FollowsRelationship followsRelationshipFound = this.findFollowedRelationshipByUserId(user.id);
    if (followsRelationshipFound == null) {
      //todo handle situation where follow had already been ended (by followEndDate) and somebody re-followed
      following.add(new FollowsRelationship(user, followingSince));
    }
  }

  public void unfollow(User user) {
    if (following == null) {
      following = new HashSet<>();
    } else {
      FollowsRelationship followsRelationshipFound = this.findFollowedRelationshipByUserId(user.id);
      // check if user has already been followed
      if (followsRelationshipFound != null) {
        LocalDateTime now = LocalDateTime.now();
        // check if user is followed when trying to unfollow
        if (followsRelationshipFound.isUserFollowed(now)) {
          //todo handle situation where follow had already been ended (by followEndDate)
          followsRelationshipFound.setFollowEndDate(now);
        }
      }
    }
  }

  private User findFollowedUserById(Long id) {
    FollowsRelationship foundRelationship = this.findFollowedRelationshipByUserId(id);
    if (foundRelationship != null) {
      return foundRelationship.getUser();
    } else {
      return null;
    }
  }

  private FollowsRelationship findFollowedRelationshipByUserId(Long id) {
    return following.stream()
        .filter(relationship -> relationship.getUser().id.equals(id))
        .findFirst()
        .orElse(null);
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    User user = (User) o;

    if (!id.equals(user.id)) return false;
    if (!Objects.equals(nickname, user.nickname)) return false;
    if (!Objects.equals(sex, user.sex)) return false;
    if (!Objects.equals(nationality, user.nationality)) return false;
    return Objects.equals(creationTimestamp, user.creationTimestamp);
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + (nickname != null ? nickname.hashCode() : 0);
    result = 31 * result + (sex != null ? sex.hashCode() : 0);
    result = 31 * result + (nationality != null ? nationality.hashCode() : 0);
    result = 31 * result + (creationTimestamp != null ? creationTimestamp.hashCode() : 0);
    return result;
  }
}
