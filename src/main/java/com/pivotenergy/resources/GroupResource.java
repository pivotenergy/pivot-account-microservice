package com.pivotenergy.resources;

import com.pivotenergy.domain.Group;
import com.pivotenergy.domain.User;
import com.pivotenergy.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups")
public class GroupResource {
    private GroupService groupService;

    @Autowired
    GroupResource(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_SUPPORT_CREATE_USER')")
    public @ResponseBody
    Group create(@RequestBody Group group) {
        return groupService.create(group);
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ROLE_SUPPORT_READ_USER')")
    public @ResponseBody
    Page<Group> getCollection(@PageableDefault Pageable pageable) {
        return groupService.getRepository().findAll(pageable);
    }

    @PutMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('ROLE_SUPPORT_UPDATE_USER')")
    public @ResponseBody
    Group updateById(@PathVariable String id, @RequestBody Group group) throws Throwable {
        return groupService.update(id, group);
    }

    @PatchMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public @ResponseBody
    Group patchyId(@PathVariable String id, @RequestBody Map<String, Object> patch) throws Throwable {
        return groupService.patch(id, patch);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_USER_SOFT_DELETE_USER')")
    public void deleteById(@PathVariable String id) {
        groupService.softDelete(id);
    }

    @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Group getById(@PathVariable String id) throws Throwable {
        return groupService.getById(id);
    }


    /*
     * USERS ENDPOINTS
     */
    @PostMapping(path = "/{id}/users", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_SUPPORT_CREATE_USER')")
    public @ResponseBody
    User addUser(@PathVariable String id, @RequestBody @Valid User user) throws Throwable {
        return groupService.addUser(id, user);
    }


    /*
     * METADATA ENDPOINTS
     */
    @GetMapping(path = "/metadata/types", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    List<Group.Type> getTypes() {
        return Arrays.asList(Group.Type.values());
    }
}
