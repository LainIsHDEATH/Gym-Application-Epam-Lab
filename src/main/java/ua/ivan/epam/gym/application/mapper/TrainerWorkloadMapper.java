package ua.ivan.epam.gym.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.ivan.epam.gym.application.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.application.dto.request.WorkloadActionType;
import ua.ivan.epam.gym.application.model.Training;

@Mapper(componentModel = "spring")
public interface TrainerWorkloadMapper {

    @Mapping(target = "trainerUsername", source = "training.trainer.user.username")
    @Mapping(target = "trainerFirstName", source = "training.trainer.user.firstName")
    @Mapping(target = "trainerLastName", source = "training.trainer.user.lastName")
    @Mapping(target = "isActive", source = "training.trainer.user.isActive")
    @Mapping(target = "trainingDate", source = "training.trainingDate")
    @Mapping(target = "trainingDuration", source = "training.trainingDuration")
    @Mapping(target = "actionType", source = "actionType")
    TrainerWorkloadRequest toRequest(Training training, WorkloadActionType actionType);
}