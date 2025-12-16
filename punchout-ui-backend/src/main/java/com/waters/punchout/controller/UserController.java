package com.waters.punchout.controller;

import com.waters.punchout.mongo.entity.UserDocument;
import com.waters.punchout.mongo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    /**
     * Get all users
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserDocument>> getAllUsers() {
        log.info("GET /api/users - Fetching all users");
        List<UserDocument> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDocument> getUserById(@PathVariable String id) {
        log.info("GET /api/users/{} - Fetching user by ID", id);
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get user by userId
     * GET /api/users/userId/{userId}
     */
    @GetMapping("/userId/{userId}")
    public ResponseEntity<UserDocument> getUserByUserId(@PathVariable String userId) {
        log.info("GET /api/users/userId/{} - Fetching user by userId", userId);
        return userService.getUserByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get user by username
     * GET /api/users/username/{username}
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDocument> getUserByUsername(@PathVariable String username) {
        log.info("GET /api/users/username/{} - Fetching user by username", username);
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get users by role
     * GET /api/users/role/{role}
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDocument>> getUsersByRole(@PathVariable String role) {
        log.info("GET /api/users/role/{} - Fetching users by role", role);
        List<UserDocument> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get users by status
     * GET /api/users/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<UserDocument>> getUsersByStatus(@PathVariable String status) {
        log.info("GET /api/users/status/{} - Fetching users by status", status);
        List<UserDocument> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get users by department
     * GET /api/users/department/{department}
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<UserDocument>> getUsersByDepartment(@PathVariable String department) {
        log.info("GET /api/users/department/{} - Fetching users by department", department);
        List<UserDocument> users = userService.getUsersByDepartment(department);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Search users
     * GET /api/users/search?q={searchTerm}
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDocument>> searchUsers(@RequestParam("q") String searchTerm) {
        log.info("GET /api/users/search?q={} - Searching users", searchTerm);
        List<UserDocument> users = userService.searchUsers(searchTerm);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Create new user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserDocument> createUser(@RequestBody UserDocument user) {
        log.info("POST /api/users - Creating new user: {}", user.getUsername());
        try {
            UserDocument createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            log.error("Failed to create user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update user
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDocument> updateUser(
            @PathVariable String id,
            @RequestBody UserDocument user) {
        log.info("PUT /api/users/{} - Updating user", id);
        try {
            UserDocument updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            log.error("Failed to update user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Update user status
     * PATCH /api/users/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserDocument> updateUserStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {
        log.info("PATCH /api/users/{}/status - Updating user status", id);
        try {
            String status = statusUpdate.get("status");
            UserDocument updatedUser = userService.updateUserStatus(id, status);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            log.error("Failed to update user status: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Delete user
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        log.info("DELETE /api/users/{} - Deleting user", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get user statistics
     * GET /api/users/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        log.info("GET /api/users/statistics - Fetching user statistics");
        Map<String, Object> stats = userService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Export users to CSV
     * GET /api/users/export
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department) {
        log.info("GET /api/users/export - role={}, status={}, department={}", role, status, department);
        
        String csvContent = userService.generateExportCsv(role, status, department);
        byte[] csvBytes = csvContent.getBytes();
        
        String filename = "users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("no-cache, no-store, must-revalidate");
        
        log.info("Exporting users as CSV");
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
    
    /**
     * Bulk status update
     * POST /api/users/bulk/status
     */
    @PostMapping("/bulk/status")
    public ResponseEntity<List<UserDocument>> bulkUpdateStatus(@RequestBody Map<String, Object> request) {
        log.info("POST /api/users/bulk/status - Bulk updating user status");
        try {
            @SuppressWarnings("unchecked")
            List<String> userIds = (List<String>) request.get("userIds");
            String status = (String) request.get("status");
            
            if (userIds == null || userIds.isEmpty() || status == null) {
                return ResponseEntity.badRequest().build();
            }
            
            List<UserDocument> updatedUsers = userService.bulkUpdateStatus(userIds, status);
            return ResponseEntity.ok(updatedUsers);
        } catch (Exception e) {
            log.error("Failed to bulk update status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Reset password
     * POST /api/users/{id}/reset-password
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable String id) {
        log.info("POST /api/users/{}/reset-password - Resetting password", id);
        try {
            userService.resetPassword(id);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully. User will receive a new temporary password."));
        } catch (IllegalArgumentException e) {
            log.error("Failed to reset password: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all departments
     * GET /api/users/departments
     */
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAllDepartments() {
        log.info("GET /api/users/departments - Fetching all departments");
        List<String> departments = userService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }
    
    /**
     * Toggle 2FA
     * PATCH /api/users/{id}/two-factor
     */
    @PatchMapping("/{id}/two-factor")
    public ResponseEntity<UserDocument> toggleTwoFactor(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request) {
        log.info("PATCH /api/users/{}/two-factor - Toggling 2FA", id);
        try {
            Boolean enabled = request.get("enabled");
            if (enabled == null) {
                return ResponseEntity.badRequest().build();
            }
            UserDocument updatedUser = userService.toggleTwoFactor(id, enabled);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            log.error("Failed to toggle 2FA: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
