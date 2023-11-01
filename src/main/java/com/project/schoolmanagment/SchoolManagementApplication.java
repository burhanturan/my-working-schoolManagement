package com.project.schoolmanagment;

import com.project.schoolmanagment.entity.conceretes.user.UserRole;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.repository.user.UserRoleRepository;
import com.project.schoolmanagment.service.user.UserRoleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SchoolManagementApplication implements CommandLineRunner {

    private final UserRoleRepository userRoleRepository;

    private final UserRoleService userRoleService;

    public SchoolManagementApplication(UserRoleRepository userRoleRepository, UserRoleService userRoleService) {
        this.userRoleRepository = userRoleRepository;
        this.userRoleService = userRoleService;
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
    }
}









