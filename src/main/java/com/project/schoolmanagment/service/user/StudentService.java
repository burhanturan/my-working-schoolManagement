package com.project.schoolmanagment.service.user;

import com.project.schoolmanagment.contactmessage.dto.ResponseMessage;
import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.payload.mappers.UserMapper;
import com.project.schoolmanagment.payload.messages.SuccessMessages;
import com.project.schoolmanagment.payload.request.user.StudentRequest;
import com.project.schoolmanagment.payload.response.user.StudentResponse;
import com.project.schoolmanagment.repository.user.UserRepository;
import com.project.schoolmanagment.service.helper.MethodHelper;
import com.project.schoolmanagment.service.validator.UniquePropertyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final UserRepository userRepository;

    private final MethodHelper methodHelper;

    private final UniquePropertyValidator uniquePropertyValidator;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final UserRoleService userRoleService;

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
}
