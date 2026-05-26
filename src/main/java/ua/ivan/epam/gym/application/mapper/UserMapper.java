package ua.ivan.epam.gym.application.mapper;

import org.mapstruct.Mapper;
import ua.ivan.epam.gym.application.dto.response.RegistrationResponse;
import ua.ivan.epam.gym.application.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    RegistrationResponse toRegistrationResponse(User user);
}
