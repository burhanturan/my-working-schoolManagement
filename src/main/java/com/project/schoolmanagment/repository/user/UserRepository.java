package com.project.schoolmanagment.repository.user;

import com.project.schoolmanagment.entity.conceretes.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsBySsn(String ssn);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.userRole.roleName = :userRole")
    Page<User> findByUserRole(String userRole, Pageable pageable);

    List<User> getUserByNameContaining(String userName);

    User findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.isAdvisor =?1")
    List<User> findAllByAdvisorTeacher(boolean isAdvisor);

    List<User> findByAdvisorTeacherId(Long id);

}
