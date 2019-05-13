package com.pivotenergy.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.pivotenergy.domain.Role;
import com.pivotenergy.domain.User;
import com.pivotenergy.exceptions.PivotEntityNotFoundException;
import com.pivotenergy.repositories.RoleRepository;
import com.pivotenergy.repositories.UserRepository;
import com.pivotenergy.security.model.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.pivotenergy.domain.Role.Action.*;
import static com.pivotenergy.domain.Role.Scope.ROLE_ADMIN;
import static com.pivotenergy.domain.Role.Scope.ROLE_SUPPORT;
import static com.pivotenergy.domain.Role.Target.GLOBAL;
import static com.pivotenergy.domain.Role.Target.USERS;

@Service
public class UserService {
    private Logger LOG = LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;
    private RoleRepository roleRepository;

    @Autowired
    UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    @Transactional(readOnly = true)
    public User getById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(new PivotEntityNotFoundException(User.class, id));

        if(user.getGroup().getDeleted().equals(Boolean.TRUE)) {
            throw new PivotEntityNotFoundException(User.class, id);
        }

        if(isAdminOrSupportRequest(user)) {
            return user;
        }
        else if(isOwnerRequest(user)) {
            return user;
        }

        String message = String.format("Either this resource does not belong to you or you do not have the " +
                "privileges required to modify this resource.");
        throw new AccessDeniedException("Request Denied", new Throwable(message));
    }

    @Transactional
    public User create(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User update(String id, User update) {
        if(!userRepository.existsById(id)){
            throw new PivotEntityNotFoundException(User.class, id);
        }

        return userRepository.save(update);
    }

    @Transactional
    public User patch(String id, Map<String, Object> patch) throws IOException {

        User incumbent = userRepository.findById(id)
                .orElseThrow(new PivotEntityNotFoundException(User.class, id));

        if(isOwnerRequest(incumbent) || isAdminOrSupportRequest(incumbent)) {
            if(isAdminOrSupportRequest(incumbent)) {
                patch = sanitizeForAdminOrSupport(patch);
            }
            else {
                patch = sanitizeForUser(patch);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectReader objectReader = objectMapper.readerForUpdating(incumbent);
            User updated = objectReader.readValue(objectMapper.writeValueAsString(patch));

            return userRepository.save(updated);
        }

        String message = String.format("Either this resource does not belong to you or you do not have the " +
                "privileges required to modify this resource.");
        throw new AccessDeniedException("Request Denied", new Throwable(message));
    }

    @Transactional
    public Role addRole(String id, Role role) {
        User user = getById(id);
        role.setRole(role.getScope(), role.getAction(), role.getTarget())
                .setUser(user);

        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(final String id, final String roleId) {
        User user = getById(id);
        Role role = user.getRoles()
                .stream()
                .filter(x -> x.getId().equals(roleId)).findFirst()
                .orElseThrow(new PivotEntityNotFoundException(Role.class, id));

        roleRepository.delete(role);
    }

    /**
     * Removes entries from map which are not allowed to be updated by the user
     *
     * @param map patch data
     * @return Map<String, Object>
     */
    public Map<String, Object> sanitizeForUser(Map<String, Object> map) {
        map.remove("id");
        map.remove("groupId");
        map.remove("type");
        map.remove("enabled");
        map.remove("locked");
        map.remove("expired");
        map.remove("failedLoginAttempts");
        map.remove("lastLoginAttempt");
        map.remove("roles");
        map.remove("password");
        map.remove("group");
        map.remove("deleted");
        map.remove("createdAt");
        map.remove("createdBy");
        map.remove("updatedAt");
        map.remove("updatedBy");

        return map;
    }

    public boolean isOwnerRequest(User user) {
        UserSession session = (UserSession) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return user.getId().equals(session.getId()) && user.getGroup().getId().equals(session.getTenantId());
    }

    /**
     * Removes entries from map which are not allowed to be updated by the user
     *
     * @param map patch data
     * @return Map<String, Object>
     */
    public Map<String, Object> sanitizeForAdminOrSupport(Map<String, Object> map) {
        map.remove("id");
        map.remove("createdAt");
        map.remove("createdBy");
        map.remove("updatedAt");
        map.remove("updatedBy");
        return map;
    }

    public boolean isAdminOrSupportRequest(User user) {
        UserSession session = (UserSession) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Set<String> roles = new HashSet<>();
        roles.addAll(getSupportRoles());
        roles.addAll(getAdministrativeRoles());

        return user.getGroup().getId().equals(session.getTenantId())
                && (session.getAccountType().equals(UserSession.Type.ADMIN) ||
                session.getAccountType().equals(UserSession.Type.SUPPORT)) &&
                session.getAuthorities().stream().anyMatch(roles::contains);
    }

    @Transactional
    public void softDelete(String id) {
        if(!userRepository.existsById(id)){
            throw new PivotEntityNotFoundException(User.class, id);
        }

        userRepository.softDeleteById(id);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_SUPPORT_HARD_DELETE_USER')")
    public void hardDelete(String id) {
        if(!userRepository.existsById(id)){
            throw new PivotEntityNotFoundException(User.class, id);
        }

        userRepository.deleteById(id);
    }


    @SuppressWarnings("Duplicates")
    public static Set<String> getAdministrativeRoles() {
        Set<String> roles = new HashSet<>();
        roles.add(String.format("%s_%s_%s", ROLE_ADMIN, ADMIN, GLOBAL));
        roles.add(String.format("%s_%s_%s", ROLE_ADMIN, CREATE, GLOBAL));
        roles.add(String.format("%s_%s_%s", ROLE_ADMIN, UPDATE, GLOBAL));
        roles.add(String.format("%s_%s_%s", ROLE_ADMIN, ADMIN, USERS));
        roles.add(String.format("%s_%s_%s", ROLE_ADMIN, CREATE, USERS));
        roles.add(String.format("%s_%s_%s", ROLE_ADMIN, UPDATE, USERS));

        return roles;
    }

    @SuppressWarnings("Duplicates")
    public static Set<String> getSupportRoles() {
        Set<String> roles = new HashSet<>();
        roles.add(String.format("%s_%s_%s", ROLE_SUPPORT, ADMIN, GLOBAL));
        roles.add(String.format("%s_%s_%s", ROLE_SUPPORT, CREATE, GLOBAL));
        roles.add(String.format("%s_%s_%s", ROLE_SUPPORT, UPDATE, GLOBAL));
        roles.add(String.format("%s_%s_%s", ROLE_SUPPORT, ADMIN, USERS));
        roles.add(String.format("%s_%s_%s", ROLE_SUPPORT, CREATE, USERS));
        roles.add(String.format("%s_%s_%s", ROLE_SUPPORT, UPDATE, USERS));

        return roles;
    }
}
