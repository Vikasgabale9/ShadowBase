
package com.shadowbase.security.dto;

public record AuthResponse(
        boolean success,
        String message,
        String token,
        String tokenType,
        Long userId,
        String username,
        String email,
        String role
) {

    public static AuthResponse success(
            String message,
            String token,
            Long userId,
            String username,
            String email,
            String role
    ) {
        return new AuthResponse(
                true,
                message,
                token,
                "Bearer",
                userId,
                username,
                email,
                role
        );
    }
}
