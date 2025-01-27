package net.youssfi.transactionservice.web;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class VectorStoreController {
    private EmbeddingStore embeddingStore;
    private EmbeddingModel embeddingModel;
    private JdbcTemplate jdbcTemplate;

    public VectorStoreController(EmbeddingStore embeddingStore, EmbeddingModel embeddingModel, JdbcTemplate jdbcTemplate) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/getEmbeddings")
    public List<String> getAllEmbeddings(String query){
        Embedding content = embeddingModel.embed(query).content();
        System.out.println(content);
        EmbeddingSearchResult search = embeddingStore.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(content)
                .build());
        return search.matches().stream().map(m->m.toString()).toList();
    }

    @GetMapping("/documents")
    public List<Map<String, Object>> documentList(){
        List<Map<String, Object>> respo = this.jdbcTemplate.query("SELECT * from public.data_vs_v3", new Object[]{}, (rs, rowNum) -> {
            Map<String, Object> map = new HashMap();
            map.put("embedding_id", rs.getObject("embedding_id"));
            map.put("text", rs.getObject("text"));
            map.put("embedding", rs.getObject("embedding"));
            map.put("metadata", rs.getObject("metadata"));
            return map;
        });
        return respo;
    }
}
