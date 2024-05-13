package tojoos.temporarygraphetl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tojoos.temporarygraphetl.Dto.ParentUserDto;
import tojoos.temporarygraphetl.Dto.UserDto;
import tojoos.temporarygraphetl.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional("transactionManager")
public class UserService {

    private final static Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Set<ParentUserDto> findAll() {
        log.info("Timestamp not specified, getting latest data");
        return this.findAll(LocalDateTime.now());
    }

    public Set<ParentUserDto> findAll(LocalDateTime timestamp) {
        log.debug("Filtering all users that had been created after '{}' with valid follows for given timestamp", timestamp);
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
        Optional<User> foundUser = userRepository.findById(id);
        if (foundUser.isPresent()) {
            return foundUser.get();
        } else {
            log.debug("User with id={} not found, returning null", id);
            return null;
        }
    }

    public User add(User user) {
        log.debug("Saving new user '{}' with id={} in database", user.getNickname(), user.getId());
        return userRepository.save(user);
    }

    public ParentUserDto update(UserDto user) {
        log.debug("Updating user '{}' with id={}", user.nickname(), user.id());
        User foundUser = this.findById(user.id());
        if (foundUser == null) {
            // create a new user if not found
            return new ParentUserDto(this.add(new User(user)));
        }
        foundUser.setNickname(user.nickname());
        foundUser.setNationality(user.nationality());
        foundUser.setSex(user.sex());

        return new ParentUserDto(userRepository.save(foundUser));
    }

    public void followUserById(Long followerId, Long userId) throws UserNotFoundException {
        this.followUser(this.findById(followerId), this.findById(userId));
    }

    public void unfollowUserById(Long followerId, Long userId) throws UserNotFoundException {
        this.unfollowUser(this.findById(followerId), this.findById(userId));
    }

    public void followUser(User follower, User user) throws UserNotFoundException {
        if (follower != null && user != null) {
            log.debug("Creating new following relationship between ['{}', id={}] -> ['{}', id={}]",
                follower.getNickname(), follower.getId(), user.getNickname(), user.getId());
            follower.follow(user);
            userRepository.save(follower);
        } else {
            log.debug("Provided users are invalid. Follow relationship will not be created.");
            throw new UserNotFoundException("Provided users are invalid. Follow relationship will not be created.");
        }
    }

    public void unfollowUser(User follower, User user) throws UserNotFoundException {
        if (follower != null && user != null) {
            log.debug("Deleting follow relationship between ['{}', id={}] -> ['{}', id={}]",
                follower.getNickname(), follower.getId(), user.getNickname(), user.getId());
            follower.unfollow(user);
            userRepository.save(follower);
        } else {
            log.debug("Provided users are invalid. Follow relationship will not be deleted.");
            throw new UserNotFoundException("Provided users are invalid. Follow relationship will not be deleted.");
        }
    }
}
