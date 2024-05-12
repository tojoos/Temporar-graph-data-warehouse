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
        return this.findAll(LocalDateTime.now());
    }

    public Set<ParentUserDto> findAll(LocalDateTime timestamp) {
        return userRepository.findAll().stream()
            .filter(u -> u.getCreationTimestamp().isBefore(timestamp))
            .map(ParentUserDto::new)
            .map(u -> {
                Set<UserDto> filteredUserDtos = u.following().stream()
                    // user had been created after requested timestamp (shouldn't be relevant in real life scenario)
                    .filter(uFollowing -> uFollowing.creationTimestamp().isBefore(timestamp) &&
                        // user started following before requested time and is following till now (hadn't stopped following before timestamp)
                        uFollowing.followStartDate().isBefore(timestamp) && uFollowing.followEndDate().isAfter(timestamp))
                    .collect(Collectors.toSet());
                return new ParentUserDto(u.id(), u.nickname(), u.sex(), u.nationality(), u.creationTimestamp(), filteredUserDtos);
            })
            .collect(Collectors.toSet());
    }

    public User findById(Long id) {
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

    public void followUserById(Long followerId, Long userId) {
        this.followUser(this.findById(followerId), this.findById(userId));
    }

    public void unfollowUserById(Long followerId, Long userId) {
        this.unfollowUser(this.findById(followerId), this.findById(userId));
    }

    public void followUser(User follower, User user) {
        follower.follow(user);
        userRepository.save(follower);
    }

    public void unfollowUser(User follower, User user) {
        follower.unfollow(user);
        userRepository.save(follower);
    }
}
