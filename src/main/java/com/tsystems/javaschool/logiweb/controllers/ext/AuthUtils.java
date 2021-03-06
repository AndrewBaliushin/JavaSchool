package com.tsystems.javaschool.logiweb.controllers.ext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.tsystems.javaschool.logiweb.LogiwebAppContext;
import com.tsystems.javaschool.logiweb.model.status.UserRole;

/**
 * Set of utility methods for classes that use authentication.
 * 
 * @author Andrey Baliushin
 */
public class AuthUtils {

    private AuthUtils() {
    }

    /**
     * Check if user is marked in session as logged in.
     * 
     * @param request
     * @return
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return request.getSession().getAttribute(LogiwebAppContext.SESSION_ATTR_TO_STORE_ROLE) != null;
    }
    
    /**
     * Create authentificated session by adding user role attribute to 
     * session for current user (the one who made request).
     * 
     * @param session
     * @param role
     */
    public static void startAuthSessionForRole(HttpSession session, UserRole role) {
        session.setAttribute(LogiwebAppContext.SESSION_ATTR_TO_STORE_ROLE, role);
    }
    
    /**
     * Destroy authentificated session for current user (the one who made request)
     * by removing role attribute.
     * 
     * @param session
     */
    public static void destroyAuthSession(HttpSession session) {
        session.setAttribute(LogiwebAppContext.SESSION_ATTR_TO_STORE_ROLE, null);
    }
    
    /**
     * Get user role from session parameter of request.
     * 
     * @param request
     * @return role or null if not set or object in sess. param. is not UserRole
     */
    public static UserRole getUserRole(HttpServletRequest request) {
        Object roleObject = request.getSession().getAttribute(LogiwebAppContext.SESSION_ATTR_TO_STORE_ROLE);
        if (roleObject != null && roleObject instanceof UserRole) {
            UserRole role = (UserRole) roleObject;
            return role;
        } else {
            return null;
        }
    }
}
