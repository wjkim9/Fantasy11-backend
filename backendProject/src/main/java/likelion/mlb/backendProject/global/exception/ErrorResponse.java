package likelion.mlb.backendProject.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private int code;
    private String message;
    private String detail;

}
