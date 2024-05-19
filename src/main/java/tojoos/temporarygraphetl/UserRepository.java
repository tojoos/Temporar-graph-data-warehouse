package tojoos.temporarygraphetl;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends Neo4jRepository<User, Long> {

    @Query("""
        MATCH (user)
        OPTIONAL MATCH (user)-[r:FOLLOWS]->(users2)
        RETURN DISTINCT user, COLLECT(r) as follows_rels, collect(DISTINCT users2) as followed_users
        """)
    List<User> findAllEager();

    @Query("""
        WITH $timestamp AS timestamp
        MATCH (user)
        WHERE user.creationTimestamp <= localdatetime(timestamp)
        RETURN user
        """)
    List<User> findAllNoRelationsForCurrentTimestamp(@Param("timestamp") LocalDateTime timestamp);

    @Query("""
        WITH $timestamp AS timestamp
        MATCH (user:User)
        WHERE user.creationTimestamp <= localdatetime(timestamp)
        RETURN user, COLLECT {
            MATCH (user)-[r:FOLLOWS]->(user2:User)
            WHERE r.followStartDate <= localdatetime(timestamp) AND r.followEndDate >= localdatetime(timestamp)
            RETURN r
        },
        COLLECT {
            MATCH (user)-[r:FOLLOWS]->(user2:User)
            WHERE r.followStartDate <= localdatetime(timestamp) AND r.followEndDate >= localdatetime(timestamp)
            RETURN user2
        }
        """)
    List<User> findAllForCurrentTimestamp(@Param("timestamp") LocalDateTime timestamp);

    @Query("""
        WITH $timestamp AS timestamp
        MATCH (user:User)
        WHERE user.creationTimestamp <= localdatetime(timestamp) AND user.id = $id
        RETURN user, COLLECT {
            MATCH (user)-[r:FOLLOWS]->(user2:User)
            WHERE r.followStartDate <= localdatetime(timestamp) AND r.followEndDate >= localdatetime(timestamp)
            RETURN r
        },
        COLLECT {
            MATCH (user)-[r:FOLLOWS]->(user2:User)
            WHERE r.followStartDate <= localdatetime(timestamp) AND r.followEndDate >= localdatetime(timestamp)
            RETURN user2
        }
        """)
    Optional<User> findByIdForCurrentTimestamp(@Param("id") Long id, @Param("timestamp") LocalDateTime timestamp);
}
