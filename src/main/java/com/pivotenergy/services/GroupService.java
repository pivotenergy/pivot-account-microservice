package com.pivotenergy.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.pivotenergy.domain.Group;
import com.pivotenergy.domain.Role;
import com.pivotenergy.domain.User;
import com.pivotenergy.exceptions.PivotEntityNotFoundException;
import com.pivotenergy.repositories.GroupRepository;
import com.pivotenergy.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Locale;
import java.util.Map;

@Service
public class GroupService extends BaseService<Group, GroupRepository> {
    private UserRepository userRepository;

    @Autowired
    GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        super(Group.class, groupRepository);
        this.userRepository = userRepository;
    }

    public Group patch(String id, Map<String, Object> patch) throws Throwable {
        Group incumbent = getById(id);

        sanitizePatch(patch);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectReader objectReader = objectMapper.readerForUpdating(incumbent);
        Group updated = objectReader.readValue(objectMapper.writeValueAsString(patch));

        return  repository.save(updated);
    }

    /**
     * Removes entries from map which are not allowed to be updated
     *
     * @param map patch data
     */
    private void sanitizePatch(Map<String, Object> map) {
        map.remove("id");
        map.remove("deleted");
        map.remove("createdAt");
        map.remove("createdBy");
        map.remove("updatedAt");
        map.remove("updatedBy");
        map.remove("users");
    }

    @Override
    @Transactional
    public void softDelete(String id) {
        if(!repository.existsById(id)){
            throw new PivotEntityNotFoundException(Group.class, id);
        }

        repository.softDeleteById(id);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_SUPPORT_HARD_DELETE_USER')")
    public void hardDelete(String id) {
        if(!repository.existsById(id)){
            throw new PivotEntityNotFoundException(Group.class, id);
        }

        repository.deleteById(id);
    }

    @Transactional
    public User addUser(String id, User user) throws Throwable {
        Group group = getById(id);
        user.getRoles().forEach(x -> x.setRole(x.getScope(), x.getAction(), x.getTarget()));
        user.setGroup(group);

        return userRepository.save(user);
    }

    @PostConstruct
    @Transactional
    public void initializeBaseUsers() {

        Group group;
        if(repository.findByContactEmail("axle@pivotenergy.com").isPresent()) {
            LOG.info("Found account for base users");
            group = repository.findByContactEmail("axle@pivotenergy.com").get();
        }
        else {
            LOG.info("Creating account for base users");
            group = repository.save(new Group()
                    .setName("Pivot Energy")
                    .setContactEmail("axle@pivotenergy.com")
                    .setCompanyName("Pivot Energy Services"));
            LOG.info("Group created");

        }

        if(!userRepository.findByEmail("app-admin@metro.io").isPresent()) {
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
