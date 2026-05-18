package ua.ivan.epam.gym.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.model.TrainingType;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    @Mapping(target = "trainingType", source = "trainingTypeName")
    TrainingTypeResponse toTrainingTypeResponse(TrainingType trainingType);
}
