package tojoos.temporarygraphetl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tojoos.temporarygraphetl.Dto.ParentUserDto;
import tojoos.temporarygraphetl.Dto.UserDto;
import tojoos.temporarygraphetl.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional("transactionManager")
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Set<ParentUserDto> findAll() {
        return userRepository.findAll().stream()
            .map(ParentUserDto::new)
            .collect(Collectors.toSet());
    }

    public Set<ParentUserDto> findAll(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.findAll().stream()
            .filter(u -> startDate.isAfter(u.getCreationTimestamp()))
            .map(ParentUserDto::new)
            .map(u -> {
                Set<UserDto> filteredUserDtos = u.following().stream() //todo remove enddate - just date in time that will be checking current state as for this date
                    .filter(uFollowing -> startDate.isAfter(uFollowing.followStartDate())
                        && endDate.isBefore(uFollowing.followEndDate()))
                    .collect(Collectors.toSet());
                return new ParentUserDto(u.id(), u.nickname(), u.sex(), u.nationality(), u.creationTimestamp(), filteredUserDtos);
            })
            .collect(Collectors.toSet());
    }

    public User findById(Long id) throws UserNotFoundException {
//        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id={" + id + "} not found."));
        return userRepository.findById(id).orElse(null);
    }

    public User add(User user) {
        user.setCreationTimestamp(LocalDateTime.now());
        return userRepository.save(user);
    }

    public ParentUserDto update(UserDto user) throws UserNotFoundException {
        User foundUser = this.findById(user.id());
        if (foundUser == null) {
            // create a new user in this case
            return new ParentUserDto(this.add(new User(user)));
        }
        foundUser.setNickname(user.nickname());
        foundUser.setNationality(user.nationality());
        foundUser.setSex(user.sex());

        return new ParentUserDto(userRepository.save(foundUser));
    }
}
