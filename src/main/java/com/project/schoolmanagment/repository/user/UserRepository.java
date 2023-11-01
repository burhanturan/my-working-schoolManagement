package com.project.schoolmanagment.repository.user;

import com.project.schoolmanagment.entity.conceretes.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsBySsn(String ssn);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.userRole.roleName = :userRole")
    Page<User> findByUserRole(String userRole, Pageable pageable);

}
