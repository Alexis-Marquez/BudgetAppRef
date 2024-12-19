package budgetapprefactored.Accounts;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/{userId}")
public class AccountsController {
    private final AccountService accountService;

    public AccountsController(AccountService accountService) {
        this.accountService = accountService;
    }

    @DeleteMapping("accounts/{id}")
    public ResponseEntity<Void> deleteAccountById(@PathVariable String id) throws AccountNotFoundException {
        try {
            boolean deleted = accountService.deleteAccountById(id);
            if (deleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }catch (AccountNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts(@PathVariable String userId){
        Optional<List<Account>> accounts = accountService.getAccountsByUserId(userId);
        return accounts.map(accountList -> new ResponseEntity<>(accountList, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<Account> getSingleAccount(@PathVariable String id, @PathVariable String userId){
        Optional<Account> account = accountService.singleAccountByUserId(id, userId);
        return account.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("accounts/new-account")
    public ResponseEntity<Account> createAccount(@RequestBody AccountRequest payload, @PathVariable String userId) {
        try {
            Optional<Account> account = accountService.createAccount(
                    userId,
                    payload.getType(),
                    payload.getName(),
                    payload.getBalance()
            );
            return account.map(value -> new ResponseEntity<>(value, HttpStatus.CREATED))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        catch (DuplicateAccountException e){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
}

