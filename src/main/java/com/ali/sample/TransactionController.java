package com.ali.sample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
//@CrossOrigin(origins = "http://localhost:4200")  // Allow Angular frontend
public class TransactionController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserService userService;
    // Get all transactions for a specific user
    @GetMapping("/list")
    public List<Transaction> getTransactionsByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = this.userService.findByUsername(userDetails.getUsername()).get().getId(); // Cast to your UserDetails implementation
            return transactionService.findByUserId(userId);
        }
        return null;
    }

    // Add a new transaction for a specific user
    @PostMapping("/add")
    public Transaction addTransaction( @RequestBody Transaction transaction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = this.userService.findByUsername(userDetails.getUsername()).get().getId(); // Cast to your UserDetails implementation
            return transactionService.saveTransaction(userId, transaction);
        }
        return null;
    }
}
