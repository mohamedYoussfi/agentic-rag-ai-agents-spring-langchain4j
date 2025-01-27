package net.youssfi.transactionservice.web;

import dev.langchain4j.model.chat.ChatLanguageModel;
import net.youssfi.transactionservice.agents.TransactionAiAgent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@CrossOrigin("*")
public class AiAssistantController {
    private TransactionAiAgent transactionAiAgent;

    public AiAssistantController(TransactionAiAgent transactionAiAgent) {
        this.transactionAiAgent = transactionAiAgent;
    }

    @GetMapping("/askAgent")
    public Flux<String> chat(@RequestParam(defaultValue = "Bonjour") String question){
        return transactionAiAgent.chat(question);
    }
}
