package com.lara.denunciaservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DenunciaNoEncontradaException.class)
    public ProblemDetail noEncontrada(DenunciaNoEncontradaException exception) {
        return problema(HttpStatus.NOT_FOUND, "Denuncia no encontrada", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException exception) {
        ProblemDetail problem = problema(HttpStatus.BAD_REQUEST, "Solicitud inválida", "Revise los campos del formulario");
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumento(IllegalArgumentException exception) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud inválida", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail tipo(MethodArgumentTypeMismatchException exception) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud inválida", "Formato incorrecto para el parámetro " + exception.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail json(HttpMessageNotReadableException exception) {
        return problema(HttpStatus.BAD_REQUEST, "Solicitud inválida", "Envíe JSON válido y fecha con formato yyyy-MM-dd'T'HH:mm:ss");
    }

    private ProblemDetail problema(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
