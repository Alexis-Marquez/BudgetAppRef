package budgetapprefactored.AccountsTests;

import budgetapprefactored.Accounts.Account;
import budgetapprefactored.Accounts.AccountRepository;
import budgetapprefactored.Accounts.AccountService;
import budgetapprefactored.Users.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import budgetapprefactored.Users.User;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class AccountsTest {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;

    private Account account;

    private User user;
    @Test
    public void testConstructor(){
        Account account = new Account("0000", "savings", "Test0", BigDecimal.ONE);
        assertNotNull(account);
    }
    @Test
    public void testConstructor2(){
        Account account = new Account("0000", "savings", "Test0", BigDecimal.ZERO);
        assertEquals(account.getBalance(), BigDecimal.ZERO);
    }

    @BeforeEach
    void setUp() {
        user = new User("test", "test@email.com", "pass");
        String userId = user.getUserId();
        account = accountRepository.insert(new Account("0000", "savings", "Test0", BigDecimal.ONE));
        mongoTemplate.update(User.class)
                .matching(Criteria.where("userId").is(userId))
                .apply(new Update().push("accountList").value(account))
                .first();
    }

    @AfterEach
    void tearDown() throws AccountNotFoundException {
        accountService.deleteAccountById(account.getAccountId());
        userService.deleteUserByUserId(user.getUserId());
    }


}

