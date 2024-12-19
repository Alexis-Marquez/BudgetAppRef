package budgetapprefactored.Users;

import budgetapprefactored.Budgets.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
        return new ResponseEntity<>(userService.getUserByUserId(userId), HttpStatus.OK);
    }

    @GetMapping("/{userId}/categories")
    public ResponseEntity<List<Category>> getAvailableCategories(@PathVariable String userId){
        return new ResponseEntity<>(userService.getAvailableCategories(userId), HttpStatus.OK);
    }

    @PostMapping("/{userId}/add-category")
    public ResponseEntity<Optional<Category>> addCategory(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        Optional<Category> category;
        if (payload.containsKey("name")) {
            if (payload.containsKey("total")) {
                category = userService.addCategory(userId, new BigDecimal(payload.get("total")), payload.get("name"));
            }
            category = userService.addCategory(userId, BigDecimal.ZERO, payload.get("name"));
        } else {
            category = Optional.empty();
        }
        if (category.isPresent()) {
            return new ResponseEntity<Optional<Category>>(category, HttpStatus.CREATED);
        }
        return new ResponseEntity<Optional<Category>>(HttpStatus.BAD_REQUEST);
    }

    @PatchMapping("/{userId}/modify-budget/{newTotal}/{monthYear}") //Only use when creating the first month budget of a new account or when modifying the current budget limit
    public ResponseEntity<Boolean> modifyBudget(@PathVariable String userId, @PathVariable BigDecimal newTotal, @PathVariable YearMonth monthYear){
        boolean budget = userService.createBudget(userId, newTotal, monthYear);
        if (budget) {
            return new ResponseEntity<>(true, HttpStatus.ACCEPTED);
        }else{
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

}

