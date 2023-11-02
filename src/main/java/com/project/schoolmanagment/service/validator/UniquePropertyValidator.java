package com.project.schoolmanagment.service.validator;

import com.project.schoolmanagment.entity.conceretes.user.User;
import com.project.schoolmanagment.exception.ConflictException;
import com.project.schoolmanagment.payload.messages.ErrorMessages;
import com.project.schoolmanagment.payload.request.abstracts.AbstractUserRequest;
import com.project.schoolmanagment.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniquePropertyValidator {

    private final UserRepository userRepository;

    public void checkDuplicate(String username, String ssn, String phoneNumber, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException(String.format(ErrorMessages.ALREADY_REGISTER_MESSAGE_USERNAME,username));
        }

        if (userRepository.existsBySsn(ssn)) {
            throw new ConflictException(String.format(ErrorMessages.ALREADY_REGISTER_MESSAGE_SSN,ssn));
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ConflictException(String.format(ErrorMessages.ALREADY_REGISTER_MESSAGE_PHONE_NUMBER,phoneNumber));
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(String.format(ErrorMessages.ALREADY_REGISTER_MESSAGE_EMAIL,email));
        }
    }

    public void checkUniqueProperties(User user, AbstractUserRequest abstractUserRequest) {
         String updatedUsername = "";
         String updatedSsn = "";
         String updatedPhone = "";
         String updatedEmail = "";
         boolean isChanged = false;

         //this verifies that we have changed the values
         if (!user.getUsername().equalsIgnoreCase(abstractUserRequest.getUsername())){
             updatedUsername = abstractUserRequest.getUsername();
             isChanged =  true;
         }

         if (!user.getSsn().equalsIgnoreCase(abstractUserRequest.getSsn())){
             updatedSsn = abstractUserRequest.getSsn();
             isChanged =  true;
         }

         if (!user.getPhoneNumber().equalsIgnoreCase(abstractUserRequest.getPhoneNumber())){
             updatedPhone = abstractUserRequest.getPhoneNumber();
             isChanged =  true;
         }

         if (!user.getEmail().equalsIgnoreCase(abstractUserRequest.getEmail())){
             updatedEmail = abstractUserRequest.getEmail();
             isChanged =  true;
         }

         if (isChanged){
             checkDuplicate(updatedUsername, updatedSsn, updatedPhone, updatedEmail);
         }


    }


}
