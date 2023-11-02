package com.project.schoolmanagment.service.helper;

import com.project.schoolmanagment.contactmessage.exception.BadRequestException;
import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.payload.messages.ErrorMessages;
import com.project.schoolmanagment.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MethodHelper {

    private final UserRepository userRepository;

    public void isUserBuiltIn(User user){
        if (user.getBuiltIn()){
            throw new BadRequestException(ErrorMessages.NOT_PERMITTED_METHOD_MESSAGE);
        }
    }

}
