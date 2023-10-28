package com.project.schoolmanagment.entity.conceretes.business;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lessonId;

    private String lessonName;

    private Integer creditScore;

    private Boolean isCompulsory;

    @ManyToMany(mappedBy ="lessons",cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<LessonProgram> lessonPrograms;



}
