package com.wasiq.inmobiliaria.auth.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {

    //* Solo se utilizará password y numero
    /*

    private String firstName;
    private String lastName;
    private String email;

     */
    private String password;
    private String phoneNumber;


}
