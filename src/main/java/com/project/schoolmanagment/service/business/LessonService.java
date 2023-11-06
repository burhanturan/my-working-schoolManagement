package com.project.schoolmanagment.service.business;


import com.project.schoolmanagment.contactmessage.dto.ResponseMessage;
import com.project.schoolmanagment.entity.conceretes.business.Lesson;
import com.project.schoolmanagment.exception.ConflictException;
import com.project.schoolmanagment.exception.ResourceNotFoundException;
import com.project.schoolmanagment.payload.mappers.LessonMapper;
import com.project.schoolmanagment.payload.messages.ErrorMessages;
import com.project.schoolmanagment.payload.messages.SuccessMessages;
import com.project.schoolmanagment.payload.request.business.LessonRequest;
import com.project.schoolmanagment.payload.response.business.LessonResponse;
import com.project.schoolmanagment.repository.business.LessonRepository;
import com.project.schoolmanagment.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    private final LessonMapper lessonMapper;

    private final PageableHelper pageableHelper;


    /**
     * @param lessonRequest DTO to save a lesson
     * @return response message with the lessonResponse DTO
     */
    public ResponseMessage<LessonResponse> saveLesson(LessonRequest lessonRequest) {
        //only one lesson should exist according to the lesson name
        isLessonExistByLessonName(lessonRequest.getLessonName());

        //mapper + saving
        Lesson savedLesson = lessonRepository.save(lessonMapper.mapLessonRequestToLesson(lessonRequest));

        return ResponseMessage.<LessonResponse>builder()
                .message(SuccessMessages.LESSON_SAVE)
                .object(lessonMapper.mapLessonToLessonResponse(savedLesson))
                .httpStatus(HttpStatus.CREATED)
                .build();

        //1,03.18

    }

    /**
     * exception handler method for lesson name
     *
     * @param lessonName to search
     * @return true if lesson does not exist.
     */
    private boolean isLessonExistByLessonName(String lessonName) {
        boolean lessonExist = lessonRepository.existsByLessonNameEqualsIgnoreCase(lessonName);
        if (lessonExist) {
            throw new ConflictException(String.format(ErrorMessages.ALREADY_REGISTER_LESSON_MESSAGE, lessonName));
        } else {
            return true;
        }
    }

    private Lesson isLessonExistById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.NOT_FOUND_LESSON_MESSAGE));
    }

    public ResponseMessage deleteLessonById(Long id) {
        isLessonExistById(id);
        lessonRepository.deleteById(id);
        return ResponseMessage.builder()
                .message(SuccessMessages.LESSON_DELETE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public ResponseMessage getLessonByName(String lessonName) {
        if (lessonRepository.getLessonByLessonNameIgnoreCase(lessonName).isPresent()) {
            Lesson lesson = lessonRepository.getLessonByLessonNameIgnoreCase(lessonName).get();
            return ResponseMessage.<LessonResponse>builder()
                    .message(SuccessMessages.LESSON_FOUND)
                    .object(lessonMapper.mapLessonToLessonResponse(lesson))
                    .httpStatus(HttpStatus.OK)
                    .build();
        } else {
            return ResponseMessage.<LessonResponse>builder()
                    .message(String.format(ErrorMessages.NOT_FOUND_LESSON_MESSAGE, lessonName))
                    .build();
        }
    }


    public Page<LessonResponse> getLEssonByPage(int page, int size, String sort, String type) {

        Pageable pageableWithProperties = pageableHelper.getPageableWithProperties(page, size, sort, type);
        return lessonRepository.findAll(pageableWithProperties)
                .map(lessonMapper::mapLessonToLessonResponse);
    }

    public Set<Lesson> getAllLessonByLessonId(Set<Long> idSet) {
        return idSet.stream()
                //this represent this class's method
                //isLessonExistById method is in this class
                .map(this::isLessonExistById)
                //.map(lessonMapper::mapLessonToLessonResponse)
                .collect(Collectors.toSet());
    }


    public LessonResponse updateLesson(Long lessonId, LessonRequest lessonRequest) {
        //validation 1: is lesson existing?
        Lesson lesson = isLessonExistById(lessonId);
        //validation 2: if you are updating lesson name, is this exists in DB ?

        //step 1: are you really changing the name of the lesson?
        //step 2: if the step 1 is the case, is this lesson name in DB or not
        if (!lesson.getLessonName().equals(lessonRequest.getLessonName())
                && lessonRepository.existsByLessonNameEqualsIgnoreCase(lessonRequest.getLessonName())
        ) {
            throw new ConflictException(String.format(ErrorMessages.ALREADY_REGISTER_LESSON_MESSAGE, lessonRequest.getLessonName()));
        }

        Lesson updatedLesson = lessonMapper.mapLessonRequestToLesson(lessonRequest);

        //Lesson programs is not suitable for putting them in mappers
        //because while we are creating a lesson we are not specifying the lesson programs
        updatedLesson.setLessonPrograms(lesson.getLessonPrograms());
        updatedLesson.setLessonId(lesson.getLessonId());

        Lesson savedLesson = lessonRepository.save(updatedLesson);

        return lessonMapper.mapLessonToLessonResponse(savedLesson);
    }


    public ResponseMessage<LessonResponse> getLessonById(Long id) {
        Lesson lesson = isLessonExistById(id);

        return ResponseMessage.<LessonResponse>builder()
                .message(SuccessMessages.LESSON_FOUND)
                .object(lessonMapper.mapLessonToLessonResponse(lesson))
                .httpStatus(HttpStatus.OK)
                .build();
    }
}
