package com.project.schoolmanagment.service.user;

import com.project.schoolmanagment.contactmessage.dto.ResponseMessage;
import com.project.schoolmanagment.entity.conceretes.business.LessonProgram;
import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.exception.ResourceNotFoundException;
import com.project.schoolmanagment.payload.mappers.UserMapper;
import com.project.schoolmanagment.payload.messages.ErrorMessages;
import com.project.schoolmanagment.payload.messages.SuccessMessages;
import com.project.schoolmanagment.payload.request.user.ChooseLessonProgramWithId;
import com.project.schoolmanagment.payload.request.user.StudentRequest;
import com.project.schoolmanagment.payload.request.user.StudentRequestWithoutPassword;
import com.project.schoolmanagment.payload.response.user.StudentResponse;
import com.project.schoolmanagment.repository.user.UserRepository;
import com.project.schoolmanagment.service.business.LessonProgramService;
import com.project.schoolmanagment.service.helper.MethodHelper;
import com.project.schoolmanagment.service.helper.PageableHelper;
import com.project.schoolmanagment.service.validator.DateTimeValidator;
import com.project.schoolmanagment.service.validator.UniquePropertyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final UserRepository userRepository;

    private final MethodHelper methodHelper;

    private final UniquePropertyValidator uniquePropertyValidator;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final UserRoleService userRoleService;

    private final LessonProgramService lessonProgramService;

    private final DateTimeValidator dateTimeValidator;

    private final PageableHelper pageableHelper;

    public ResponseMessage<StudentResponse> saveStudent(StudentRequest studentRequest) {
        //check DB is this user (advisorTeacher) really exists or not
        User advisorTeacher = methodHelper.isUserExist(studentRequest.getAdvisorTeacherId());

        //check DB is t his user really an advisor teacher
        methodHelper.checkAdvisor(advisorTeacher);

        //validate unique property
        uniquePropertyValidator.checkDuplicate(
                studentRequest.getUsername(),
                studentRequest.getSsn(),
                studentRequest.getPhoneNumber(),
                studentRequest.getEmail());

        //mapping to domain objects
        User student = userMapper.mapStudentRequestToUser(studentRequest);
        student.setAdvisorTeacherId(studentRequest.getAdvisorTeacherId());
        student.setPassword(passwordEncoder.encode(studentRequest.getPassword()));
        student.setUserRole(userRoleService.getUserRole(RoleType.STUDENT));
        student.setActive(true);
        student.setIsAdvisor(false);

        //student numbers starts with 1000
        //each student has his/her own number
        student.setStudentNumber(getLastNumber());

        User savedStudent = userRepository.save(student);

        return ResponseMessage.<StudentResponse>builder()
                .message(SuccessMessages.STUDENT_SAVE)
                .object(userMapper.mapUserToStudentResponse(savedStudent))
                .build();

    }

    private int getLastNumber() {
        if (!userRepository.findUsersByRole(RoleType.STUDENT)) {
            //in case of fist student
            return 1000;
        }
        return userRepository.getMaxStudentNumber() + 1;
    }

    public ResponseEntity<String> updateStudentWithoutPassword(StudentRequestWithoutPassword studentRequestWithoutPassword, HttpServletRequest request) {
        String userName = (String) request.getAttribute("username");

        //fetch user information from DB
        User student = userRepository.findByUsername(userName);

        //validate props for uniqueness
        uniquePropertyValidator.checkUniqueProperties(student, studentRequestWithoutPassword);

        //ordinary way of mapping
        student.setUsername(studentRequestWithoutPassword.getUsername());
        student.setMotherName(studentRequestWithoutPassword.getMotherName());
        student.setFatherName(studentRequestWithoutPassword.getFatherName());
        student.setBirthDay(studentRequestWithoutPassword.getBirthDay());
        student.setEmail(studentRequestWithoutPassword.getEmail());
        student.setPhoneNumber(studentRequestWithoutPassword.getPhoneNumber());
        student.setBirthPlace(studentRequestWithoutPassword.getBirthPlace());
        student.setGender(studentRequestWithoutPassword.getGender());
        student.setName(studentRequestWithoutPassword.getName());
        student.setSurname(studentRequestWithoutPassword.getSurname());
        student.setSsn(studentRequestWithoutPassword.getSsn());

        userRepository.save(student);
        return ResponseEntity.ok(SuccessMessages.STUDENT_UPDATE);
    }


    public ResponseMessage<StudentResponse> updateStudentForManager(Long id, StudentRequest studentRequest) {

        //validate if we have this id in DB
        User student = methodHelper.isUserExist(id);

        //validate if this is really a student
        methodHelper.checkRole(student, RoleType.STUDENT);

        //validate unique properties
        uniquePropertyValidator.checkUniqueProperties(student, studentRequest);

        //mapping to domain objects
        User studentFromMapper = userMapper.mapStudentRequestToUpdatedUser(studentRequest, id);

        //mapping the rest of the properties
        studentFromMapper.setPassword(passwordEncoder.encode(studentRequest.getPassword()));
        studentFromMapper.setAdvisorTeacherId(studentRequest.getAdvisorTeacherId());

        //we do not let the user update student number
        //as we do not have this prop in student request DTO
        studentFromMapper.setStudentNumber(student.getStudentNumber());

        studentFromMapper.setUserRole(userRoleService.getUserRole(RoleType.STUDENT));
        studentFromMapper.setActive(true);

        return ResponseMessage.<StudentResponse>builder()
                .message(SuccessMessages.STUDENT_UPDATE)
                .object(userMapper.mapUserToStudentResponse(userRepository.save(studentFromMapper)))
                .httpStatus(HttpStatus.OK)
                .build();
    }


    public ResponseMessage<StudentResponse> addLessonProgram(ChooseLessonProgramWithId chooseLessonProgramWithId, HttpServletRequest request) {
        String userName = (String) request.getAttribute("username");

        User student = methodHelper.isUserExistByUsername(userName);

        Set<LessonProgram> lessonProgramSet = lessonProgramService.getLessonProgramById(chooseLessonProgramWithId.getLessonProgramId());

        Set<LessonProgram> lessonProgramsFromUserDB = student.getLessonProgramList();

        //we are merging the lesson programs
        lessonProgramsFromUserDB.addAll(lessonProgramSet);

        dateTimeValidator.checkDuplicateLessonPrograms(lessonProgramsFromUserDB);

        //if we don't get any exceptions we should set lessonProgramList to user
        student.setLessonProgramList(lessonProgramsFromUserDB);

        User updateStudent = userRepository.save(student);

        return ResponseMessage.<StudentResponse>builder()
                .message(SuccessMessages.LESSON_PROGRAM_ADD_TO_STUDENT)
                .object(userMapper.mapUserToStudentResponse(updateStudent))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public ResponseMessage changeStatusOfStudent(Long id, boolean status) {

        User student = methodHelper.isUserExist(id);

        methodHelper.checkRole(student, RoleType.STUDENT);

        student.setActive(status);

        userRepository.save(student);

        return ResponseMessage.builder()
                .message("Student is: " + (status ? "active" : "passive")) //ternary
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public Page<StudentResponse> getAllStudentByPage(int page, int size, String sort, String type, String userRole) {
        Pageable pageable = pageableHelper.getPageableWithProperties(page, size, sort, type);
        return userRepository.findByUserRole(userRole, pageable)
                .map(userMapper::mapUserToStudentResponse);
    }

    public List<StudentResponse> getAllStudentByUsernameContains(String username) {
        //List<User> savedStudents = new ArrayList<>();
        List<User> users = userRepository.findAllByUsernameAndRoleType(username);

        if (users.isEmpty()) {
            throw new ResourceNotFoundException(String.format(ErrorMessages.NOT_FOUND_USERNAME_MESSAGE, username));
        }

        return users.stream()
                .map(userMapper::mapUserToStudentResponse)
                .collect(Collectors.toList());
    }

    public com.project.schoolmanagment.payload.response.abstracts.ResponseMessage<StudentResponse> getStudentById(Long id) {
        User student = methodHelper.isUserExist(id);
        User existStudent = userRepository.findByIdAndRoleType(id);
        return com.project.schoolmanagment.payload.response.abstracts.ResponseMessage.<StudentResponse>builder()
                .object(userMapper.mapUserToStudentResponse(existStudent))
                .message(SuccessMessages.STUDENT_FOUND)
                .build();

    }

    public List<StudentResponse> getAllStudentByList(boolean isActive) {
        List<User> users = userRepository.findAllByIsActive(isActive);
        return users.stream()
                .map(userMapper::mapUserToStudentResponse)
                .collect(Collectors.toList());
    }

}
