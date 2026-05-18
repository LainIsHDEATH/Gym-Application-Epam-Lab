package ua.ivan.epam.gym.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.ivan.epam.gym.application.dto.response.TraineeShortResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerShortResponse;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;

@Mapper(
        componentModel = "spring",
        uses = {
                TrainingTypeMapper.class
        }
)
public interface ShortResponseMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    TraineeShortResponse toTraineeShortResponse(Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization")
    TrainerShortResponse toTrainerShortResponse(Trainer trainer);
}