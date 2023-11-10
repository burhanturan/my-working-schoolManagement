package com.project.schoolmanagment.repository.business;

import com.project.schoolmanagment.entity.conceretes.business.StudentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentInfoRepository extends JpaRepository<StudentInfo, Long> {

    //@Query("SELECT s FROM StudentInfo s WHERE User.id IN :studentId")
    List<StudentInfo> getAllByStudentId_Id(Long studentId);

    boolean existsByIdEquals(Long id);

}
