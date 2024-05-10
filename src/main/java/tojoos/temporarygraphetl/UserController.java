package tojoos.temporarygraphetl;

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
        return new ResponseEntity<>(userService.findAll(timestamp), HttpStatus.OK); //todo remove enddate - just date in time that will be checking current state as for this date
    }

    return new ResponseEntity<>(userService.findAll(), HttpStatus.OK);
  }

  @PutMapping("/update")
  public ResponseEntity<ParentUserDto> update(@RequestBody UserDto userDto) throws UserNotFoundException {
    if (userDto == null || userDto.id() == null) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(userService.update(userDto), HttpStatus.CREATED);
  }

  @PutMapping("/follow")
  public void follows(@RequestBody List<Pair<Long, Long>> pairs) {
    // Your processing logic here
    for (Pair<Long, Long> pair : pairs) {
      Long first = pair.getFirst();
      Long second = pair.getSecond();
      // Process the pair
      System.out.println(first + " ---- nd: ----" + second);
    }
  }

//
//  @GetMapping("/unfollow")
//  public Set<ParentUserDto> follows() {
//    // 1 2
//    // 4 5
//    // 1 2
//  }
}
