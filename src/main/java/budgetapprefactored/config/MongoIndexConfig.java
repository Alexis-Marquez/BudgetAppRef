package budgetapprefactored.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

    public MongoIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initIndexes() {
        // Ensure unique index on userId and monthYear for the "budgets" collection
        IndexOperations indexOps = mongoTemplate.indexOps("budgets");
        indexOps.ensureIndex(new Index()
                .on("userId", Sort.Direction.ASC)
                .on("monthYear", Sort.Direction.ASC));
    }
}
