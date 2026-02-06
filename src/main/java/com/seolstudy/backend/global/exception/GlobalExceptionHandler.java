package com.seolstudy.backend.global.exception;

import com.seolstudy.backend.global.payload.CommonResponse;
import com.seolstudy.backend.global.payload.status.ErrorStatus;
import com.seolstudy.backend.global.payload.status.ReasonDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSize(MaxUploadSizeExceededException e, WebRequest request) {
        CommonResponse<Object> body = CommonResponse.onFailure(
                ErrorStatus.PLANNER_IMAGE_TOO_LARGE.getCode(),
                ErrorStatus.PLANNER_IMAGE_TOO_LARGE.getMessage(),
                null
        );

        return ResponseEntity.status(ErrorStatus.PLANNER_IMAGE_TOO_LARGE.getHttpStatus()).body(body);
    }

    @ExceptionHandler(value = MultipartException.class)
    public ResponseEntity<Object> handleMultipart(MultipartException e, WebRequest request) {
        CommonResponse<Object> body = CommonResponse.onFailure(
                ErrorStatus.INVALID_PLANNER_IMAGE.getCode(),
                ErrorStatus.INVALID_PLANNER_IMAGE.getMessage(),
                null
        );

        return ResponseEntity.status(ErrorStatus.INVALID_PLANNER_IMAGE.getHttpStatus()).body(body);
    }


    @ExceptionHandler(value = ConstraintViolationException.class)
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "ConstraintViolationException이 ConstraintViolation을 포함하지 않습니다."));

        ErrorStatus errorStatus = ErrorStatus.BAD_REQUEST;
        for (ErrorStatus value : ErrorStatus.values()) {
            if (value.name().equals(errorMessage)) {
                errorStatus = value;
                break;
            }
        }

        CommonResponse<Object> body = CommonResponse.onFailure(errorStatus.getCode(),
                errorStatus.getMessage(),
                null);

        return ResponseEntity.status(errorStatus.getHttpStatus()).body(body);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                    errors.merge(fieldName, errorMessage,
                            (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
                });

        CommonResponse<Object> body = CommonResponse.onFailure(ErrorStatus.BAD_REQUEST.getCode(),
                ErrorStatus.BAD_REQUEST.getMessage(),
                errors);

        return ResponseEntity.status(ErrorStatus.BAD_REQUEST.getHttpStatus()).body(body);
    }

    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity<Object> handleGeneral(GeneralException generalException, HttpServletRequest request) {
        ReasonDto errorReasonHttpStatus = generalException.getErrorReasonHttpStatus();

        CommonResponse<Object> body = CommonResponse.onFailure(errorReasonHttpStatus.getCode(),
                errorReasonHttpStatus.getMessage(),
                null);

        return ResponseEntity.status(errorReasonHttpStatus.getHttpStatus()).body(body);
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleOthers(Exception e, WebRequest request) {
        log.error("Unhandled exception", e);
        CommonResponse<Object> body = CommonResponse.onFailure(ErrorStatus.INTERNAL_SERVER_ERROR.getCode(),
                ErrorStatus.INTERNAL_SERVER_ERROR.getMessage(),
                null);

        return ResponseEntity.status(ErrorStatus.INTERNAL_SERVER_ERROR.getHttpStatus()).body(body);
    }

    // 인증 관련 예외 처리
    @ExceptionHandler({ BadCredentialsException.class, UsernameNotFoundException.class })
    public ResponseEntity<Object> handleAuth(AuthenticationException e, WebRequest request) {
        CommonResponse<Object> body = CommonResponse.onFailure(
                ErrorStatus.UNAUTHORIZED.getCode(),     // 예: AUTH_401 같은 코드
                "이메일 또는 비밀번호가 올바르지 않습니다.",
                null
        );
        return ResponseEntity.status(ErrorStatus.UNAUTHORIZED.getHttpStatus()).body(body);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AuthorizationDeniedException e, WebRequest request) {
        CommonResponse<Object> body = CommonResponse.onFailure(
                ErrorStatus.FORBIDDEN.getCode(),
                ErrorStatus.FORBIDDEN.getMessage(),
                null
        );
        return ResponseEntity.status(ErrorStatus.FORBIDDEN.getHttpStatus()).body(body);
    }
}
