package com.pivotenergy.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import java.util.Date;

@SuppressWarnings("unused")
@Entity
@NoArgsConstructor
@Getter
public class UserRefreshToken extends MultiTenantBaseDomainEntity<UserRefreshToken> {

    @NotEmpty
    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date expiresAt;

    @ManyToOne
    @JsonIgnore
    @JsonBackReference
    private User user;

    public UserRefreshToken setToken(String token) {
        this.token = token;
        return this;
    }

    public UserRefreshToken setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public UserRefreshToken setUser(User user) {
        this.user = user;
        this.groupId = user.getGroup().id;
        return this;
    }
}
