package tojoos.temporarygraphetl;

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
  UserRepository userRepository;
  UserService userService;

  public UserController(UserService userService, UserRepository userRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
  }

  @GetMapping("/users")
  public ResponseEntity<Set<ParentUserDto>> findAll(@RequestParam(required = false) LocalDateTime timestamp) {
    if (timestamp != null) {
        return new ResponseEntity<>(userService.findAll(timestamp), HttpStatus.OK);
    }

    return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
  }

  @PutMapping("/update")
  public ResponseEntity<?> update(@RequestBody List<UserDto> userDtos) throws UserNotFoundException {
    Set<ParentUserDto> updatedUsers = new HashSet<>();
    for (UserDto userDto : userDtos) {
      if (userDto == null || userDto.id() == null) {
        return new ResponseEntity<>( "Some users could not be uploaded due to missing id field", HttpStatus.BAD_REQUEST);
      }
      updatedUsers.add(userService.update(userDto));
    }

    return new ResponseEntity<>(updatedUsers, HttpStatus.CREATED);
  }

  @PutMapping("/follow")
  public ResponseEntity<?> follow(@RequestBody List<Pair<Long, Long>> usersPairs) throws ExecutionException, InterruptedException {
    CompletableFuture<Long> successfulOperations = CompletableFuture.supplyAsync(() -> {
      long counter = 0L;
      for (Pair<Long, Long> usersPair : usersPairs) {
        Long first = usersPair.getFirst();
        Long second = usersPair.getSecond();
        // Perform follow process of users
        userService.followUserById(first, second);
        counter++;
      }
      return counter;
    });

    boolean allOperationsSuccessful = usersPairs.size() == successfulOperations.get();
    return allOperationsSuccessful ? new ResponseEntity<>("All operations successful.", HttpStatus.OK) :
        new ResponseEntity<>( "Some operations were not successful", HttpStatus.BAD_REQUEST);
  }

  @PutMapping("/unfollow")
  public ResponseEntity<?> unfollow(@RequestBody List<Pair<Long, Long>> usersPairs) throws ExecutionException, InterruptedException {
    CompletableFuture<Long> successfulOperations = CompletableFuture.supplyAsync(() -> {
      long counter = 0L;
      for (Pair<Long, Long> usersPair : usersPairs) {
        Long first = usersPair.getFirst();
        Long second = usersPair.getSecond();
        // Perform follow process of users
        userService.unfollowUserById(first, second);
        counter++;
      }
      return counter;
    });

    boolean allOperationsSuccessful = usersPairs.size() == successfulOperations.get();
    return allOperationsSuccessful ? new ResponseEntity<>("All operations successful.", HttpStatus.OK) :
        new ResponseEntity<>( "Some operations were not successful", HttpStatus.BAD_REQUEST);
  }
}
