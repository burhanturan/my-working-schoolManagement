package com.project.schoolmanagment.payload.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordUpdateRequest {

    @NotNull(message = "Please enter your old password")
    private String oldPassword;

    @NotNull(message = "Please enter your new password")
    private String newPassword;


}
