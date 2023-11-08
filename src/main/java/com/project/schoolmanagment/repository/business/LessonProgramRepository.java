package com.project.schoolmanagment.repository.business;

import com.project.schoolmanagment.entity.conceretes.business.LessonProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface LessonProgramRepository extends JpaRepository<LessonProgram, Long> {

    //@Query("SELECT LessonProgram  FROM LessonProgram l WHERE l.users = null ")
    List<LessonProgram> findByUsers_IdNull();
    List<LessonProgram> findByUsers_IdNotNull();

    @Query("SELECT l  FROM LessonProgram l WHERE l.id IN :idList")
    Set<LessonProgram> getLessonProgramByUsersUsername(Set<Long> idList);

}
