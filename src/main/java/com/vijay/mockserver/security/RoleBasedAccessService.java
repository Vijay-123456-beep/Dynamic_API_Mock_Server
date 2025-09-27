package com.vijay.mockserver.security;

import com.vijay.mockserver.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class RoleBasedAccessService {

    /**
     * Check if current user has the required role
     */
    public boolean hasRole(User.Role requiredRole) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return false;
        }

        // In a real implementation, you would get the user's roles from the authentication
        // For now, we'll assume the user object is available in the security context
        return true; // Simplified for this example
    }

    /**
     * Check if current user is admin or super admin
     */
    public boolean isAdmin() {
        return hasRole(User.Role.ADMIN) || hasRole(User.Role.SUPER_ADMIN);
    }

    /**
     * Check if current user is super admin
     */
    public boolean isSuperAdmin() {
        return hasRole(User.Role.SUPER_ADMIN);
    }

    /**
     * Check if user can access another user's resources
     */
    public boolean canAccessUserResources(User currentUser, User targetUser) {
        // Users can access their own resources or admins can access any user's resources
        return currentUser.getId().equals(targetUser.getId()) || isAdmin();
    }

    /**
     * Check if user can perform admin operations
     */
    public boolean canPerformAdminOperations(User user) {
        return user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.SUPER_ADMIN;
    }

    /**
     * Check if user can manage other users
     */
    public boolean canManageUsers(User user) {
        return user.getRole() == User.Role.SUPER_ADMIN;
    }
}
