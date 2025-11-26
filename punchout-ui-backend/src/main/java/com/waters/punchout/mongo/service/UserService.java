package com.waters.punchout.mongo.service;

import com.waters.punchout.mongo.entity.UserDocument;
import com.waters.punchout.mongo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    /**
     * Get all users
     */
    public List<UserDocument> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll();
    }
    
    /**
     * Get user by ID
     */
    public Optional<UserDocument> getUserById(String id) {
        log.debug("Fetching user by id: {}", id);
        return userRepository.findById(id);
    }
    
    /**
     * Get user by userId
     */
    public Optional<UserDocument> getUserByUserId(String userId) {
        log.debug("Fetching user by userId: {}", userId);
        return userRepository.findByUserId(userId);
    }
    
    /**
     * Get user by username
     */
    public Optional<UserDocument> getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username);
    }
    
    /**
     * Get user by email
     */
    public Optional<UserDocument> getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    /**
     * Get users by role
     */
    public List<UserDocument> getUsersByRole(String role) {
        log.debug("Fetching users by role: {}", role);
        return userRepository.findByRole(role);
    }
    
    /**
     * Get users by status
     */
    public List<UserDocument> getUsersByStatus(String status) {
        log.debug("Fetching users by status: {}", status);
        return userRepository.findByStatus(status);
    }
    
    /**
     * Get users by department
     */
    public List<UserDocument> getUsersByDepartment(String department) {
        log.debug("Fetching users by department: {}", department);
        return userRepository.findByDepartment(department);
    }
    
    /**
     * Search users by term (username, email, first name, last name)
     */
    public List<UserDocument> searchUsers(String searchTerm) {
        log.debug("Searching users with term: {}", searchTerm);
        return userRepository.searchUsers(searchTerm);
    }
    
    /**
     * Create new user
     */
    public UserDocument createUser(UserDocument user) {
        log.info("Creating new user: {}", user.getUsername());
        
        // Validate unique constraints
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }
        
        // Set timestamps
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Set default status if not provided
        if (user.getStatus() == null) {
            user.setStatus("PENDING");
        }
        
        // Set default preferences if not provided
        if (user.getPreferences() == null) {
            user.setPreferences(getDefaultPreferences());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Update existing user
     */
    public UserDocument updateUser(String id, UserDocument updatedUser) {
        log.info("Updating user: {}", id);
        
        return userRepository.findById(id)
                .map(existingUser -> {
                    // Update fields
                    if (updatedUser.getEmail() != null) {
                        existingUser.setEmail(updatedUser.getEmail());
                    }
                    if (updatedUser.getFirstName() != null) {
                        existingUser.setFirstName(updatedUser.getFirstName());
                    }
                    if (updatedUser.getLastName() != null) {
                        existingUser.setLastName(updatedUser.getLastName());
                    }
                    if (updatedUser.getRole() != null) {
                        existingUser.setRole(updatedUser.getRole());
                    }
                    if (updatedUser.getStatus() != null) {
                        existingUser.setStatus(updatedUser.getStatus());
                    }
                    if (updatedUser.getDepartment() != null) {
                        existingUser.setDepartment(updatedUser.getDepartment());
                    }
                    if (updatedUser.getPhoneNumber() != null) {
                        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
                    }
                    if (updatedUser.getJobTitle() != null) {
                        existingUser.setJobTitle(updatedUser.getJobTitle());
                    }
                    if (updatedUser.getPermissions() != null) {
                        existingUser.setPermissions(updatedUser.getPermissions());
                    }
                    if (updatedUser.getPreferences() != null) {
                        existingUser.setPreferences(updatedUser.getPreferences());
                    }
                    if (updatedUser.getIsTwoFactorEnabled() != null) {
                        existingUser.setIsTwoFactorEnabled(updatedUser.getIsTwoFactorEnabled());
                    }
                    
                    existingUser.setUpdatedAt(LocalDateTime.now());
                    
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }
    
    /**
     * Delete user
     */
    public void deleteUser(String id) {
        log.info("Deleting user: {}", id);
        userRepository.deleteById(id);
    }
    
    /**
     * Update user status
     */
    public UserDocument updateUserStatus(String id, String status) {
        log.info("Updating user status: {} to {}", id, status);
        
        return userRepository.findById(id)
                .map(user -> {
                    user.setStatus(status);
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }
    
    /**
     * Record user login
     */
    public void recordLogin(String userId) {
        log.debug("Recording login for user: {}", userId);
        
        userRepository.findByUserId(userId).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }
    
    /**
     * Get user statistics
     */
    public Map<String, Object> getUserStatistics() {
        log.debug("Generating user statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByStatus("ACTIVE"));
        stats.put("inactiveUsers", userRepository.countByStatus("INACTIVE"));
        stats.put("pendingUsers", userRepository.countByStatus("PENDING"));
        
        stats.put("adminCount", userRepository.countByRole("ADMIN"));
        stats.put("managerCount", userRepository.countByRole("MANAGER"));
        stats.put("developerCount", userRepository.countByRole("DEVELOPER"));
        stats.put("userCount", userRepository.countByRole("USER"));
        stats.put("viewerCount", userRepository.countByRole("VIEWER"));
        
        return stats;
    }
    
    /**
     * Get default user preferences
     */
    private Map<String, Object> getDefaultPreferences() {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("language", "en");
        prefs.put("timezone", "America/New_York");
        prefs.put("emailNotifications", true);
        prefs.put("theme", "light");
        return prefs;
    }
}
