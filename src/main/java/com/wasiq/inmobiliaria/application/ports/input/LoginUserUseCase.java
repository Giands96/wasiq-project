package com.wasiq.inmobiliaria.application.ports.input;

import com.wasiq.inmobiliaria.application.ports.input.command.LoginUserCommand;
import com.wasiq.inmobiliaria.domain.model.user.User;

public interface LoginUserUseCase {
    String login(LoginUserCommand command);
}
