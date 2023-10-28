package com.project.schoolmanagment.entity.conceretes.business;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.entity.enums.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.EnumSet;
import java.util.function.DoubleBinaryOperator;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer absentee;

    private Double midtermExam;

    private Double finalExam;

    private Double examAverage;

    private String infoNote;

    @Enumerated(EnumType.STRING)
    private Note letterGrade;

    @ManyToOne
    @JsonIgnore
    private User teacher;

    @ManyToOne
    @JsonIgnore
    private User student;

    @ManyToOne
    @JsonIgnore
    private Lesson lesson;

    @OneToOne
    @JsonIgnore
    private EducationTerm educationTerm;


}
