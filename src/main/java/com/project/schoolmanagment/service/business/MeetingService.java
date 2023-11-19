package com.project.schoolmanagment.service.business;

import com.project.schoolmanagment.contactmessage.dto.ResponseMessage;
import com.project.schoolmanagment.contactmessage.exception.BadRequestException;
import com.project.schoolmanagment.entity.conceretes.business.Meet;
import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.exception.ConflictException;
import com.project.schoolmanagment.exception.ResourceNotFoundException;
import com.project.schoolmanagment.payload.mappers.MeetingMapper;
import com.project.schoolmanagment.payload.messages.ErrorMessages;
import com.project.schoolmanagment.payload.messages.SuccessMessages;
import com.project.schoolmanagment.payload.request.business.MeetingRequest;
import com.project.schoolmanagment.payload.response.business.MeetingResponse;
import com.project.schoolmanagment.repository.business.MeetingRepository;
import com.project.schoolmanagment.service.helper.MethodHelper;
import com.project.schoolmanagment.service.helper.PageableHelper;
import com.project.schoolmanagment.service.user.UserService;
import com.project.schoolmanagment.service.validator.DateTimeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    private final UserService userService;

    private final MethodHelper methodHelper;

    private final DateTimeValidator dateTimeValidator;

    private final MeetingMapper meetingMapper;

    private final PageableHelper pageableHelper;

    public ResponseMessage<MeetingResponse> saveMeeting(HttpServletRequest request, MeetingRequest meetingRequest) {
        String username = (String) request.getAttribute("username");

        //validate username
        User advisorTeacher = methodHelper.isUserExistByUsername(username);

        //validate is advisorTeacher or not
        methodHelper.checkAdvisor(advisorTeacher);

        dateTimeValidator.checkTimeWithException(meetingRequest.getStartTime(), meetingRequest.getStopTime());

        checkMeetConflict(advisorTeacher.getId(),
                meetingRequest.getDate(),
                meetingRequest.getStartTime(),
                meetingRequest.getStopTime());

        List<User> students = userService.findUsersByIdArray(meetingRequest.getStudentIds());
        //validate are they really students or not
        for (User student : students) {
            methodHelper.checkRole(student, RoleType.STUDENT);
        }
        Meet meet = meetingMapper.mapMeetRequestToMeet(meetingRequest);
        meet.setStudentList(students);
        meet.setAdvisoryTeacher(advisorTeacher);
        Meet savedMeeting = meetingRepository.save(meet);

        return ResponseMessage.<MeetingResponse>builder()
                .message(SuccessMessages.MEET_SAVE)
                .object(meetingMapper.mapMeetToMeetingResponse(savedMeeting))
                .httpStatus(HttpStatus.OK)
                .build();

    }

    private void checkMeetConflict(Long userId, LocalDate date, LocalTime startTime, LocalTime stopTime) {
        List<Meet> meets;

        //try to understand if it is student or teacher and get all meetings
        if (Boolean.TRUE.equals(methodHelper.isUserExist(userId).getIsAdvisor())) { // condition : userService.getUserByUserId(userId).getIsAdvisor()
            meets = meetingRepository.findByAdvisoryTeacher_IdEquals(userId);
        } else meets = meetingRepository.findByStudentList_IdEquals(userId);

        // !!! cakısma kontrolu
        for (Meet meet : meets) {
            LocalTime existingStartTime = meet.getStartTime();
            LocalTime existingStopTime = meet.getStopTime();

            if (meet.getDate().equals(date) &&
                    ((startTime.isAfter(existingStartTime) && startTime.isBefore(existingStopTime)) ||
                            (stopTime.isAfter(existingStartTime) && stopTime.isBefore(existingStopTime)) ||
                            (startTime.isBefore(existingStartTime) && stopTime.isAfter(existingStopTime)) ||
                            (startTime.equals(existingStartTime) || stopTime.equals(existingStopTime))
                    )
            ) {
                throw new ConflictException(ErrorMessages.MEET_HOURS_CONFLICT);
            }
        }
    }


    public List<MeetingResponse> getALl() {
        return meetingRepository.findAll().stream()
                .map(meetingMapper::mapMeetToMeetingResponse)
                .collect(Collectors.toList());
    }

    public ResponseMessage<MeetingResponse> getMeetingById(Long id) {
        return ResponseMessage.<MeetingResponse>builder()
                .message(SuccessMessages.MEET_FOUND)
                .object(meetingMapper.mapMeetToMeetingResponse(isMeetExistById(id)))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    private Meet isMeetExistById(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ErrorMessages.MEET_NOT_FOUND_MESSAGE, id)));
    }


    public ResponseMessage deleteById(Long id) {
        isMeetExistById(id);

        meetingRepository.deleteById(id);
        return ResponseMessage.builder()
                .message(SuccessMessages.MEET_DELETE)
                .httpStatus(HttpStatus.OK)
                .build();
    }

    public ResponseMessage<MeetingResponse> updateMeeting(Long meetingId, MeetingRequest meetingRequest, HttpServletRequest request) {
        Meet meet = isMeetExistById(meetingId);
        //validate is the teacher is updating own meeting or not
        isMeetingAssignToThisTeacher(meet, request);

        //validate the time
        dateTimeValidator.checkTimeWithException(meetingRequest.getStartTime(), meetingRequest.getStopTime());

        if (meet.getDate().equals(meetingRequest.getDate())
                && meet.getStartTime().equals(meetingRequest.getStartTime())
                && meet.getStopTime().equals(meetingRequest.getStopTime())) {

            //TODO
            //bug has been found when user try to update only student info it throws time exception

            //conflict related to students
            for (Long studentId : meetingRequest.getStudentIds()) {
                checkMeetConflict(studentId,
                        meetingRequest.getDate(),
                        meetingRequest.getStartTime(),
                        meetingRequest.getStopTime());
            }
            //conflict related to teachers
            checkMeetConflict(meet.getAdvisoryTeacher().getId(),
                    meetingRequest.getDate(),
                    meetingRequest.getStartTime(),
                    meetingRequest.getStopTime());

        }
        List<User> students = userService.findUsersByIdArray(meetingRequest.getStudentIds());

        Meet updateMeet = meetingMapper.mapMeetUpdateRequestToMeet(meetingRequest, meetingId);

        //Set missing properties
        updateMeet.setStudentList(students);
        updateMeet.setAdvisoryTeacher(meet.getAdvisoryTeacher());
        Meet savedMeet = meetingRepository.save(updateMeet);

        return ResponseMessage.<MeetingResponse>builder()
                .message(SuccessMessages.MEET_UPDATE)
                .object(meetingMapper.mapMeetToMeetingResponse(savedMeet))
                .httpStatus(HttpStatus.OK)
                .build();
    }

    private void isMeetingAssignToThisTeacher(Meet meet, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        User user = methodHelper.isUserExistByUsername(username);
        if (user.getUserRole().getRoleType().getName().equals("Teacher") &&
                (meet.getAdvisoryTeacher().getAdvisorTeacherId() != (user.getId()))) {
            throw new BadRequestException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
        }
    }


    public ResponseEntity<List<MeetingResponse>> getAllMeetByTeachers(HttpServletRequest request) {
        //Get username from header attributes from logged user
        String username = (String) request.getAttribute("username");

        //Get user from DB
        User advisorTeacher = methodHelper.isUserExistByUsername(username);

        //check the teacher is advisor or not
        methodHelper.checkAdvisor(advisorTeacher);

//        List<MeetingResponse> meetingResponseList =
//                meetingRepository.findByAdvisoryTeacher_IdEquals(advisorTeacher.getId())
//                        .stream()
//                        .map(meetingMapper::mapMeetToMeetingResponse)
//                        .collect(Collectors.toList());
//       return ResponseEntity.ok(meetResponseList);

        List<MeetingResponse> meetResponseList =
                meetingRepository.findAll()
                        .stream()
                        .filter(x -> x.getAdvisoryTeacher().getId() == advisorTeacher.getId())
                        .map(meetingMapper::mapMeetToMeetingResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(meetResponseList);
    }

    public ResponseEntity<List<MeetingResponse>> getAllMeetByStudents(HttpServletRequest request) {
        String username = (String) request.getAttribute("username");

        User student = methodHelper.isUserExistByUsername(username);

        List<MeetingResponse> meetingResponseList =
                meetingRepository.findByStudentList_IdEquals(student.getId())
                        .stream()
                        .map(meetingMapper::mapMeetToMeetingResponse)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(meetingResponseList);


    }


    public Page<MeetingResponse> getAllMeetByPage(int page, int size) {
        Pageable pageable = pageableHelper.getPageableWithProperties(page, size);
        return meetingRepository.findAll(pageable)
                .map(meetingMapper::mapMeetToMeetingResponse);
    }

    public ResponseEntity<Page<MeetingResponse>> getAllMeetByAdvisorAsPage(HttpServletRequest request, int page, int size) {

        String username = (String) request.getAttribute("username");

        User advisorTeacher = methodHelper.isUserExistByUsername(username);

        methodHelper.checkAdvisor(advisorTeacher);

        Pageable pageable = pageableHelper.getPageableWithProperties(page, size);

//        return meetingRepository.findByAdvisoryTeacher_IdEquals(advisorTeacher.getId(), pageable)
//                .map(meetingMapper::mapMeetToMeetingResponse);

        return ResponseEntity
                .ok(
                        (meetingRepository.findByAdvisoryTeacher_IdEquals(advisorTeacher.getId(), pageable)
                                .map(meetingMapper::mapMeetToMeetingResponse))
                );
    }


}
