package likelion.mlb.backendProject.domain.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드 자동 생략
public record ChatReadStateDto(String lastReadMessageId, long unreadCount) {}