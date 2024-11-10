package com.ali.sample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
//@CrossOrigin(origins = "http://localhost:4200")  // Allow Angular frontend
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Get all transactions for a specific user
    @GetMapping("/user/{userId}")
    public List<Transaction> getTransactionsByUser(@PathVariable Long userId) {
        return transactionService.findByUserId(userId);
    }

    // Add a new transaction for a specific user
    @PostMapping("/user/{userId}")
    public Transaction addTransaction(@PathVariable Long userId, @RequestBody Transaction transaction) {
        return transactionService.saveTransaction(userId, transaction);
    }
}
