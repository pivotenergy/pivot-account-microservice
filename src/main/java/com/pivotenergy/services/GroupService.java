package com.pivotenergy.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.pivotenergy.domain.Group;
import com.pivotenergy.domain.Role;
import com.pivotenergy.domain.User;
import com.pivotenergy.exceptions.PivotEntityNotFoundException;
import com.pivotenergy.exceptions.PivotInvalidRequestException;
import com.pivotenergy.repositories.GroupRepository;
import com.pivotenergy.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Service
public class GroupService {
    private Logger LOG = LoggerFactory.getLogger(GroupService.class);

    private GroupRepository groupRepository;
    private UserRepository userRepository;

    @Autowired
    GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public GroupRepository getGroupRepository() {
        return groupRepository;
    }

    @Transactional(readOnly = true)
    public Group getById(String id) {
        return groupRepository.findById(id).orElseThrow(new PivotEntityNotFoundException(Group.class, id));
    }

    @Transactional
    public Group create(Group group) {
        return groupRepository.save(group);
    }

    @Transactional
    public Group update(String id, Group update) {
        Group group = getById(id);

        if(group.getId().equals(update.getId())) {
            return groupRepository.save(update);
        }

        String message = String.format("Invalid group identifiers! Attempting to update Group with id = %s " +
                "but the provided path variable id is %s", update.getId(), id);
        throw new PivotInvalidRequestException("Invalid Identifiers Provided", message);
    }

    @Transactional
    public Group patch(String id, Map<String, Object> patch) throws IOException {
        Group incumbent = getById(id);

        patch = sanitizePatch(patch);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectReader objectReader = objectMapper.readerForUpdating(incumbent);
        Group updated = objectReader.readValue(objectMapper.writeValueAsString(patch));

        return groupRepository.save(updated);
    }

    @Transactional
    public void softDelete(String id) {
        if(!groupRepository.existsById(id)){
            throw new PivotEntityNotFoundException(Group.class, id);
        }

        groupRepository.softDeleteById(id);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_SUPPORT_HARD_DELETE_USER')")
    public void hardDelete(String id) {
        if(!groupRepository.existsById(id)){
            throw new PivotEntityNotFoundException(Group.class, id);
        }

        groupRepository.deleteById(id);
    }

    @Transactional
    public User addUser(String id, User user) {
        Group group = getById(id);
        user.getRoles().forEach(x -> x.setRole(x.getScope(), x.getAction(), x.getTarget()));
        user.setGroup(group);

        return userRepository.save(user);
    }

    /**
     * Removes entries from map which are not allowed to be updated
     *
     * @param map patch data
     * @return Map<String, Object>
     */
    public Map<String, Object> sanitizePatch(Map<String, Object> map) {
        map.remove("id");
        map.remove("deleted");
        map.remove("createdAt");
        map.remove("createdBy");
        map.remove("updatedAt");
        map.remove("updatedBy");
        map.remove("users");

        return map;
    }

    @PostConstruct
    @Transactional
    public void initializeBaseUsers() {

        Group group;
        if(groupRepository.findByAccountEmail("axle@pivotenergy.com").isPresent()) {
            LOG.info("Found account for base users");
            group = groupRepository.findByAccountEmail("axle@pivotenergy.com").get();
        }
        else {
            LOG.info("Creating account for base users");
            group = groupRepository.save(new Group()
                    .setName("Pivot Energy")
                    .setContactEmail("axle@pivotenergy.com")
                    .setCompanyName("Pivot Energy Services"));
            LOG.info("Group created");

        }

        if(!userRepository.findByUserEmail("app-admin@metro.io").isPresent()) {
            LOG.info("Creating base users");
            userRepository.save(new User()
                    .setGroup(group)
                    .setType(User.Type.ADMIN)
                    .setFirstName("Application")
                    .setLastName("Administrator")
                    .setEmail("app-admin@metro.io")
                    .addRole(new Role().setRole(Role.Scope.ROLE_ADMIN, Role.Action.ADMIN, Role.Target.GLOBAL))
                    .setLocale(Locale.US.getISO3Language())
                    .setPassword("2S33k0u7@2016")
                    .setEnabled(true));

            LOG.info("Users created");
        }
    }
}
