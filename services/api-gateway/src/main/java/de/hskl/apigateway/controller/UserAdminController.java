package de.hskl.apigateway.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@Profile("local") // only local
public class UserAdminController {
    @PostMapping("/{uid}/role")
    public ResponseEntity<String> setUserRole(@PathVariable String uid, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || !List.of("SENDER", "STATION", "CUSTOMER").contains(role)) {
            return ResponseEntity.badRequest().body("Invalid role");
        }

        try {
            FirebaseAuth.getInstance().setCustomUserClaims(uid, Map.of("role", role));
            return ResponseEntity.ok("Role set to " + role);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
