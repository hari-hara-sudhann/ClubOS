package com.codeygen.clubos.configs;

import com.codeygen.clubos.entities.user.User;
import com.codeygen.clubos.repositories.user.UserRepository;
import com.codeygen.clubos.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    public CustomOAuth2SuccessHandler(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            response.sendRedirect(frontendUrl+"/?error=no_email");
            return;
        }

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String jwt = jwtUtils.generateToken(user.getEmail(), user.getRole().name());

            String redirectUri = String.format(frontendUrl+"/auth/callback/?token=%s&role=%s", jwt, user.getRole().name());
            response.sendRedirect(redirectUri);
        } else {
            response.sendRedirect(frontendUrl+"/?error=unregistered&email="+email);
        }
    }
}
