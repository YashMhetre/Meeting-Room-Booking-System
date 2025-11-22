package com.meetingroom.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Component
public class GlobalExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        
        // Resource Not Found
        if (ex instanceof ResourceNotFoundException) {
            ResourceNotFoundException exception = (ResourceNotFoundException) ex;
            return buildGraphQLError(
                exception.getMessage(),
                ErrorType.NOT_FOUND,
                env,
                exception.getErrorCode()
            );
        }

        // Duplicate Resource
        if (ex instanceof DuplicateResourceException) {
            DuplicateResourceException exception = (DuplicateResourceException) ex;
            return buildGraphQLError(
                exception.getMessage(),
                ErrorType.BAD_REQUEST,
                env,
                exception.getErrorCode()
            );
        }

        // Business Rule Violations
        if (ex instanceof BusinessRuleException) {
            BusinessRuleException exception = (BusinessRuleException) ex;
            return buildGraphQLError(
                exception.getMessage(),
                ErrorType.BAD_REQUEST,
                env,
                exception.getErrorCode(),
                exception.getDetails()
            );
        }

        // Unauthorized Access
        if (ex instanceof UnauthorizedException) {
            UnauthorizedException exception = (UnauthorizedException) ex;
            return buildGraphQLError(
                exception.getMessage(),
                ErrorType.UNAUTHORIZED,
                env,
                exception.getErrorCode()
            );
        }

        // Invalid Input
        if (ex instanceof InvalidInputException) {
            InvalidInputException exception = (InvalidInputException) ex;
            return buildGraphQLError(
                exception.getMessage(),
                ErrorType.BAD_REQUEST,
                env,
                exception.getErrorCode()
            );
        }

        // Spring Security Exceptions
        if (ex instanceof AccessDeniedException) {
            return buildGraphQLError(
                "Access denied",
                ErrorType.FORBIDDEN,
                env,
                ErrorCode.UNAUTHORIZED
            );
        }

        if (ex instanceof BadCredentialsException) {
            return buildGraphQLError(
                "Invalid credentials",
                ErrorType.UNAUTHORIZED,
                env,
                ErrorCode.INVALID_CREDENTIALS
            );
        }

        // Date/Time Parsing Errors
        if (ex instanceof DateTimeParseException) {
            return buildGraphQLError(
                "Invalid date or time format: " + ex.getMessage(),
                ErrorType.BAD_REQUEST,
                env,
                ErrorCode.INVALID_DATE_FORMAT
            );
        }

        // Generic RuntimeException (fallback for old code)
        if (ex instanceof RuntimeException) {
            return buildGraphQLError(
                ex.getMessage(),
                ErrorType.INTERNAL_ERROR,
                env,
                ErrorCode.INTERNAL_SERVER_ERROR
            );
        }

        // Default fallback
        return buildGraphQLError(
            "An unexpected error occurred",
            ErrorType.INTERNAL_ERROR,
            env,
            ErrorCode.INTERNAL_SERVER_ERROR
        );
    }

    private GraphQLError buildGraphQLError(
            String message,
            ErrorType errorType,
            DataFetchingEnvironment env,
            ErrorCode errorCode
    ) {
        return buildGraphQLError(message, errorType, env, errorCode, null);
    }

    private GraphQLError buildGraphQLError(
            String message,
            ErrorType errorType,
            DataFetchingEnvironment env,
            ErrorCode errorCode,
            Map additionalDetails
    ) {
        Map extensions = new HashMap<>();
        extensions.put("errorCode", errorCode.getCode());
        extensions.put("timestamp", java.time.LocalDateTime.now().toString());
        
        if (additionalDetails != null && !additionalDetails.isEmpty()) {
            extensions.put("details", additionalDetails);
        }

        return GraphqlErrorBuilder.newError()
                .errorType(errorType)
                .message(message)
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .extensions(extensions)
                .build();
    }
}