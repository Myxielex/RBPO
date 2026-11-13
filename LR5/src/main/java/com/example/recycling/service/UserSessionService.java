package com.example.recycling.service;

import com.example.recycling.dto.TokenPairResponse;
import com.example.recycling.model.SessionStatus;
import com.example.recycling.model.User;
import com.example.recycling.model.UserSession;
import com.example.recycling.repository.UserRepository;
import com.example.recycling.repository.UserSessionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class UserSessionService {
    private final UserSessionRepository sessions;
    private final UserRepository users;
    private final JwtService jwt;
    private final PasswordEncoder passwordEncoder;

    public UserSessionService(UserSessionRepository sessions, UserRepository users,
                              JwtService jwt, PasswordEncoder passwordEncoder) {
        this.sessions = sessions;
        this.users = users;
        this.jwt = jwt;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TokenPairResponse login(String username, String password, String deviceId) {
        User user = users.findByUsername(username.trim().toLowerCase()).orElse(null);
        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(password, user.getPasswordHash())
                || !user.isEnabled() || !user.isAccountNonLocked()
                || !user.isAccountNonExpired() || !user.isCredentialsNonExpired()) {
            return null;
        }
        return createPair(user, deviceId);
    }

    @Transactional
    public TokenPairResponse register(com.example.recycling.dto.RegisterUserRequest request,
                                      UserService userService, String deviceId) {
        User user = userService.register(request);
        return createPair(user, deviceId);
    }

    @Transactional
    public TokenPairResponse refresh(String refreshToken) {
        if (!jwt.isTokenOfType(refreshToken, "refresh")) return null;

        String hash = hash(refreshToken);
        UserSession oldSession = sessions.findByRefreshTokenHashForUpdate(hash).orElse(null);
        if (oldSession == null) return null;

        if (oldSession.getStatus() == SessionStatus.USED) {
            oldSession.setStatus(SessionStatus.REVOKED);
            sessions.save(oldSession);
            return null; // Возвращаем отказ, но транзакция фиксирует REVOKED.
        }
        if (oldSession.getStatus() != SessionStatus.ACTIVE
                || !oldSession.getRefreshToken().equals(refreshToken)
                || !oldSession.getRefreshTokenExpiry().isAfter(Instant.now())
                || !oldSession.getUsername().equals(jwt.getUsername(refreshToken))) {
            return null;
        }

        User user = users.findByUsername(oldSession.getUsername()).orElse(null);
        if (user == null || !user.isEnabled() || !user.isAccountNonLocked()
                || !user.isAccountNonExpired() || !user.isCredentialsNonExpired()) {
            oldSession.setStatus(SessionStatus.REVOKED);
            sessions.save(oldSession);
            return null;
        }

        oldSession.setStatus(SessionStatus.USED);
        sessions.save(oldSession);
        return createPair(user, oldSession.getDeviceId());
    }

    private TokenPairResponse createPair(User user, String deviceId) {
        String access = jwt.createAccessToken(user.getUsername());
        String refresh = jwt.createRefreshToken(user.getUsername());

        UserSession session = new UserSession();
        session.setUsername(user.getUsername());
        session.setDeviceId(deviceId);
        session.setAccessToken(access);
        session.setRefreshToken(refresh);
        session.setRefreshTokenHash(hash(refresh));
        session.setAccessTokenExpiry(jwt.getExpiry(access));
        session.setRefreshTokenExpiry(jwt.getExpiry(refresh));
        session.setStatus(SessionStatus.ACTIVE);
        sessions.save(session);

        return new TokenPairResponse(access, refresh);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Не удалось вычислить хеш токена", exception);
        }
    }
}
