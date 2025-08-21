package likelion.mlb.backendProject.domain.user.dto;

import java.util.UUID;

public record UserMeResponse(
    UUID id,
    String email,
    String name
) {}