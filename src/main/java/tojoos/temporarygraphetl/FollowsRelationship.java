package tojoos.temporarygraphetl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.neo4j.core.schema.*;
import java.time.LocalDateTime;

// Relationship Entity class (additional temporary properties)
@RelationshipProperties
public class FollowsRelationship {

    @JsonIgnore
    @RelationshipId
    private Long id;

    @TargetNode
    private User user;

    @Property
    private LocalDateTime followStartDate;

    @Property
    private LocalDateTime followEndDate;

    public FollowsRelationship() {

    }

    public FollowsRelationship(User user) {
        this.user = user;
        this.followStartDate = LocalDateTime.now();
        this.followEndDate = LocalDateTime.of(9999, 1, 1, 0, 0, 0);
    }

    public FollowsRelationship(User user, LocalDateTime followStartDate) {
        this.user = user;
        this.followStartDate = followStartDate;
        this.followEndDate = LocalDateTime.of(9999, 1, 1, 0, 0, 0);
    }

    public boolean isUserFollowed(LocalDateTime timestamp) {
        return timestamp.isAfter(followStartDate) && timestamp.isBefore(followEndDate);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getFollowStartDate() {
        return followStartDate;
    }

    public void setFollowStartDate(LocalDateTime followStartDate) {
        this.followStartDate = followStartDate;
    }

    public LocalDateTime getFollowEndDate() {
        return followEndDate;
    }

    public void setFollowEndDate(LocalDateTime followEndDate) {
        this.followEndDate = followEndDate;
    }
}
