package com.seolstudy.backend.global.payload.status;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseStatus {
    // 가장 일반적인 응답
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 에러, 관리자에게 문의 바랍니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON_400","잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON_401","인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "금지된 요청입니다."),

    // Auth Error
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_4011", "아이디 또는 비밀번호가 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_4012", "유효하지 않은 리프레시 토큰입니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_4013", "액세스 토큰이 만료되었습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_4014", "리프레시 토큰이 만료되었습니다."),
    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, "AUTH_4091", "이미 사용중인 아이디입니다."),

    // Mentoring Error
    MENTORING_INVALID_MENTOR_ROLE(HttpStatus.FORBIDDEN, "MENTORING_4031", "멘토 역할을 가진 사용자만 등록할 수 있습니다."),
    MENTORING_INVALID_MENTEE_ROLE(HttpStatus.FORBIDDEN, "MENTORING_4032", "멘티 역할을 가진 사용자만 등록할 수 있습니다."),
    MENTORING_MENTEE_ALREADY_REGISTERED(HttpStatus.CONFLICT, "MENTORING_4091", "이미 등록된 멘티입니다."),
    MENTORING_ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "MENTORING_4001", "이미 비활성화된 관계입니다."),
    MENTORING_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTORING_4041", "멘토링 관계를 찾을 수 없습니다."),

    // Feedback Error
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "FEEDBACK_4041", "피드백을 찾을 수 없습니다."),
    INVALID_FEEDBACK_DATE(HttpStatus.BAD_REQUEST, "FEEDBACK_4001", "유효하지 않은 피드백 날짜 형식입니다."),
    FEEDBACK_ALREADY_EXISTS(HttpStatus.CONFLICT, "FEEDBACK_4091", "이미 피드백이 존재합니다."),

    // Member Error
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER_4001", "사용자가 없습니다."),
    NICKNAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "MEMBER_4002", "닉네임은 필수 입니다."),

    // Task Error
    TASK_NOT_FOUND(HttpStatus.BAD_REQUEST, "TASK_4001", "과제를 찾을 수 없습니다."),
    INVALID_TASK_DATE(HttpStatus.BAD_REQUEST, "TASK_4002", "유효하지 않은 날짜 형식입니다."),

    // Planner Error
    PLANNER_NOT_FOUND(HttpStatus.BAD_REQUEST, "PLANNER_4001", "플래너를 찾을 수 없습니다."),
    PLANNER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "PLANNER_4002", "해당 날짜의 플래너가 이미 존재합니다."),
    INVALID_PLANNER_DATE(HttpStatus.BAD_REQUEST, "PLANNER_4003", "유효하지 않은 날짜 형식입니다."),
    PLANNER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PLANNER_4004", "플래너에 접근할 수 없습니다."),
    INVALID_PLANNER_IMAGE(HttpStatus.BAD_REQUEST, "PLANNER_4005", "플래너 이미지는 필수입니다."),
    INVALID_PLANNER_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "PLANNER_4006", "이미지 파일만 업로드할 수 있습니다."),
    PLANNER_IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "PLANNER_4007", "이미지 파일 용량이 너무 큽니다."),
    PLANNER_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PLANNER_5001", "플래너 이미지 업로드에 실패했습니다."),
    INVALID_MENTEE_ROLE(HttpStatus.FORBIDDEN, "PLANNER_4008", "멘티만 플래너 등록이 가능합니다."),
    INVALID_MENTOR_ROLE(HttpStatus.FORBIDDEN, "PLANNER_4009", "멘토만 코멘트 등록이 가능합니다."),

    // Subject Error
    SUBJECT_NOT_FOUND(HttpStatus.BAD_REQUEST, "SUBJECT_4001", "과목을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDto getReasonHttpStatus() {
        return ReasonDto.builder()
                .httpStatus(httpStatus)
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }
}
