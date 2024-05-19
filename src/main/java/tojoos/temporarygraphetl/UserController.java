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
  public ResponseEntity<Set<ParentUserDto>> findAll(@RequestParam(required = false) LocalDateTime timestamp,
                                                    @RequestParam(required = false, defaultValue = "true") Boolean optimized) {
    log.info("Received incoming request... GET '/users'");
    Set<ParentUserDto> usersFound = optimized ? userService.findAllOptimized(timestamp) : userService.findAllDefault(timestamp);

    log.info("Successfully finished processing of request GET '/users'");
    return new ResponseEntity<>(usersFound, HttpStatus.OK);
  }

  @GetMapping("/no-relations/users")
  public ResponseEntity<Set<ParentUserDto>> findAllNoRelations(@RequestParam(required = false) LocalDateTime timestamp) {
    log.info("Received incoming request... GET '/no-relations/users'");
    Set<ParentUserDto> usersFound = userService.findAllNoRelationsForCurrentTimestamp(timestamp);

    log.info("Successfully finished processing of request GET '/no-relations/users'");
    return new ResponseEntity<>(usersFound, HttpStatus.OK);
  }

  @GetMapping("/user/{id}")
  public ResponseEntity<ParentUserDto> findById(@RequestParam(required = false) LocalDateTime timestamp,
                                                @PathVariable Long id) {
    log.info("Received incoming request... GET '/use/{}'", id);
    ParentUserDto userFound = this.userService.findByIdForCurrentTimestamp(id, timestamp);

    log.info("Successfully finished processing of request GET '/user/{}'", id);
    return new ResponseEntity<>(userFound, HttpStatus.OK);
  }

  @PutMapping("/update")
  public ResponseEntity<Set<ParentUserDto>> update(@RequestBody List<UserDto> userDtos) {
    log.info("Received incoming request... PUT '/update': " + userDtos);

    Set<ParentUserDto> updatedUsers = this.userService.updateAll(userDtos);

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
