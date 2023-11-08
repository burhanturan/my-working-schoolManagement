package com.project.schoolmanagment.service.user;

import com.project.schoolmanagment.contactmessage.dto.ResponseMessage;
import com.project.schoolmanagment.entity.conceretes.business.LessonProgram;
import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.payload.mappers.UserMapper;
import com.project.schoolmanagment.payload.messages.SuccessMessages;
import com.project.schoolmanagment.payload.request.user.ChooseLessonTeacherRequest;
import com.project.schoolmanagment.payload.request.user.TeacherRequest;
import com.project.schoolmanagment.payload.response.user.TeacherResponse;
import com.project.schoolmanagment.payload.response.user.UserResponse;
import com.project.schoolmanagment.repository.user.UserRepository;
import com.project.schoolmanagment.service.business.LessonProgramService;
import com.project.schoolmanagment.service.helper.MethodHelper;
import com.project.schoolmanagment.service.validator.DateTimeValidator;
import com.project.schoolmanagment.service.validator.UniquePropertyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final UserRepository userRepository;

    private final UserRoleService userRoleService;

    private final LessonProgramService lessonProgramService;

    private final UniquePropertyValidator uniquePropertyValidator;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final MethodHelper methodHelper;

    private final DateTimeValidator dateTimeValidator;


    public ResponseMessage<TeacherResponse> saveTeacher(TeacherRequest teacherRequest) {
        //validate 1: lesson program set
        Set<LessonProgram> lessonProgramSet = lessonProgramService
                .getLessonProgramById(teacherRequest.getLessonsProgramIdList());
        //validate 2: unique properties
        uniquePropertyValidator.checkDuplicate(teacherRequest.getUsername(),
                teacherRequest.getSsn(),
                teacherRequest.getPhoneNumber(),
                teacherRequest.getEmail());

        // mapping to domain objects
        User teacher = userMapper.mapTeacherRequestToUser(teacherRequest);

        //map missing properties
        teacher.setUserRole(userRoleService.getUserRole(RoleType.TEACHER));
        teacher.setLessonProgramList(lessonProgramSet);
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));

        //isAdvisoryTeacher
        teacher.setIsAdvisor(teacherRequest.getIsAdvisorTeacher());

        User savedTeacher = userRepository.save(teacher);

        return ResponseMessage.<TeacherResponse>builder()
                .message(SuccessMessages.TEACHER_SAVE)
                .object(userMapper.mapUserToTeacherResponse(savedTeacher))
                .build();
    }

    public ResponseMessage<TeacherResponse> updateTeacherForManagers(TeacherRequest teacherRequest, Long userId) {

        //validate if this user exists
        User user = methodHelper.isUserExist(userId);

        //validate the role
        methodHelper.checkRole(user, RoleType.TEACHER);

        //validate lesson program set
        Set<LessonProgram> lessonPrograms = lessonProgramService
                .getLessonProgramById(teacherRequest.getLessonsProgramIdList());

        //validate unique properties
        uniquePropertyValidator.checkUniqueProperties(user, teacherRequest);

        //mapping to the domain objects
        User updatedTeacher = userMapper.mapTeacherRequestToUser(teacherRequest);

        //setting the missing properties
        updatedTeacher.setId(user.getId());
        updatedTeacher.setPassword(passwordEncoder.encode(teacherRequest.getPassword()));
        updatedTeacher.setLessonProgramList(lessonPrograms);
        updatedTeacher.setUserRole(userRoleService.getUserRole(RoleType.TEACHER));

        //saving
        User savedTeacher = userRepository.save(updatedTeacher);

        //returning
        return ResponseMessage.<TeacherResponse>builder()
                .message(SuccessMessages.TEACHER_UPDATE)
                .object(userMapper.mapUserToTeacherResponse(savedTeacher))
                .httpStatus(HttpStatus.OK)
                .build();
    }


    public ResponseMessage<TeacherResponse> addLessonProgram(ChooseLessonTeacherRequest teacherRequest) {
        //validate teacher exists or not
        User teacher = methodHelper.isUserExist(teacherRequest.getTeacherId());

        //check role
        methodHelper.checkRole(teacher, RoleType.TEACHER);

        //validate lesson program
        Set<LessonProgram> lessonPrograms = lessonProgramService
                .getLessonProgramById(teacherRequest.getLessonProgramId());

        Set<LessonProgram> teacherExistingLessonProgram = teacher
                .getLessonProgramList();

        //just validating the new ones
        dateTimeValidator.checkDuplicateLessonPrograms(lessonPrograms);

        teacherExistingLessonProgram.addAll(lessonPrograms);

        //validating all lesson programs
        dateTimeValidator.checkDuplicateLessonPrograms(teacherExistingLessonProgram);

        teacher.setLessonProgramList(teacherExistingLessonProgram);

        User updatedTeacher = userRepository.save(teacher);

        return ResponseMessage.<TeacherResponse>builder()
                .message(SuccessMessages.TEACHER_UPDATE)
                .object(userMapper.mapUserToTeacherResponse(updatedTeacher))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public List<UserResponse> getAllAdvisorTeachers() {
        return userRepository.findAllByAdvisorTeacher(true)
                .stream()
                .map(userMapper::mapUserToUserResponse)
                .collect(Collectors.toList());
    }

    public ResponseMessage<UserResponse> deleteAdvisorTeacherById(Long id) {
        //is user really existing
        User teacher = methodHelper.isUserExist(id);

        //is user really a teacher
        methodHelper.checkRole(teacher, RoleType.TEACHER);

        //is user really an advisorId
        methodHelper.checkAdvisor(teacher);

        teacher.setIsAdvisor(false);
        userRepository.save(teacher);

        List<User> allStudents = userRepository.findByAdvisorTeacherId(id);

        if (!allStudents.isEmpty()) {
            //we need to set this deleted advisor teacher info from students
            allStudents.forEach(student -> student.setAdvisorTeacherId(null));
        }

        return ResponseMessage.<UserResponse>builder()
                .message(SuccessMessages.ADVISOR_TEACHER_DELETE)
                .object(userMapper.mapUserToUserResponse(teacher))
                .httpStatus(HttpStatus.OK)
                .build();
    }


}
