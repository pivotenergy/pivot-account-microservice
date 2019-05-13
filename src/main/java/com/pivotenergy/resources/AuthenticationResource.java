package com.pivotenergy.resources;

import com.pivotenergy.exceptions.PivotRefreshTokenException;
import com.pivotenergy.security.JWTSecurityService;
import com.pivotenergy.security.model.response.TokenPair;
import com.pivotenergy.services.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.pivotenergy.security.JWTSecurityService.AUTHORIZATION_REFRESH;

@RestController
public class AuthenticationResource {
    private AuthenticationService authenticationService;

    @Autowired
    AuthenticationResource(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TokenPair> login(@RequestHeader(name=HttpHeaders.AUTHORIZATION) String credentials) {
        return sendTokenPairAndHeaders(authenticationService.loginUser(credentials));
    }

    @PostMapping(path = "/refresh/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TokenPair> refresh(@PathVariable String token) {
        TokenPair tokenPair = authenticationService.refreshAccessToken(token)
                .orElseThrow(PivotRefreshTokenException::new);

        return sendTokenPairAndHeaders(tokenPair);
    }

    @DeleteMapping(path = "/logout/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@PathVariable String token) {
        authenticationService.logoutUser(token);
    }

    private ResponseEntity<TokenPair> sendTokenPairAndHeaders(TokenPair tokenPair) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(JWTSecurityService.AUTHORIZATION_HEADER, tokenPair.getBearerToken());
        headers.set(AUTHORIZATION_REFRESH, tokenPair.getRefreshToken());
        return new ResponseEntity<>(tokenPair, headers, HttpStatus.OK);
    }
}
