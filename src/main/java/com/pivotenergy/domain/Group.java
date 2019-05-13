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

import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Entity
@NoArgsConstructor
public class Group extends BaseDomainEntity<Group> {
    @Size(min = 4, message="The group name must be at least 4 character long")
    @NotBlank(message="The group name cannot be empty")
    private String name;

    @URL(message="Valid image url must be provided")
    @Column(name = "logo_image", unique = true)
    private String logoImage;


    @Column(name = "company_name", unique = true)
    private String companyName;

    @URL(message="Valid url must be provided")
    @Column(name = "company_website", unique = true)
    private String companyWebsite;

    @Email(message = "Valid email must be provided")
    @Column(name = "contact_email", unique = true)
    private String contactEmail;

    @Size(max = 20)
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 128)
    private Type type = Type.CLIENT;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private Set<User> users = new HashSet<>();

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public String getLogoImage() {
        return logoImage;
    }

    public Group setLogoImage(String logoImage) {
        this.logoImage = logoImage;
        return this;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Group setCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public String getCompanyWebsite() {
        return companyWebsite;
    }

    public Group setCompanyWebsite(String companyWebsite) {
        this.companyWebsite = companyWebsite;
        return this;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public Group setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Group setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public Type getType() {
        return type;
    }

    public Group setType(Type type) {
        this.type = type;
        return this;
    }

    public Set<User> getUsers() {
        return users;
    }

    public Group setUsers(Set<User> users) {
        this.users = users;
        return this;
    }

    public Group addUser(User user) {
        this.users.add(user.setGroup(this));
        return this;
    }

    public enum Type {
        ADMIN,
        CLIENT,
        SUPPORT
    }
}
