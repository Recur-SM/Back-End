package com.seolstudy.backend.global.exception;

import com.seolstudy.backend.global.payload.status.BaseStatus;
import com.seolstudy.backend.global.payload.status.ReasonDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private final BaseStatus status;

    public ReasonDto getErrorReasonHttpStatus(){
        return this.status.getReasonHttpStatus();
    }
}
