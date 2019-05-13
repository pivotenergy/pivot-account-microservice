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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserResource {
    private UserService userService;

    @Autowired
    UserResource(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_SUPPORT_CREATE_USER')")
    public @ResponseBody
    User create(@RequestBody User user) {
        return userService.create(user);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Page<User> getCollection(@PageableDefault Pageable pageable) {
        return userService.getUserRepository().findAll(pageable);
    }

    @PutMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('ROLE_SUPPORT_UPDATE_USER')")
    public @ResponseBody
    User updateById(@PathVariable String id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @PatchMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody
    User patchById(@PathVariable String id, @RequestBody Map<String, Object> patch) throws IOException {
        return userService.patch(id, patch);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_USER_SOFT_DELETE_USER')")
    public void deleteById(@PathVariable String id) {
        userService.softDelete(id);
    }

    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    User getById(@PathVariable String id) {
        return userService.getById(id);
    }


    /*
     * ROLES ENDPOINTS
     */
    @PostMapping(path = "/{id}/roles", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_SUPPORT_UPDATE_USER')")
    public @ResponseBody
    Role addRole(@PathVariable String id, @RequestBody @Valid Role role) {
        return userService.addRole(id, role);
    }

    @DeleteMapping(path = "/{id}/roles/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_SUPPORT_UPDATE_USER')")
    public void deleteRole(@PathVariable String id, @PathVariable String roleId) {
        userService.deleteRole(id, roleId);
    }


    /*
     * METADATA ENDPOINTS
     */
    @GetMapping(path = "/metadata/types", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<User.Type> getTypes() {
        return Arrays.asList(User.Type.values());
    }

    @GetMapping(path = "/metadata/roles/scopes", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<Role.Scope> getRoleScopes() {
        return Arrays.asList(Role.Scope.values());
    }

    @GetMapping(path = "/metadata/roles/actions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<Role.Action> getRoleActions() {
        return Arrays.asList(Role.Action.values());
    }

    @GetMapping(path = "/metadata/roles/targets", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<Role.Target> getRoleTargets() {
        return Arrays.asList(Role.Target.values());
    }
}
