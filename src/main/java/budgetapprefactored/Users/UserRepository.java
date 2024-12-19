package budgetapprefactored.Users;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {

    Optional<User> findUserByUserId(String userId);
    Optional<User> findUserByEmail(String email);
    boolean deleteUserByUserId(String userId);
}
