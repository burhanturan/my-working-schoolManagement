package com.project.schoolmanagment.service.business;

import com.project.schoolmanagment.contactmessage.dto.ResponseMessage;
import com.project.schoolmanagment.entity.conceretes.business.EducationTerm;
import com.project.schoolmanagment.entity.conceretes.business.Lesson;
import com.project.schoolmanagment.entity.conceretes.business.StudentInfo;
import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.entity.enums.Note;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.exception.ConflictException;
import com.project.schoolmanagment.exception.ResourceNotFoundException;
import com.project.schoolmanagment.payload.mappers.StudentInfoMapper;
import com.project.schoolmanagment.payload.messages.ErrorMessages;
import com.project.schoolmanagment.payload.messages.SuccessMessages;
import com.project.schoolmanagment.payload.request.business.StudentInfoRequest;
import com.project.schoolmanagment.payload.request.business.UpdateStudentInfoRequest;
import com.project.schoolmanagment.payload.response.business.StudentInfoResponse;
import com.project.schoolmanagment.repository.business.StudentInfoRepository;
import com.project.schoolmanagment.service.helper.MethodHelper;
import com.project.schoolmanagment.service.helper.PageableHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentInfoService {

    private final StudentInfoRepository studentInfoRepository;

    private final MethodHelper methodHelper;

    private final LessonService lessonService;

    private final EducationTermService educationTermService;

    private final StudentInfoMapper studentInfoMapper;

    private final PageableHelper pageableHelper;

    @Value("${midterm.exam.impact.percentage}")
    private Double midtermExamPercentage;
    @Value("${final.exam.impact.percentage}")
    private Double finalExamPercentage;


    public ResponseMessage<StudentInfoResponse> saveStudentInfo(HttpServletRequest request, StudentInfoRequest studentInfoRequest) {

        String teacherUsername = (String) request.getAttribute("username");

        //get student
        User student = methodHelper.isUserExist(studentInfoRequest.getStudentId());

        //validate user is a student or not
        methodHelper.checkRole(student, RoleType.STUDENT);

        //get teacher
        User teacher = methodHelper.isUserExistByUsername(teacherUsername);

        //get lesson
        Lesson lesson = lessonService.isLessonExistById(studentInfoRequest.getLessonId());

        //get educationTerm
        EducationTerm educationTerm = educationTermService.isEducationTermExist(studentInfoRequest.getEducationTermId());


        //Requirements:
        //1. a student may have only one studentInfo related to one lesson
        isDuplicatedLessonAndInfo(studentInfoRequest.getStudentId(), lesson.getLessonName());

        //calculate the average note and get the suitable note
        Double averageNote = calculateAverageNote(
                studentInfoRequest.getMidtermExam(),
                studentInfoRequest.getFinalExam()
        );
        Note note = checkLetterGrade(averageNote);

        //map DTO to domain object
        StudentInfo studentInfo = studentInfoMapper.mapStudentInfoRequestToStudentInfo(
                studentInfoRequest, note, averageNote);

        //set missing properties
        studentInfo.setStudent(student);
        studentInfo.setTeacher(teacher);
        studentInfo.setLesson(lesson);
        studentInfo.setEducationTerm(educationTerm);
        StudentInfo savedStudent = studentInfoRepository.save(studentInfo);
        return ResponseMessage.<StudentInfoResponse>builder()
                .message(SuccessMessages.STUDENT_INFO_SAVE)
                .object(studentInfoMapper.mapStudentInfoToStudentInfoResponse(savedStudent))
                .httpStatus(HttpStatus.CREATED)
                .build();

    }

    private void isDuplicatedLessonAndInfo(Long studentId, String lessonName) {
        boolean isLessonDuplicationExists =
                studentInfoRepository.getAllByStudentId_Id(studentId)
                        .stream()
                        .anyMatch(info -> info.getLesson().getLessonName().equalsIgnoreCase(lessonName));

        if (isLessonDuplicationExists) {
            throw new ConflictException(String.format(ErrorMessages.ALREADY_REGISTER_LESSON_MESSAGE, lessonName));
        }
    }

    private Double calculateAverageNote(Double midtermExam, Double finalExam) {
        return (midtermExam * midtermExamPercentage + finalExam * finalExamPercentage);
    }

    private Note checkLetterGrade(Double average) {

        if (average < 50.0) {
            return Note.FF;
        } else if (average < 60) {
            return Note.DD;
        } else if (average < 65) {
            return Note.CC;
        } else if (average < 70) {
            return Note.CB;
        } else if (average < 75) {
            return Note.BB;
        } else if (average < 80) {
            return Note.BA;
        } else {
            return Note.AA;
        }
    }

    public ResponseMessage deleteById(Long studentInfoId) {
        //validate if student info exists
        StudentInfo studentInfo = isStudentInfoExists(studentInfoId);
        studentInfoRepository.deleteById(studentInfoId);
        return ResponseMessage.builder()
                .message(SuccessMessages.STUDENT_INFO_DELETE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    //Practicing purposes
    public StudentInfo isStudentInfoExists(Long id) {
        boolean isExists = studentInfoRepository.existsByIdEquals(id);
        if (!isExists) {
            throw new ResourceNotFoundException(String.format(ErrorMessages.STUDENT_INFO_NOT_FOUND, id));
        } else {
            return studentInfoRepository.findById(id).get();
        }
    }

    public Page<StudentInfoResponse> getStudentInfoByPage(int page, int size, String sort, String type) {
        Pageable pageable = pageableHelper.getPageableWithProperties(page, size, sort, type);
        return studentInfoRepository.findAll(pageable)
                .map(studentInfoMapper::mapStudentInfoToStudentInfoResponse);
    }

    public ResponseMessage<StudentInfoResponse> update(UpdateStudentInfoRequest studentInfoRequest, Long studentInfoId) {
        //validate studentInfo is exists or not
        StudentInfo studentInfo = isStudentInfoExists(studentInfoId);

        //get lesson from update request DTO
        Lesson lesson = lessonService.isLessonExistById(studentInfoRequest.getLessonId());

        //get educationTerm from update request DTO
        EducationTerm educationTerm = educationTermService.isEducationTermExist(studentInfoRequest.getEducationTermId());

        Double averageNote = calculateAverageNote(studentInfoRequest.getMidtermExam(), studentInfoRequest.getFinalExam());

        Note note = checkLetterGrade(averageNote);

        //map DTO to domain object
        StudentInfo updatedStudentInfo = studentInfoMapper
                .mapUpdateStudentInfoRequestToStudentInfo(
                        studentInfoRequest,
                        studentInfoId,
                        lesson,
                        educationTerm,
                        note,
                        averageNote);

        //set missing properties
        updatedStudentInfo.setStudent(studentInfo.getStudent());
        updatedStudentInfo.setTeacher(studentInfo.getTeacher());

        StudentInfo savedStudentInfo = studentInfoRepository.save(updatedStudentInfo);

        return ResponseMessage.<StudentInfoResponse>builder()
                .message(SuccessMessages.STUDENT_INFO_UPDATE)
                .object(studentInfoMapper.mapStudentInfoToStudentInfoResponse(savedStudentInfo))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public Page<StudentInfoResponse> getAllForTeacher(HttpServletRequest request, int page, int size) {

        Pageable pageable = pageableHelper.getPageableWithProperties(page, size);
        String username = (String) request.getAttribute("username");
        User teacher = methodHelper.isUserExistByUsername(username);
        return studentInfoRepository.findByTeacherUsername(username, pageable)
                .map(studentInfoMapper::mapStudentInfoToStudentInfoResponse);
    }

    public Page<StudentInfoResponse> getAllForStudent(HttpServletRequest request, int page, int size) {

        Pageable pageable = pageableHelper.getPageableWithProperties(page, size);
        String username = (String) request.getAttribute("username");
        User teacher = methodHelper.isUserExistByUsername(username);
        return studentInfoRepository.findByStudentUsername(username, pageable)
                .map(studentInfoMapper::mapStudentInfoToStudentInfoResponse);
    }

    public List<StudentInfoResponse> getStudentInfoByStudentId(Long studentId) {
        //Validate is exists
        User student = methodHelper.isUserExist(studentId);
        //Validate is really student
        methodHelper.checkRole(student, RoleType.STUDENT);

        if (!studentInfoRepository.existsByStudentId(studentId)) {
            throw new ResourceNotFoundException(
                    String.format(ErrorMessages.STUDENT_INFO_NOT_FOUND_BY_STUDENT_ID, studentId));
        }
        return studentInfoRepository.getAllByStudentId_Id(studentId).stream()
                .map(studentInfoMapper::mapStudentInfoToStudentInfoResponse)
                .collect(Collectors.toList());
    }

    public StudentInfoResponse findById(Long id) {
        return studentInfoMapper.mapStudentInfoToStudentInfoResponse(isStudentInfoExists(id));
    }
}
