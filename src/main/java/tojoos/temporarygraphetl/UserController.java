package tojoos.temporarygraphetl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tojoos.temporarygraphetl.Dto.ParentUserDto;
import tojoos.temporarygraphetl.Dto.UserDto;
import tojoos.temporarygraphetl.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
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
  public ResponseEntity<Set<ParentUserDto>> findAll(@RequestParam(required = false) LocalDateTime startDate,
                                    @RequestParam(required = false) LocalDateTime endDate) {
    System.out.println(startDate);
    System.out.println(endDate);
    if (startDate != null && endDate != null) {
        return new ResponseEntity<>(userService.findAll(startDate, endDate), HttpStatus.OK); //todo remove enddate - just date in time that will be checking current state as for this date
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
}
