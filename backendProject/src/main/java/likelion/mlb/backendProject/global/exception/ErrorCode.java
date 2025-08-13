package likelion.mlb.backendProject.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor

public enum ErrorCode {

  /* 400 - Bad Request */
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "입력 값이 올바르지 않습니다."),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "입력값 유효성 검사에 실패했습니다."),

  /* 401 - Unauthorized */
  AUTHENTICATION_FAILED(HttpStatus.BAD_REQUEST, "AUTHENTICATION_FAILED", "인증에 실패했습니다."),
  EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_ACCESS_TOKEN", "Access Token이 만료되었습니다. 토큰을 재발급해주세요"),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다."),

  /* 403 - Forbidden */
  FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근할 수 있는 권한이 없습니다."),
  EXPIRED_OR_PREVIOUS_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "EXPIRED_OR_PREVIOUS_REFRESH_TOKEN",
      "만료되었거나 이전에 발급된 리프레쉬 토큰 입니다."),

  /* 404 - Not Found */
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "해당 사용자를 찾을 수 없습니다."),
  ROUND_NOT_FOUND(HttpStatus.NOT_FOUND, "ROUND_NOT_FOUND", "시작 예정 라운드를 찾을 수 없습니다."),
  ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ASSIGNMENT_NOT_FOUND", "배정된 드래프트가 없습니다."),

  /* 409 - CONFLICT */
  ASSIGNMENT_CONFLICT(HttpStatus.CONFLICT, "ASSIGNMENT_CONFLICT", "중복 배정이 감지되었습니다."),
  CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
  DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "DRAFT_NOT_FOUND", "드래프트방을 찾을 수 없습니다."),
  PARTICIPANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PARTICIPANT_NOT_FOUND", "참가자를 찾을 수 없습니다."),

  /* 500 - Internal Server Error */
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버에 오류가 발생했습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;

}
