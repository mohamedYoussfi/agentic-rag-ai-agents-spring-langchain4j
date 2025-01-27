package net.youssfi.transactionservice.agents;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.output.structured.Description;
import lombok.extern.slf4j.Slf4j;
import net.youssfi.transactionservice.entities.Transaction;
import net.youssfi.transactionservice.entities.TransactionStatus;
import net.youssfi.transactionservice.repository.TransactionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class TransactionAiTools {
    private TransactionRepository transactionRepository;

    public TransactionAiTools(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }
    @Tool("Get All Transactions")
    public List<Transaction> getAllTransactions(){
        return transactionRepository.findAll();
    }
    @Tool("Get All Transactions By Account ID")
    public List<Transaction> getAllTransactionsByAccountId(long accountId){
        return transactionRepository.findByAccountId(accountId);
    }
    @Tool
    public Transaction updateTransactionStatus(Long transactionId,TransactionStatus transactionStatus){
        Transaction transaction = transactionRepository.findById(transactionId).get();
        transaction.setStatus(transactionStatus);
        transactionRepository.save(transaction);
        return transaction;
    }
}
