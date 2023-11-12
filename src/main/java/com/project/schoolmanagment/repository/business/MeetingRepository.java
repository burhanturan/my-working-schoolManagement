package com.project.schoolmanagment.repository.business;

import com.project.schoolmanagment.entity.conceretes.business.Meet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meet, Long> {

    List<Meet>findByStudentList_IdEquals(Long studentId);

    List<Meet>findByAdvisoryTeacher_IdEquals(Long teacherId);

    Page<Meet> findByAdvisoryTeacher_IdEquals(Long userId, Pageable pageable);


}
