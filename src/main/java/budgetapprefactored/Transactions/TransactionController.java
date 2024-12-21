package budgetapprefactored.Transactions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Arrays;

import static budgetapprefactored.Transactions.Transaction.TransactionType.EXPENSE;
import static budgetapprefactored.Transactions.Transaction.TransactionType.INCOME;

@RestController
@RequestMapping("/api/{userId}/transactions")
public class TransactionController {

private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping()
    public ResponseEntity<Optional<Transaction>> createTransaction(
            @RequestBody Map<String, String> payload,
            @PathVariable String userId) {

        List<String> requiredFields = Arrays.asList("amount", "accountId", "name", "type", "dateTime", "category");

        for (String field : requiredFields) {
            if (!payload.containsKey(field) || payload.get(field).isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        try {
            BigDecimal amount = new BigDecimal(payload.get("amount"));
            String accountId = payload.get("accountId");
            String name = payload.get("name");
            String description = payload.getOrDefault("description", "");
            String category = payload.get("category");
            LocalDate dateTime = LocalDate.parse(payload.get("dateTime"));
            Transaction.TransactionType type;

            try {
                type = Transaction.TransactionType.valueOf(payload.get("type").toUpperCase());
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Invalid type
            }

            if (type != Transaction.TransactionType.EXPENSE && type != Transaction.TransactionType.INCOME) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Optional<Transaction> transaction = transactionService.createTransaction(
                    amount, accountId, userId, dateTime, name, description, category, type);

            return transaction.map(transaction1 -> new ResponseEntity<>(transaction,HttpStatus.CREATED)).orElseGet(()-> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        } catch (NumberFormatException | DateTimeParseException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{page}")
    public ResponseEntity<List<Transaction>> getNextPageRecentTransactions(@PathVariable String userId, @PathVariable int page){
        if (page < 1) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            List<Transaction> transactions = transactionService.getNext5RecentTransactions(userId, page);

            if (transactions.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(transactions, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
@GetMapping("/size")
    public ResponseEntity<Map<String,Integer>> getTransactionsSize(@PathVariable String userId){
    try {
        int transactionSize = transactionService.getTransactionSize(userId);

        return new ResponseEntity<>(Map.of("transactionSize", transactionSize), HttpStatus.OK);

    } catch (Exception e) {
        e.printStackTrace();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
}
