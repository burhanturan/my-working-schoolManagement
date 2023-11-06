package com.project.schoolmanagment.repository.business;

import com.project.schoolmanagment.entity.conceretes.business.LessonProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonProgramRepository extends JpaRepository<LessonProgram, Long> {

    //@Query("select LessonProgram from LessonProgram lp where lp.users is null")
    List<LessonProgram> findByUsers_IdNull();
    List<LessonProgram> findByUsers_IdNotNull();

}
