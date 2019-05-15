package com.pivotenergy.services

import com.pivotenergy.domain.Group
import com.pivotenergy.domain.Role
import com.pivotenergy.domain.User
import com.pivotenergy.exceptions.PivotAuthenticationFailureException
import com.pivotenergy.repositories.GroupRepository
import com.pivotenergy.repositories.UserRefreshTokenRepository
import com.pivotenergy.repositories.UserRepository
import com.pivotenergy.security.JWTSecurityService
import com.pivotenergy.security.model.request.UserLogin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import spock.lang.Shared
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDateTime
import java.time.OffsetDateTime

@DataJpaTest(properties = "classpath:application.properties")
class AuthenticationServiceTest extends Specification {
    @Autowired
    GroupRepository groupRepository
    @Autowired
    UserRepository userRepository
    @Autowired
    UserRefreshTokenRepository refreshTokenRepository

    @Shared static def user
    @Shared static def email = "john.doe@test.io"
    @Shared static def password = "password"
    @Shared static def authenticationService

    def setup() {
        def passwordEncoder = new BCryptPasswordEncoder()
        authenticationService = new AuthenticationService(userRepository,
                refreshTokenRepository,
                passwordEncoder, new JWTSecurityService("secret", 60000))
        def group = groupRepository.save(new Group()
                .setCreatedAt(new Date())
                .setCreatedBy("1")
                .setUpdatedAt(new Date())
                .setUpdatedBy("1")
                .setName(email)
                .setContactEmail(email)
                .setType(Group.Type.SUPPORT))

        user = userRepository.save(new User()
                .setCreatedAt(new Date())
                .setCreatedBy("1")
                .setUpdatedAt(new Date())
                .setUpdatedBy("1")
                .setGroup(group)
                .setType(User.Type.USER)
                .setLocale("EN")
                .setEmail(email)
                .setFirstName("John")
                .setLastName("Doe")
                .setFailedLoginAttempts(0)
                .setEnabled(true)
                .setExpired(false)
                .setLocked(false)
                .setPassword(password)
                .addRole(new Role()
                        .setCreatedAt(new Date())
                        .setCreatedBy("1")
                        .setUpdatedAt(new Date())
                        .setUpdatedBy("1")
                        .setRole(Role.Scope.ROLE_ADMIN, Role.Action.ADMIN, Role.Target.BUILDINGS))
                .addRole(new Role()
                        .setCreatedAt(new Date())
                        .setCreatedBy("1")
                        .setUpdatedAt(new Date())
                        .setUpdatedBy("1")
                        .setRole(Role.Scope.ROLE_USER, Role.Action.READ, Role.Target.GLOBAL)))
    }

    def cleanup() {
        SecurityContextHolder.clearContext()
    }

    def "user from setup spec should exist"() {
        given: "the email address of the previously saved user"
        def emailAddress = email

        when: "user is loaded from repository by email"
        def found = userRepository.findByEmail(emailAddress)

        then: "the user should be present"
        found.present

        and: "the email addresses should be the same"
        found.get().email == user.email
    }

    def "user lookup with invalid email should fail" () {
        given: "a invalid email address"
        def emailAddress = "bad.email@test.io"

        when: "user is loaded from repository by email"
        def found = userRepository.findByEmail(emailAddress)

        then: "the user should not be present"
        !found.present
    }

    def "user login should succeed with valid credentials" () {
        given: "a valid login payload"
        def login = new UserLogin(email, password)

        when: "a successful login attempt is made"
        def attempt = authenticationService.loginUser(login)

        then: "the attempt result should contain a jwt string"
        attempt.accessToken != null

        and: "the attempt result should contain a refresh token string"
        attempt.refreshToken != null

        and: "a refresh token record should be present"
        def refreshToken = refreshTokenRepository.findByToken(attempt.refreshToken)
        refreshToken.present

    }

    def "user login should fail with invalid username" () {
        given: "a invalid password"
        def login = new UserLogin(email+"_", password)

        when: "a login attempt is made"
        authenticationService.loginUser(login)

        then: "the a PivotAuthenticationFailureException should be thrown"
        final PivotAuthenticationFailureException exception = thrown()
        exception.message == "Login Failed"
    }

    def "user login should fail with invalid password" () {
        given: "a invalid password"
        def login = new UserLogin(email, password+"_")

        when: "a login attempt is made"
        authenticationService.loginUser(login)

        then: "the a PivotAuthenticationFailureException should be thrown"
        final PivotAuthenticationFailureException exception = thrown()
        exception.message == "Login Failed"

        and: "failed login counter should be incremented by 1"
        def found = userRepository.findByEmail(email).get()
        found.failedLoginAttempts == 1
    }

    def "user account should locked after 5 failed login attempts" () {
        given: "a invalid password"
        def login = new UserLogin(email, password+"_")

        when: "after 5 failed login attempts"
        5.times {
            try {
                authenticationService.loginUser(login)
            }
            catch(PivotAuthenticationFailureException ignore) {}
        }

        then: "the users account should be locked"
        def found = userRepository.findByEmail(email).get()
        !found.isValid()
        found.locked
        found.failedLoginAttempts >= 5
    }

    def "locked user account should remain locked during cool down period" () {
        given: "a locked user account due to to many login failures"
        user.locked = true
        user.failedLoginAttempts = 5
        LocalDateTime current = LocalDateTime.now()
        OffsetDateTime offset = OffsetDateTime.now(Clock.systemDefaultZone())
        user.lastLoginAttempt = Date.from(current.toInstant(offset.getOffset()))

        when: "when the 5 minute cool down period has not completed"
        def updated = authenticationService.resetLockedUser(user as User)

        then: "the users account should still be locked and login attempts should be 5"
        updated.locked
        updated.failedLoginAttempts == 5
    }

    def "locked user account should be unlocked after cool down period" () {
        given: "a locked user account due to to many login failures"
        user.locked = true
        user.failedLoginAttempts = 5
        LocalDateTime current = LocalDateTime.now().minusMinutes(5)
        OffsetDateTime offset = OffsetDateTime.now(Clock.systemDefaultZone())
        user.lastLoginAttempt = Date.from(current.toInstant(offset.getOffset()))

        when: "when the 5 minute cool down period has completed"
        def updated = authenticationService.resetLockedUser(user as User)

        then: "the users account should not be locked and login attempts should be 0"
        !updated.locked
        updated.failedLoginAttempts == 0
    }
}