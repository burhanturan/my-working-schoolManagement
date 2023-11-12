package com.project.schoolmanagment.service.business;

import com.project.schoolmanagment.contactmessage.dto.ResponseMessage;
import com.project.schoolmanagment.entity.conceretes.business.Meet;
import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.entity.enums.RoleType;
import com.project.schoolmanagment.exception.ConflictException;
import com.project.schoolmanagment.payload.mappers.MeetingMapper;
import com.project.schoolmanagment.payload.messages.ErrorMessages;
import com.project.schoolmanagment.payload.messages.SuccessMessages;
import com.project.schoolmanagment.payload.request.business.MeetingRequest;
import com.project.schoolmanagment.payload.response.business.MeetingResponse;
import com.project.schoolmanagment.repository.business.MeetingRepository;
import com.project.schoolmanagment.service.helper.MethodHelper;
import com.project.schoolmanagment.service.user.UserService;
import com.project.schoolmanagment.service.validator.DateTimeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    private final UserService userService;

    private final MethodHelper methodHelper;

    private final DateTimeValidator dateTimeValidator;

    private final MeetingMapper meetingMapper;

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

    //    private void checkMeetingConflicts(Long userId, LocalDate date, LocalTime startTime, LocalTime stopTime) {
//        List<Meet> meets;
//
//        //try to understand if it is student or teacher and get all meetings
//        if (Boolean.TRUE.equals(methodHelper.isUserExist(userId).getIsAdvisor())) {
//            meets = meetingRepository.findByAdvisoryTeacher_IdEquals(userId);
//        } else {
//            meets = meetingRepository.findByStudentList_IdEquals(userId);
//        }
//
//        //conflict validation
//        for (Meet meet : meets) {
//            LocalTime existingStartTime = meet.getStartTime();
//            LocalTime existingStopTime = meet.getStopTime();
//            if (meet.getDate().equals(date) && (
//                    startTime.isAfter(existingStartTime) && startTime.isBefore(existingStopTime) ||
//                            startTime.isAfter(existingStartTime) && stopTime.isBefore(existingStopTime) ||
//                            startTime.isBefore(existingStartTime) && stopTime.isAfter(existingStopTime) ||
//                            startTime.equals(existingStartTime) && stopTime.equals(existingStopTime)
//            )) {
//                throw new ConflictException(ErrorMessages.MEET_HOURS_CONFLICT);
//            }
//        }
//
//    }

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


}
