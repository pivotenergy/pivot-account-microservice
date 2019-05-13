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
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@SuppressWarnings("unused")
@Entity
@NoArgsConstructor
public class Role extends MultiTenantBaseDomainEntity<Role> {
    @NotBlank
    @Column(name = "role", nullable = false, length = 128)
    private String role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 128)
    private Scope scope;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 128)
    private Action action;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target", nullable = false, length = 128)
    private Target target;

    @ManyToOne
    @JoinColumn(name = "`user_id`", referencedColumnName = "`id`")
    @JsonIgnore
    @JsonBackReference
    private User user;

    public String getRole() {
        return role;
    }

    public Role setRole(String role) {
        this.role = role;
        return this;
    }

    public Scope getScope() {
        return scope;
    }

    public Role setScope(Scope scope) {
        this.scope = scope;
        return this;
    }

    public Action getAction() {
        return action;
    }

    public Role setAction(Action action) {
        this.action = action;
        return this;
    }

    public Target getTarget() {
        return target;
    }

    public Role setTarget(Target target) {
        this.target = target;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Role setUser(User user) {
        this.user = user;
        return this;
    }

    public Role setRole(Scope scope, Action action, Target target) {
        Assert.notNull(scope, "Scope cannot be null");
        Assert.notNull(scope, "Action cannot be null");
        Assert.notNull(scope, "Target cannot be null");

        this.scope = scope;
        this.action = action;
        this.target = target;
        this.role = String.format("%s_%s_%s", scope, action, target);
        return this;
    }


    public enum Scope {
        ROLE_ADMIN,
        ROLE_SUPPORT,
        ROLE_API,
        ROLE_DEVELOPER,
        ROLE_USER
    }

    public enum Action {
        ADMIN,
        CREATE,
        SOFT_DELETE,
        HARD_DELETE,
        UPDATE,
        READ
    }

    public enum Target {
        GLOBAL,
        USERS,
        BUILDINGS,
        MEASURABLES,
        BASELINES,
        OPPORTUNITIES,
        MEASURES,
    }
}
