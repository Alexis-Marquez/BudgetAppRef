package budgetapprefactored.Accounts;

import budgetapprefactored.Transactions.TransactionRepository;
import budgetapprefactored.Users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import budgetapprefactored.Users.User;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MongoTemplate mongoTemplate;
    private final UserService userService;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository, MongoTemplate mongoTemplate, UserService userService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.mongoTemplate = mongoTemplate;
        this.userService = userService;
    }

    public Optional<List<Account>> getAccountsByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be null or empty.");
        }
        List<Account> accounts = accountRepository.findAccountsByUserId(userId);
        return accounts.isEmpty() ? Optional.empty() : Optional.of(accounts);
    }

    public Optional<Account> singleAccountByUserId(String id, String userId){
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be null or empty.");
        }
        return accountRepository.findAccountByAccountIdAndUserId(id, userId);
    }

    public Optional<Account> singleAccount(String id){
        return accountRepository.findAccountByAccountId(id);
    }

    public Optional<ArrayList<Account>> accountsByTypeAndUserId(String type, String userId){
        return accountRepository.findAccountsByTypeIgnoreCaseAndUserId(type, userId);
    }

    @Transactional
    public Optional<Account> createAccount(String userId, String type, String name, BigDecimal balance)
            throws DuplicateAccountException, UserNotFoundException {
        if (type == null || type.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("Account type and name must not be null or empty.");
        }
        boolean accountExists = accountRepository.existsByUserIdAndName(userId, name);
        if (accountExists) {
            throw new DuplicateAccountException("An account with this name already exists for the user.");
        }
        if (userService.getUserByUserId(userId).isEmpty()) {
            throw new UserNotFoundException("User with ID " + userId + " does not exist.");
        }

        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        Account account = accountRepository.insert(new Account(userId, type, name, balance));
        mongoTemplate.update(User.class)
                .matching(Criteria.where("userId").is(userId))
                .apply(new Update().push("accountList", account))
                .first();

        return Optional.of(account);
    }


    @Transactional
    public boolean deleteAccountById(String id) throws AccountNotFoundException {
        Optional<Account> account = accountRepository.findAccountByAccountId(id);
        if (account.isEmpty()) {
            throw new AccountNotFoundException("Account with ID " + id + " not found.");
        }
        transactionRepository.deleteAllByAccountId(id);

        accountRepository.deleteAccountByAccountId(id);
        return false;
    }


}