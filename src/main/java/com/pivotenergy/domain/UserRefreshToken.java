package com.pivotenergy.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@SuppressWarnings("unused")
@Entity
@NoArgsConstructor
public class UserRefreshToken extends MultiTenantBaseDomainEntity<UserRefreshToken> {

    @NotEmpty
    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "`user_id`", referencedColumnName = "`id`", insertable = false, updatable = false)
    @JsonIgnore
    @JsonBackReference
    private User user;

    public String getToken() {
        return token;
    }

    public UserRefreshToken setToken(String token) {
        this.token = token;
        return this;
    }

    public User getUser() {
        return user;
    }

    public UserRefreshToken setUser(User user) {
        this.user = user;
        return this;
    }
}
