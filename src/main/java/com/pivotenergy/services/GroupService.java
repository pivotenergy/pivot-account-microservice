package com.pivotenergy.services;

import com.pivotenergy.domain.Group;
import com.pivotenergy.domain.Role;
import com.pivotenergy.domain.User;
import com.pivotenergy.exceptions.PivotEntityNotFoundException;
import com.pivotenergy.repositories.GroupRepository;
import com.pivotenergy.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Locale;

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

    public Group findGroupById(String id) {
        return groupRepository.findById(id).orElseThrow(new PivotEntityNotFoundException(Group.class, id));
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
