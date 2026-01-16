package com.wasiq.inmobiliaria.application.ports.input;

import com.wasiq.inmobiliaria.application.ports.input.command.RegisterUserCommand;
import com.wasiq.inmobiliaria.domain.model.user.User;

public interface RegisterUserUseCase {

    User registerUser(RegisterUserCommand command);
}
