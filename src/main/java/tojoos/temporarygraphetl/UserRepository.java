package tojoos.temporarygraphetl;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import java.util.Optional;

public interface UserRepository extends Neo4jRepository<User, Long> {
    Optional<User> findByNickname(String nickname);
}
