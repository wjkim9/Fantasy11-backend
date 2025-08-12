package likelion.mlb.backendProject.domain.user.entity;

import jakarta.persistence.*;
import likelion.mlb.backendProject.global.staticdata.UserRole;
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
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "name", nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private UserRole role = UserRole.USER;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Builder
  public User(UUID id, String email, String name, UserRole role) {
    this.id = id;
    this.email = email;
    this.name = name;
    this.role = role != null ? role : UserRole.USER;
    this.createdAt = LocalDateTime.now();
  }

}