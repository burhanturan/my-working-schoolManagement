package com.project.schoolmanagment.repository.user;

import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.entity.enums.RoleType;
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

    @Query(value = "SELECT (count (u)>0) FROM User u WHERE u.userRole.roleType = ?1")
    boolean findUsersByRole(RoleType roleType);

    @Query(value = "SELECT MAX(u.studentNumber) FROM User u")
    int getMaxStudentNumber();

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')) AND u.userRole.roleType = 'STUDENT'")
    List<User> findAllByUsernameAndRoleType(String username);

    //to find the users by given id array
    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    List<User> findUsersByIdArray(Long[] userIds);

    @Query("SELECT u FROM User u WHERE u.id = ?1 AND u.userRole.roleType = 'STUDENT'")
    User findByIdAndRoleType(Long id);

    @Query("SELECT u FROM User u WHERE u.isActive =?1")
    List<User>findAllByIsActive(boolean isActive);

}
