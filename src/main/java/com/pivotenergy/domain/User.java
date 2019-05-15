/*
 * ________________________________________________________________________
 * METRO.IO CONFIDENTIAL
 * ________________________________________________________________________
 *
 * Copyright (c) 2017.
 * Metro Labs Incorporated
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Metro Labs Incorporated and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Metro Labs Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Metro Labs Incorporated.
 */

package com.pivotenergy.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
@Getter
@NoArgsConstructor
public class User extends MultiTenantBaseDomainEntity<User> {
    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @NotBlank
    private String locale;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Type type = Type.USER;

    @NotNull
    private Boolean enabled = false;

    @NotNull
    private Boolean locked = false;

    @NotNull
    private Boolean expired = false;

    @NotNull
    private Integer failedLoginAttempts = 0;

    @Past
    @Temporal(value= TemporalType.TIMESTAMP)
    private Date lastLoginAttempt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private Set<Role> roles = new HashSet<>();

    @NotBlank
    private String password = null;

    @ManyToOne
    @JoinColumn(name = "groupId")
    @JsonIgnore
    @JsonBackReference
    private Group group;

    public User setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public User setType(Type type) {
        this.type = type;
        return this;
    }

    public User setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public User setLocked(Boolean locked) {
        this.locked = locked;
        return this;
    }

    public User setExpired(Boolean expired) {
        this.expired = expired;
        return this;
    }

    public User setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
        return this;
    }

    public User setLastLoginAttempt(Date lastLoginAttempt) {
        this.lastLoginAttempt = lastLoginAttempt;
        return this;
    }

    public User setRoles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }

    public User addRole(Role role) {
        this.roles.add(role.setUser(this));
        return this;
    }

    public User setPassword(String password) {
        Assert.notNull(password, "password cannot be null");
        this.password = ENCODER.encode(password);
        return this;
    }

    public User setGroup(Group group) {
        this.group = group;
        return this;
    }

    @JsonIgnore
    public boolean isValid() {
        return enabled && !locked && !expired && Objects.nonNull(password);
    }

    @JsonIgnore
    public boolean isLocked() {
        return locked;
    }

    public enum Type {
        API,
        USER,
        ADMIN,
        SUPPORT
    }
}
