package com.project.schoolmanagment.repository.business;

import com.project.schoolmanagment.entity.conceretes.business.EducationTerm;
import com.project.schoolmanagment.entity.enums.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationTermRepository extends JpaRepository<EducationTerm, Long> {

    //?1 means first parameter and second parameter is ?2
    @Query("SELECT (count(e)>0) FROM EducationTerm e WHERE e.term = ?1 and extract(year from e.startDate) = ?2")
    boolean existsByTermAndYear(Term term, int year);

}
