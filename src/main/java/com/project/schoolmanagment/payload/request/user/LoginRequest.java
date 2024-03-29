package com.project.schoolmanagment.payload.request.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotNull(message = "Username must not be null.")
    private String username;

    @NotNull(message = "Password must not be null.")
    private String password;

}
