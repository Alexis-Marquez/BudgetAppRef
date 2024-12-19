package budgetapprefactored.Transactions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/{userId}/transactions")
public class TransactionController {

private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Optional<Transaction>> createTransaction(@RequestBody Map<String, String> payload, @PathVariable String userId) {
    if(payload.containsKey("amount")&& payload.containsKey("accountId")&&payload.containsKey("name")&&payload.containsKey("type")&&payload.containsKey("dateTime")&&payload.containsKey("category")) {
        if(payload.get("amount")==null&&payload.get("accountId")==null&&payload.get("name")==null&&payload.get("type")==null&&payload.get("dateTime")==null&&payload.get("category")==null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String type = payload.get("type");
        if(Objects.equals(type, "expense") || Objects.equals(type, "income")) {
        try {
            BigDecimal amount = new BigDecimal(payload.get("amount"));
            Optional<Transaction> transaction = transactionService.createTransaction(amount,
                    payload.get("accountId"), userId, LocalDate.parse(payload.get("dateTime")), payload.get("name"), payload.get("description"),
                    payload.get("category"), type);
            if(transaction.isPresent()) {
                return new ResponseEntity<>(transaction, HttpStatus.CREATED);
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }}
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
}
@GetMapping("/{page}")
    public ResponseEntity<List<Transaction>> getNextPageRecentTransactions(@PathVariable String userId, @PathVariable int page){
    return new ResponseEntity<>(transactionService.getNext5RecentTransactions(userId, page),HttpStatus.OK);
}
@GetMapping("/size")
    public ResponseEntity<String> getTransactionsSize(@PathVariable String userId){
    return new ResponseEntity<>(String.valueOf(transactionService.getTransactionSize(userId)), HttpStatus.OK);
}
}
