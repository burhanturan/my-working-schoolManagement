package com.project.schoolmanagment;

import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.entity.conceretes.user.UserRole;
import com.project.schoolmanagment.entity.enums.Gender;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.repository.user.UserRepository;
import com.project.schoolmanagment.repository.user.UserRoleRepository;
import com.project.schoolmanagment.service.user.UserRoleService;
import com.project.schoolmanagment.service.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@SpringBootApplication
public class SchoolManagementApplication implements CommandLineRunner {

    private final UserRoleRepository userRoleRepository;

    private final UserRoleService userRoleService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public SchoolManagementApplication(UserRoleRepository userRoleRepository, UserRoleService userRoleService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRoleRepository = userRoleRepository;
        this.userRoleService = userRoleService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(SchoolManagementApplication.class, args);
        //this is our first day
    }

    //This method will run before the SpringBootApplication runs
    @Override
    public void run(String... args) throws Exception {
        if (userRoleService.getAllUserRoles().isEmpty()) {

            UserRole admin = new UserRole();
            admin.setRoleName("Admin");
            admin.setRoleType(RoleType.ADMIN);
            userRoleRepository.save(admin);

            UserRole dean = new UserRole();
            dean.setRoleName("Dean");
            dean.setRoleType(RoleType.MANAGER);
            userRoleRepository.save(dean);

            UserRole viceDean = new UserRole();
            viceDean.setRoleName("ViceDean");
            viceDean.setRoleType(RoleType.ASSISTANT_MANAGER);
            userRoleRepository.save(viceDean);

            UserRole student = new UserRole();
            student.setRoleName("Student");
            student.setRoleType(RoleType.STUDENT);
            userRoleRepository.save(student);

            UserRole teacher = new UserRole();
            teacher.setRoleName("Teacher");
            teacher.setRoleType(RoleType.TEACHER);
            userRoleRepository.save(teacher);
        }
        if (!userRepository.existsByUsername("superAdmin")) {
            User superAdmin = new User();
            superAdmin.setUsername("superAdmin");
            superAdmin.setEmail("superAdmin@gmail.com");
            superAdmin.setSsn("1111-11-1111");
            superAdmin.setPassword(passwordEncoder.encode("superAdmin123"));
            superAdmin.setName("Pars");
            superAdmin.setSurname("Star");
            superAdmin.setPhoneNumber("111-111-1111");
            superAdmin.setGender(Gender.FEMALE);
            superAdmin.setBirthDay(LocalDate.of(1980, 2, 2));
            superAdmin.setBirthPlace("Istanbul");
            superAdmin.setBuiltIn(true);
            superAdmin.setUserRole(userRoleService.getUserRole(RoleType.ADMIN));
            userRepository.save(superAdmin);
        }

    }
}

/*
admin login
{
    "username": "Admin19",
    "password": "Ankara06*"
}
 */







