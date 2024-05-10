package tojoos.temporarygraphetl.Dto;

import tojoos.temporarygraphetl.User;

import java.time.LocalDateTime;
import java.util.Set;

public record ParentUserDto(Long id, String nickname, String sex, String nationality, LocalDateTime creationTimestamp, Set<UserDto> following) {

  public ParentUserDto(User user) {
    this(user.getId(), user.getNickname(), user.getSex(), user.getNationality(), user.getCreationTimestamp(), user.getFollowingUsersAsDtos());
  }
}
