package com.project.schoolmanagment.payload.mappers;

import com.project.schoolmanagment.entity.conceretes.business.EducationTerm;
import com.project.schoolmanagment.entity.conceretes.business.Lesson;
import com.project.schoolmanagment.entity.conceretes.business.LessonProgram;
import com.project.schoolmanagment.payload.request.business.LessonProgramRequest;
import com.project.schoolmanagment.payload.response.business.LessonProgramResponse;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Set;

@Data
@Component
public class LessonProgramMapper {

    public LessonProgram mapLessonProgramRequestToLessonProgram(
            LessonProgramRequest lessonProgramRequest,
            Set<Lesson> lessonSet,
            EducationTerm educationTerm
    ) {
        return LessonProgram.builder()
                .startTime(lessonProgramRequest.getStartTime())
                .stopTime(lessonProgramRequest.getStopTime())
                .day(lessonProgramRequest.getDay())
                .lessons(lessonSet)
                .educationTerm(educationTerm)
                .build();
    }

    public LessonProgramResponse mapLessonProgramToLessonProgramResponse(LessonProgram lessonProgram) {

        return LessonProgramResponse.builder()
                .day(lessonProgram.getDay())
                .startTime(lessonProgram.getStartTime())
                .stopTime(lessonProgram.getStopTime())
                .lessonProgramId(lessonProgram.getLessonProgramId())
                .lessonName(lessonProgram.getLessons())
                .educationTerm(lessonProgram.getEducationTerm())
                .build();
    }


}
