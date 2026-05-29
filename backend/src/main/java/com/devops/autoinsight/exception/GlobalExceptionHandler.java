package com.devops.autoinsight.exception;

import com.devops.autoinsight.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**

* Global centralized exception handler for AutoInsight REST APIs.
*
* Responsibilities:
* * Standardized API error responses
* * Validation handling
* * File upload exception handling
* * Logging and debugging support
* * Production-grade error management
    */
    @Slf4j
    @RestControllerAdvice
    public class GlobalExceptionHandler {

  // ─────────────────────────────────────────────────────────────────────────
  // 404 - Resource Not Found
  // ─────────────────────────────────────────────────────────────────────────

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
  ResourceNotFoundException ex,
  HttpServletRequest request) {

  
   log.warn("Resource not found -> resource: {}, id: {}, path: {}",
           ex.getResourceName(),
           ex.getResourceId(),
           request.getRequestURI());

   return buildErrorResponse(
           HttpStatus.NOT_FOUND,
           ex.getMessage(),
           "RESOURCE_NOT_FOUND"
   );
  

  }

  // ─────────────────────────────────────────────────────────────────────────
  // 400 - Invalid File Upload
  // ─────────────────────────────────────────────────────────────────────────

  @ExceptionHandler(InvalidFileException.class)
  public ResponseEntity<ApiResponse<Void>> handleInvalidFile(
  InvalidFileException ex,
  HttpServletRequest request) {

  
   log.warn("Invalid file upload at {} -> {}",
           request.getRequestURI(),
           ex.getMessage());

   return buildErrorResponse(
           HttpStatus.BAD_REQUEST,
           ex.getMessage(),
           "INVALID_FILE"
   );
  

  }

  // ─────────────────────────────────────────────────────────────────────────
  // 400 - Illegal Arguments
  // ─────────────────────────────────────────────────────────────────────────

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
  IllegalArgumentException ex,
  HttpServletRequest request) {

  
   log.warn("Illegal argument at {} -> {}",
           request.getRequestURI(),
           ex.getMessage());

   return buildErrorResponse(
           HttpStatus.BAD_REQUEST,
           ex.getMessage(),
           "INVALID_REQUEST"
   );
  

  }

  // ─────────────────────────────────────────────────────────────────────────
  // 400 - DTO Validation Errors
  // ─────────────────────────────────────────────────────────────────────────

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
  MethodArgumentNotValidException ex,
  HttpServletRequest request) {

  
   Map<String, String> validationErrors = new HashMap<>();

   ex.getBindingResult()
           .getAllErrors()
           .forEach(error -> {

               String fieldName = ((FieldError) error).getField();
               String errorMessage = error.getDefaultMessage();

               validationErrors.put(fieldName, errorMessage);
           });

   log.warn("Validation failed at {} -> {}",
           request.getRequestURI(),
           validationErrors);

   ApiResponse<Map<String, String>> response =
           ApiResponse.<Map<String, String>>builder()
                   .success(false)
                   .message("Validation failed")
                   .errorCode("VALIDATION_ERROR")
                   .timestamp(LocalDateTime.now())
                   .data(validationErrors)
                   .build();

   return ResponseEntity
           .status(HttpStatus.BAD_REQUEST)
           .body(response);
  

  }

  // ─────────────────────────────────────────────────────────────────────────
  // 413 - File Too Large
  // ─────────────────────────────────────────────────────────────────────────

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceeded(
  MaxUploadSizeExceededException ex,
  HttpServletRequest request) {

  
   log.warn("File upload exceeded size limit at {}",
           request.getRequestURI());

   return buildErrorResponse(
           HttpStatus.PAYLOAD_TOO_LARGE,
           "File size exceeds the maximum allowed upload size of 10MB",
           "FILE_TOO_LARGE"
   );
  

  }

  // ─────────────────────────────────────────────────────────────────────────
  // 400 - Multipart Request Errors
  // ─────────────────────────────────────────────────────────────────────────

  @ExceptionHandler(MultipartException.class)
  public ResponseEntity<ApiResponse<Void>> handleMultipartException(
  MultipartException ex,
  HttpServletRequest request) {

  
   log.warn("Multipart request error at {} -> {}",
           request.getRequestURI(),
           ex.getMessage());

   return buildErrorResponse(
           HttpStatus.BAD_REQUEST,
           "Invalid multipart request",
           "MULTIPART_ERROR"
   );
  

  }

  // ─────────────────────────────────────────────────────────────────────────
  // 422 - Log Processing Errors
  // ─────────────────────────────────────────────────────────────────────────

  @ExceptionHandler(LogProcessingException.class)
  public ResponseEntity<ApiResponse<Void>> handleLogProcessingException(
  LogProcessingException ex,
  HttpServletRequest request) {


   log.error("Log processing failed at {}",
           request.getRequestURI(),
           ex);

   return buildErrorResponse(
           HttpStatus.UNPROCESSABLE_ENTITY,
           ex.getMessage(),
           "LOG_PROCESSING_ERROR"
   );


  }

  // ─────────────────────────────────────────────────────────────────────────
  // 500 - Unexpected Errors
  // ─────────────────────────────────────────────────────────────────────────

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(
  Exception ex,
  HttpServletRequest request) {

  
   log.error("Unhandled exception occurred at {}",
           request.getRequestURI(),
           ex);

   return buildErrorResponse(
           HttpStatus.INTERNAL_SERVER_ERROR,
           "An unexpected error occurred. Please try again or contact support.",
           "INTERNAL_SERVER_ERROR"
   );
  

  }

  // ─────────────────────────────────────────────────────────────────────────
  // Helper Method
  // ─────────────────────────────────────────────────────────────────────────

  private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
  HttpStatus status,
  String message,
  String errorCode) {

  
   ApiResponse<Void> response = ApiResponse.<Void>builder()
           .success(false)
           .message(message)
           .errorCode(errorCode)
           .timestamp(LocalDateTime.now())
           .build();

   return ResponseEntity
           .status(status)
           .body(response);
  

  }
  }
