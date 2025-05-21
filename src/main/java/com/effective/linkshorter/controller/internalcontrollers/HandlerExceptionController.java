package com.effective.linkshorter.controller.internalcontrollers;

import com.effective.linkshorter.others.ApiErrorResponse;
import com.effective.linkshorter.others.Violation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

import static com.effective.linkshorter.others.ConstantsClass.LINE_FEED;

/**
 * <pre>
 *     Контроллер, обрабатывающий все эксепшены, котоыре могут быть выброшены в процессе работы приложения
 * </pre>
 */
@ControllerAdvice
@Slf4j
public class HandlerExceptionController {

    /**
     * Общий обработчик для всех остальных исключений
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiErrorResponse> handleAllExceptions(Exception e, HttpServletRequest request) {
        log.error("Возникла непредвиденная ошибка " + e.getClass() + LINE_FEED + e.getMessage() + LINE_FEED +
                e);

        ApiErrorResponse apiErrorResponse = buildApiErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ApiErrorResponse> handleAllRunTimeException(RuntimeException e, HttpServletRequest request) {
        log.error("Возникла непредвиденная ошибка " + e.getClass() + LINE_FEED + e.getMessage() + LINE_FEED +
                e);

        ApiErrorResponse apiErrorResponse = buildApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage(),
                request.getRequestURI(),
                null
        );

        return new ResponseEntity<>(apiErrorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ApiErrorResponse buildApiErrorResponse(HttpStatus status, String message, String path, List<Violation> violations) {
        return ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .violations(violations)
                .build();
    }
}
