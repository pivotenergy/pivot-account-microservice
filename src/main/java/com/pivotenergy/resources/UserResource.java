package com.pivotenergy.resources;

import com.pivotenergy.domain.Role;
import com.pivotenergy.domain.User;
import com.pivotenergy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@RestController
public class UserResource {
    private UserService userService;

    @Autowired
    UserResource(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_SUPPORT_CREATE_USER')")
    public User createUser(@RequestBody User user) {
        return userService.saveUser(user);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Page<User> getCollectionOfUser(@PageableDefault Pageable pageable) {
        return userService.getUserRepository().findAll(pageable);
    }

    @PutMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('ROLE_SUPPORT_UPDATE_USER')")
    public @ResponseBody User updateUserById(@PathVariable String id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    @PatchMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody User patchUserById(@PathVariable String id, @RequestBody Map<String, Object> patch) throws IOException {
        return userService.patchUser(id, patch);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_USER_SOFT_DELETE_USER')")
    public void deleteUserById(@PathVariable String id) {
        userService.softDeleteUser(id);
    }

    /*
     * ROLES ENDPOINTS
     */
    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SUPPORT_UPDATE_USER')")
    public @ResponseBody User getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @PostMapping(path = "/{id}/roles", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_SUPPORT_UPDATE_USER')")
    public @ResponseBody Role addRoleToUser(@PathVariable String id, @RequestBody @Valid Role role) {
        return userService.addRoleToUser(id, role);
    }

    @DeleteMapping(path = "/{id}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_SUPPORT_UPDATE_USER')")
    public void deleteRoleById(@PathVariable String id, @PathVariable String roleId) {
        userService.deleteUserRole(id, roleId);
    }
}
