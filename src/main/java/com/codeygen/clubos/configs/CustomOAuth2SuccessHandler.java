package com.codeygen.clubos.configs;

import com.codeygen.clubos.entities.user.User;
import com.codeygen.clubos.repositories.user.UserRepository;
import com.codeygen.clubos.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Email not found from OAuth2 provider");
            return;
        }

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String jwt = jwtUtils.generateToken(user.getEmail(), user.getRole().name());

            response.setContentType("application/json");
            Map<String, String> tokenResponse = new HashMap<>();
            tokenResponse.put("token", jwt);
            tokenResponse.put("email", user.getEmail());
            tokenResponse.put("role", user.getRole().name());

            response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Forbidden");
            errorResponse.put("message", "User with email " + email + " is not registered in the system.");
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
