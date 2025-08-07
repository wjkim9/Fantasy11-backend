package likelion.mlb.backendProject.global.security.dto;

import java.util.Collection;
import java.util.List;
import likelion.mlb.backendProject.domain.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

  private final User user;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER")); // 필요 시 DB에서 꺼내도 됨
  }

  @Override
  public String getUsername() {
    return user.getEmail(); // 또는 id
  }

  @Override
  public String getPassword() {
    return null; // 소셜 로그인이면 null 반환 가능
  }

  @Override public boolean isAccountNonExpired()     { return true; }
  @Override public boolean isAccountNonLocked()      { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled()               { return true; }
}

