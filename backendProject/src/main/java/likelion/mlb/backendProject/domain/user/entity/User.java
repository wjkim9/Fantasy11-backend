package likelion.mlb.backendProject.domain.user.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "provider")  // ex: "google"
    private String provider; // google

    @Column(name = "role")
    private String role = "USER";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(UUID id, String email, String name, String profileImage, String provider, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }
    
}