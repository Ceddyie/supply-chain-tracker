package de.hskl.shipmentservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity handleNotFound(ShipmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(TrackingNotFoundException.class)
    public ResponseEntity handleTrackingNotFound(TrackingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity handleAccess(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }

    public static class ShipmentNotFoundException extends RuntimeException {
        public ShipmentNotFoundException(UUID id) {
            super("Shipment with id " + id + " not found");
        }
    }

    public static class TrackingNotFoundException extends RuntimeException {
        public TrackingNotFoundException(String trackingId) {
            super("Shipment with Tracking-ID " + trackingId + " not found");
        }
    }

    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String msg) {
            super(msg);
        }
    }
}
