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
import java.util.ArrayList;
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

    @Test
    public void testGetAccountsByUserIdNoContent(){
        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountsByUserId(null));
    }

    @Test
    public void testGetAccountsByUserId() throws Exception {
        List<Account> accountList = accountService.getAccountsByUserId(user.getUserId());
        assertNotNull(accountList, "Account list should not be null");
        assertDoesNotThrow(()->accountService.createAccount(user.getUserId(), "test","name",BigDecimal.ONE));
        assertFalse(accountList.isEmpty(), "Account list should not be empty");
    }

    @Test
    public void testGetSingleAccount(){
        Optional<Account> account1 = accountService.singleAccountByUserId(account.getAccountId(),user.getUserId());
        assertTrue(account1.isPresent(), "Account should exist in the system");
    }

    @Test
    public void testGetSingleAccount2() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.singleAccountByUserId(account.getAccountId(),null));
    }

    @Test
    public void testGetSingleAccount3() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.singleAccountByUserId(null,null));
    }
    @Test
    public void testGetSingleAccount4() throws Exception {
        Optional<Account> account1 = accountService.singleAccount(account.getAccountId());
        assertTrue(account1.isPresent(), "Account should exist in the system");
    }

    @Test
    public void testGetSingleAccount5() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.singleAccount(null));
    }

    @Test
    public void testGetSingleAccount6() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.singleAccount(""));
    }

    @Test
    public void testGetSingleAccount7() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> accountService.singleAccountByUserId(null, user.getUserId()));
    }

    @Test
    public void testAccountsByTypeAndUser(){
        ArrayList<Account> accountList = accountService.accountsByTypeAndUserId("savings", user.getUserId());
        assertNotNull(accountList, "Account list should not be null");
        assertFalse(accountList.isEmpty(), "Expected non-empty account list");
        assertTrue(accountList.stream().anyMatch(a -> a.getAccountId().equals(account.getAccountId())),
                "Account list should contain an account with id " + account.getAccountId());
    }

    @Test
    public void testAccountsByTypeAndUser2(){
        assertThrows(IllegalArgumentException.class, ()->accountService.accountsByTypeAndUserId("savings", null));
    }

    @Test
    public void testAccountsByTypeAndUser3(){
        assertThrows(IllegalArgumentException.class, ()->accountService.accountsByTypeAndUserId(null, user.getUserId()));
    }

    @Test
    public void testAccountsByTypeAndUser4(){
        ArrayList<Account> accountList = accountService.accountsByTypeAndUserId("savings", "29382");
        assertNotNull(accountList, "Account list should not be null");
        assertTrue(accountList.isEmpty(), "Account list should be empty");
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

