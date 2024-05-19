package tojoos.temporarygraphetl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tojoos.temporarygraphetl.Dto.ParentUserDto;
import tojoos.temporarygraphetl.Dto.UserDto;
import tojoos.temporarygraphetl.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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


    // This is not optimized, default Spring Data neo4j performs select on ever single object resulting in very long query time.
    public Set<ParentUserDto> findAllDefault(LocalDateTime timestamp) {
        LocalDateTime finalTimestamp = ensureTimestampIsNotNull(timestamp);

        log.info("Using default findAll query");
        log.debug("Filtering all users that had been created after '{}' with valid follows for given timestamp", timestamp);
        Set<ParentUserDto> foundParentUsers =  userRepository.findAll().stream()
            .filter(u -> u.getCreationTimestamp().isBefore(finalTimestamp))
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

        log.info("Found {} unique users using default findAll query", foundParentUsers.size());
        return foundParentUsers;
    }

    public Set<ParentUserDto> findAllOptimized(LocalDateTime timestamp) {
        timestamp = ensureTimestampIsNotNull(timestamp);

        Set<ParentUserDto> foundParentUsers = this.userRepository.findAllForCurrentTimestamp(timestamp).stream()
            .map(ParentUserDto::new)
            .collect(Collectors.toSet());

        log.info("Found {} unique users using optimized findAll query", foundParentUsers.size());
        return foundParentUsers;
    }

    public Set<ParentUserDto> findAllNoRelationsForCurrentTimestamp(LocalDateTime timestamp) {
        timestamp = ensureTimestampIsNotNull(timestamp);

        Set<ParentUserDto> foundParentUsers = this.userRepository.findAllNoRelationsForCurrentTimestamp(timestamp).stream()
            .map(ParentUserDto::new)
            .collect(Collectors.toSet());

        log.info("Found {} unique users using optimized findAllNoRelations query", foundParentUsers.size());
        return foundParentUsers;
    }

    public ParentUserDto findByIdForCurrentTimestamp(Long id, LocalDateTime timestamp) {
        timestamp = ensureTimestampIsNotNull(timestamp);
        log.debug("Getting user with id={} for given timestamp: {}", id, timestamp);

        Optional<User> foundUser = userRepository.findByIdForCurrentTimestamp(id, timestamp);
        if (foundUser.isPresent()) {
            return new ParentUserDto(foundUser.get());
        } else {
            log.debug("User with id={} not found, returning null", id);
            return null;
        }
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

    public Set<ParentUserDto> updateAll(List<UserDto> users) {
        log.debug("Updating [{}] users...", users.size());

        Set<User> updatedUsers = users.stream()
            .peek(user -> {
                if (user == null) {
                    log.warn("Null user found in the requested update list. Removing it from further processing.");
                }
            })
            .filter(Objects::nonNull)
            .map(user -> {
                User foundUser = this.findById(user.id());
                if (foundUser == null) {
                    // create a new user if not found
                    return new User(user);
                }
                foundUser.setNickname(user.nickname());
                foundUser.setNationality(user.nationality());
                foundUser.setSex(user.sex());
                return foundUser;
            })
            .collect(Collectors.toSet());

        // there is no support for batch neo4j insertion, so it will take quite a lot of time when inserting large number of objects
        return userRepository.saveAll(updatedUsers).stream()
            .map(ParentUserDto::new)
            .collect(Collectors.toSet());
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


    private LocalDateTime ensureTimestampIsNotNull(LocalDateTime timestamp) {
        if (timestamp == null) {
            LocalDateTime now = LocalDateTime.now();
            log.info("Timestamp not specified, using current moment one: {}", now);
            return now;
        }
        return timestamp;
    }
}
