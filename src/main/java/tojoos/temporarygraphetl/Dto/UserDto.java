package tojoos.temporarygraphetl.Dto;

import tojoos.temporarygraphetl.User;
import java.time.LocalDateTime;

public record UserDto(Long id, String nickname, String sex, String nationality, LocalDateTime creationTimestamp, LocalDateTime followStartDate, LocalDateTime followEndDate) {

  public UserDto(User user) {
    this(user.getId(), user.getNickname(), user.getSex(), user.getNationality(), user.getCreationTimestamp(), null, null);
  }

  public UserDto(User user, LocalDateTime followStartDate, LocalDateTime followEndDate) {
    this(user.getId(), user.getNickname(), user.getSex(), user.getNationality(), user.getCreationTimestamp(), followStartDate, followEndDate);
  }
}
