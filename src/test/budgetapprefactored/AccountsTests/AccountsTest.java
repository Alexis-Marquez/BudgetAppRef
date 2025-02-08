package budgetapprefactored.AccountsTests;

import budgetapprefactored.Accounts.Account;
import budgetapprefactored.Accounts.AccountRepository;
import budgetapprefactored.Accounts.AccountService;
import budgetapprefactored.Users.UserService;
import budgetapprefactored.utils.EncryptionUtil;
import net.bytebuddy.utility.dispatcher.JavaDispatcher;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import budgetapprefactored.Users.User;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import org.springframework.context.ApplicationContextInitializer;
import java.net.UnknownServiceException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
    private EncryptionUtil encryptionUtil;

    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.4.2"));

    @BeforeAll
    static void startContainer() {
        mongoDBContainer.start(); // Start MongoDB in a container
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }


    @Test
    public void testConstructor() throws Exception {
        Account accountTest = new Account("0000", "savings", "Test0", BigDecimal.ONE);
        assertNotNull(accountTest);
    }
    @Test
    public void testConstructor2() throws Exception {
        Account accountTest = new Account("0000", "savings", "Test0", BigDecimal.ONE);
        assertEquals(accountTest.getBalance(), BigDecimal.ONE);
    }

    @Test
    public void testAccountUserConnection() throws Exception {
        Optional<User> userOptional = userService.getUserByUserId(user.getUserId());
        assertTrue(userOptional.isPresent(), "User should exist in the system");

        User refreshedUser = userOptional.orElseThrow(() -> new Exception("User not found!"));
        List<Account> accountList = refreshedUser.getAccountList();

        assertNotNull(accountList, "Account list should not be null");
        assertFalse(accountList.isEmpty(), "Account list should not be empty");
        assertTrue(accountList.stream().anyMatch(acc -> acc.getAccountId().equals(account.getAccountId())),
                "Account list should contain the test account by ID");
    }




    @BeforeEach
    void setUp() throws Exception {
        user = userService.createUser("test", "test@email.com", "pass");
        account = accountService.createAccount(user.getUserId(), "savings", "Test Account", BigDecimal.valueOf(100))
                .orElseThrow(() -> new Exception("Account creation failed"));
        assertNotNull(userService.getUserByUserId(user.getUserId()), "User should be created before tests");
    }


    @AfterEach
    void tearDown() throws Exception {
        if (account != null) {
            accountService.deleteAccountById(account.getAccountId());
        }
        userService.deleteUserByUserId(user.getUserId());
    }

    @AfterAll
    static void stopContainer() {
        mongoDBContainer.stop();
    }


}

