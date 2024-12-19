package budgetapprefactored.Users;

import budgetapprefactored.Exceptions.DuplicateCategoryException;
import budgetapprefactored.Exceptions.UserNotFoundException;
import budgetapprefactored.Budgets.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<Optional<User>> getUser(@PathVariable String userId){

        Optional<User> userByUserId = userService.getUserByUserId(userId);
        if(userByUserId.isPresent()){
            return new ResponseEntity<>(userByUserId, HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{userId}/categories")
    public ResponseEntity<List<Category>> getAvailableCategories(@PathVariable String userId){
        List<Category> availableCategories = userService.getAvailableCategories(userId);
        if(availableCategories.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else {
            return new ResponseEntity<>(availableCategories, HttpStatus.OK);
        }
    }

    @PostMapping("/{userId}/add-category")
    public ResponseEntity<Optional<Category>> addCategory(@PathVariable String userId, @RequestBody Map<String, String> payload) throws DuplicateCategoryException {
        if (!payload.containsKey("name")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String name = payload.get("name");
        BigDecimal total = payload.containsKey("total") ? new BigDecimal(payload.get("total")) : BigDecimal.ZERO;

        Optional<Category> category;
        try {
            category = userService.addCategory(userId, total, name);
        } catch (DuplicateCategoryException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        if (category.isPresent()) {
            return new ResponseEntity<>(category, HttpStatus.CREATED);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }


    @PatchMapping("/{userId}/modify-budget/{newTotal}/{monthYear}")
    public ResponseEntity<String> modifyBudget(
            @PathVariable String userId,
            @PathVariable BigDecimal newTotal,
            @PathVariable YearMonth monthYear) throws UserNotFoundException {
        boolean success;
        try{
           success = userService.createBudget(userId, newTotal, monthYear);
        }catch (UserNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        if (success) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Budget successfully modified for " + monthYear);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to modify budget. input is invalid.");
        }
    }


}

