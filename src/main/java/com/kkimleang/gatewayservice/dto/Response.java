package com.kkimleang.gatewayservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response implements Serializable {
    private Status status;
    private int statusCode;
    private UserResponse payload;
    private Object errors;
    private boolean success = false;
    private Instant timestamp = Instant.now();
    private Object metadata;

    public static  Response badRequest() {
        Response response = new Response();
        response.setStatus(Status.BAD_REQUEST);
        response.setStatusCode(400);
        return response;
    }

    public static  Response ok() {
        Response response = new Response();
        response.setStatus(Status.OK);
        response.setStatusCode(200);
        response.setSuccess(true);
        return response;
    }

    public static  Response created() {
        Response response = new Response();
        response.setStatus(Status.CREATED);
        response.setSuccess(true);
        return response;
    }

    public static  Response unauthorized() {
        Response response = new Response();
        response.setStatus(Status.UNAUTHORIZED);
        return response;
    }

    public static  Response wrongCredentials() {
        Response response = new Response();
        response.setStatus(Status.WRONG_CREDENTIALS);
        return response;
    }

    public static  Response accessDenied() {
        Response response = new Response();
        response.setStatus(Status.ACCESS_DENIED);
        return response;
    }

    public static  Response exception() {
        Response response = new Response();
        response.setStatus(Status.EXCEPTION);
        return response;
    }

    public static  Response invalidToken() {
        Response response = new Response();
        response.setStatus(Status.VALIDATION_EXCEPTION);
        return response;
    }

    public static  Response notFound() {
        Response response = new Response();
        response.setStatus(Status.NOT_FOUND);
        return response;
    }

    public static  Response generationNotAvailable() {
        Response response = new Response();
        response.setStatus(Status.GENERATION_NOT_AVAILABLE);
        return response;
    }

    public static  Response duplicateEntity() {
        Response response = new Response();
        response.setStatus(Status.DUPLICATE_ENTITY);
        return response;
    }

    public void addErrorMsgToResponse(String errorMsg, Exception ex) {
        ResponseError error = new ResponseError()
                .setDetails(errorMsg)
                .setMessage(ex.getMessage())
                .setTimestamp(new Date());
        setErrors(error);
    }

    public enum Status {
        GENERATION_NOT_AVAILABLE, OK, BAD_REQUEST, UNAUTHORIZED, VALIDATION_EXCEPTION, EXCEPTION, WRONG_CREDENTIALS, ACCESS_DENIED, NOT_FOUND, CREATED, DUPLICATE_ENTITY
    }

    @Getter
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageMetadata {
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final int number;

        public PageMetadata(int size, long totalElements, int totalPages, int number) {
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.number = number;
        }
    }

}