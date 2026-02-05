package com.seolstudy.backend.global.exception;

import com.seolstudy.backend.global.payload.status.BaseStatus;

public class TmpException extends GeneralException {
    public TmpException(BaseStatus status) {
        super(status);
    }
}
