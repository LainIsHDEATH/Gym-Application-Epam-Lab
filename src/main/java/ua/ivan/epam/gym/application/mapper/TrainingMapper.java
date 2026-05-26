package ua.ivan.epam.gym.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.ivan.epam.gym.application.dto.response.TraineeTrainingResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerTrainingResponse;
import ua.ivan.epam.gym.application.model.Training;

@Mapper(
        componentModel = "spring",
        uses = {
                TrainingTypeMapper.class
        }
)
public interface TrainingMapper {

    @Mapping(target = "trainerName", source = "trainer.user.username")
    TraineeTrainingResponse toTraineeTrainingResponse(Training training);

    @Mapping(target = "traineeName", source = "trainee.user.username")
    TrainerTrainingResponse toTrainerTrainingResponse(Training training);
}
