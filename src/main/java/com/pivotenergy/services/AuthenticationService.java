package com.pivotenergy.services;

import com.auth0.jwt.JWTCreator;
import com.pivotenergy.domain.User;
import com.pivotenergy.domain.UserRefreshToken;
import com.pivotenergy.exceptions.PivotAuthenticationFailureException;
import com.pivotenergy.exceptions.PivotEntityNotFoundException;
import com.pivotenergy.repositories.UserRefreshTokenRepository;
import com.pivotenergy.repositories.UserRepository;
import com.pivotenergy.security.JWTSecurityService;
import com.pivotenergy.security.model.UserSession;
import com.pivotenergy.security.model.request.UserLogin;
import com.pivotenergy.security.model.response.TokenPair;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.pivotenergy.security.JWTSecurityService.AUTHORIZATION_REFRESH;

@Service
public class AuthenticationService {
    private Logger LOG = LoggerFactory.getLogger(AuthenticationService.class);

    private UserRepository userRepository;
    private UserRefreshTokenRepository userRefreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JWTSecurityService jwtSecurityService;

    private static final long REFRESH_TOKEN_LIFE = Duration.ofHours(48).toMillis();

    /**
     *
     * @param userRepository UserRepository
     * @param userRefreshTokenRepository UserRefreshTokenRepository
     * @param passwordEncoder PasswordEncoder
     * @param jwtSecurityService JWTSecurityService
     */
    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 UserRefreshTokenRepository userRefreshTokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 JWTSecurityService jwtSecurityService)
    {
        this.userRepository = userRepository;
        this.userRefreshTokenRepository = userRefreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecurityService = jwtSecurityService;
    }


    /**
     * Attempt authorization using Http Basic: credentials
     *
     * @param authentication bse64 encoded basic authentication
     * @return TokenPair
     */
    public TokenPair loginUser(final String authentication) {
        if (authentication != null && authentication.toLowerCase().startsWith("basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authentication.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credString = new String(credDecoded, StandardCharsets.UTF_8);
            // credentials = username:password
            final String[] values = credString.split(":", 2);
            String username = values[0];
            String password = values[1];
            return loginUser(new UserLogin(username, password));
        }

        throw new PivotAuthenticationFailureException("Login Failed", "Invalid Credentials Provided");
    }

    /**
     *
     * @param userLogin username password wrapper
     * @return TokenPair
     */
    public TokenPair loginUser(UserLogin userLogin) {
        return userRepository.findByUserEmail(userLogin.getEmail())
                .map(user -> {

                    if(user.getGroup().getDeleted().equals(Boolean.TRUE)) {
                        throw new PivotEntityNotFoundException(User.class, user.getId());
                    }

                    LocalDateTime current = LocalDateTime.now();
                    OffsetDateTime offset = OffsetDateTime.now(Clock.systemDefaultZone());
                    user.setLastLoginAttempt(Date.from(current.toInstant(offset.getOffset())));
                    resetLockedUser(user);
                    if (user.isValid() && passwordEncoder.matches(userLogin.getPassword(), user.getPassword())) {
                        user.setFailedLoginAttempts(0);
                        return doLoginUser(user);
                    }
                    else {
                        incrementFailedLoginAttempt(user);
                        throw new PivotAuthenticationFailureException("Login Failed", "Invalid Credentials Provided");
                    }
                })
                .orElseThrow(new PivotAuthenticationFailureException("Login Failed", "User Not Found"));
    }

    private TokenPair doLoginUser(User user) {

        if(user.getGroup().getDeleted().equals(Boolean.TRUE)) {
            throw new PivotEntityNotFoundException(User.class, user.getId());
        }

        UserSession userSession = new UserSession();
        userSession.setId(user.getId());
        userSession.setUserEmail(user.getEmail());
        userSession.setFirstName(user.getFirstName());
        userSession.setLastName(user.getLastName());
        userSession.setAccountId(user.getGroup().getId());
        userSession.setAccountType(UserSession.Type.valueOf(user.getType().toString()));
        userSession.setLocale(user.getLocale());
        userSession.setRoles(user.getRoles()
                .stream()
                .map(x -> new UserSession.Role().addRole(
                        UserSession.Role.Scope.valueOf(x.getScope().toString()),
                        UserSession.Role.Action.valueOf(x.getAction().toString()),
                        UserSession.Role.Target.valueOf(x.getTarget().toString())))
                .collect(Collectors.toSet()));

        JWTCreator.Builder builder = jwtSecurityService.buildJWT(userSession);
        String refreshToken = createRefreshToken(user);
        builder.withClaim(AUTHORIZATION_REFRESH, refreshToken);
        String jwt = jwtSecurityService.signJWT(builder);
        int expires = (int) jwtSecurityService.getTokenLifeSeconds();
        return new TokenPair(jwt, expires, refreshToken);
    }


    /**
     * @return newly generated access token or nothing, if the refresh token is not valid
     */
    public Optional<TokenPair> refreshAccessToken(final String refreshToken) {
        return userRefreshTokenRepository.findByToken(refreshToken)
                .map(userRefreshToken -> {
                    logoutUser(refreshToken);
                    return doLoginUser(userRefreshToken.getUser());
                });
    }

    private String createRefreshToken(User user) {
        String token = RandomStringUtils.randomAlphanumeric(128);
        UserRefreshToken refreshToken = new UserRefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Date.from(Instant.now().plusMillis(REFRESH_TOKEN_LIFE)));
        userRefreshTokenRepository.save(refreshToken);
        return token;
    }

    /**
     * if the account was locked due to failed login attempts and the 5 minute cool down time has elapsed
     * unlock the account, reset the timer and failure counter
     * @param user
     * Return User
     */
    public User resetLockedUser(User user) {
        if(user.isLocked() && user.getFailedLoginAttempts() > 0) {
            if (null != user.getLastLoginAttempt()) {
                Instant instant = user.getLastLoginAttempt().toInstant();
                LocalDateTime lastLogin = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                LocalDateTime current = LocalDateTime.now();
                Duration duration = Duration.between(lastLogin, current);
                if (duration.toMinutes() >= 5) {
                    user.setLocked(false);
                    OffsetDateTime offset = OffsetDateTime.now(Clock.systemDefaultZone());
                    user.setLastLoginAttempt(Date.from(current.toInstant(offset.getOffset())));
                    user.setFailedLoginAttempts(0);
                    return userRepository.save(user);
                }
            }
        }

        return user;
    }

    /**
     * increment failed login counter upto 5 and then locks the account
     * @param user the user
     */
    @Transactional
    public void incrementFailedLoginAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        if(attempts >= 5) {
            user.setLocked(true);
        }

        user.setFailedLoginAttempts(attempts);
        LOG.warn("{} login attempts for user {} account locked {}", attempts, user.getEmail(), user.isLocked());
        userRepository.save(user);
    }

    /**
     * Destroy refresh token proactively on logout
     *
     * @param refreshToken refresh token
     */
    @Transactional
    public void logoutUser(String refreshToken) {
        userRefreshTokenRepository.findByToken(refreshToken)
                .ifPresent(userRefreshTokenRepository::delete);
    }

    @Scheduled(fixedDelayString = "PT15M", initialDelayString = "PT5M")
    public void scheduleFixedRateWithInitialDelayTask() {
        LOG.debug("Purging Expired Refresh Tokens");
        userRefreshTokenRepository.purgeExpiredTokens();
    }
}
