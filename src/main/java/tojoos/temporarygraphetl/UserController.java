package tojoos.temporarygraphetl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tojoos.temporarygraphetl.Dto.ParentUserDto;
import tojoos.temporarygraphetl.Dto.UserDto;
import tojoos.temporarygraphetl.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class UserController {

  private final static Logger log = LoggerFactory.getLogger(UserController.class);

  UserRepository userRepository;
  UserService userService;

  public UserController(UserService userService, UserRepository userRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
  }

  @GetMapping("/users")
  public ResponseEntity<Set<ParentUserDto>> findAll(@RequestParam(required = false) LocalDateTime timestamp) {
    log.info("Received incoming request... GET '/users'");
    Set<ParentUserDto> usersFound;
    if (timestamp != null) {
      usersFound = userService.findAll(timestamp);
      log.info("Successfully finished processing of request GET '/users'");
      return new ResponseEntity<>(usersFound, HttpStatus.OK);
    }

    usersFound = userService.findAll();
    log.info("Successfully finished processing of request GET '/users'");
    return new ResponseEntity<>(usersFound, HttpStatus.OK);
  }

  @PutMapping("/update")
  public ResponseEntity<?> update(@RequestBody List<UserDto> userDtos) throws UserNotFoundException {
    log.info("Received incoming request... PUT '/update': " + userDtos);
    Set<ParentUserDto> updatedUsers = new HashSet<>();
    for (UserDto userDto : userDtos) {
      if (userDto == null || userDto.id() == null) {
        log.error("User or userId is not set, aborting further processing");
        return new ResponseEntity<>( "Some users could not be uploaded due to missing id field", HttpStatus.BAD_REQUEST);
      }
      updatedUsers.add(userService.update(userDto));
    }

    log.info("Successfully finished processing of PUT '/update': " + userDtos);
    return new ResponseEntity<>(updatedUsers, HttpStatus.OK);
  }

  @PutMapping("/follow")
  public ResponseEntity<?> follow(@RequestBody List<Pair<Long, Long>> usersPairs) throws ExecutionException, InterruptedException {
    log.info("Received incoming request... PUT '/follow': " + usersPairs.toString());
    CompletableFuture<Long> successfulOperations = CompletableFuture.supplyAsync(() -> {
      long counter = 0L;
      for (Pair<Long, Long> usersPair : usersPairs) {
        Long first = usersPair.getFirst();
        Long second = usersPair.getSecond();
        // Perform follow process for each id pair
        try {
          userService.followUserById(first, second);
          counter++;
        } catch (UserNotFoundException e) {
          log.error("Pair '{}' contains invalid user ids, follow relationship will not be created for these.", usersPair);
        }
      }
      return counter;
    });

    boolean allOperationsSuccessful = usersPairs.size() == successfulOperations.get();
    log.info("Finished processing of PUT '/follow': " + usersPairs);
    return allOperationsSuccessful ? new ResponseEntity<>("All operations successful.", HttpStatus.OK) :
        new ResponseEntity<>( "Some operations were not successful", HttpStatus.BAD_REQUEST);
  }

  @PutMapping("/unfollow")
  public ResponseEntity<?> unfollow(@RequestBody List<Pair<Long, Long>> usersPairs) throws ExecutionException, InterruptedException {
    log.info("Received incoming request... PUT '/unfollow': " + usersPairs.toString());
    CompletableFuture<Long> successfulOperations = CompletableFuture.supplyAsync(() -> {
      long counter = 0L;
      for (Pair<Long, Long> usersPair : usersPairs) {
        Long first = usersPair.getFirst();
        Long second = usersPair.getSecond();
        // Perform unfollow process for each id pair
        try {
          userService.unfollowUserById(first, second);
          counter++;
        } catch (UserNotFoundException e) {
          log.error("Pair '{}' contains invalid user ids, follow relationship will not be updated for these.", usersPair);
        }
      }
      return counter;
    });

    boolean allOperationsSuccessful = usersPairs.size() == successfulOperations.get();
    log.info("Finished processing of PUT '/unfollow': " + usersPairs);
    return allOperationsSuccessful ? new ResponseEntity<>("All operations successful.", HttpStatus.OK) :
        new ResponseEntity<>( "Some operations were not successful", HttpStatus.BAD_REQUEST);
  }
}
