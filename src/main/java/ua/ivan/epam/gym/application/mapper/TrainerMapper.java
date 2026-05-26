package ua.ivan.epam.gym.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ua.ivan.epam.gym.application.dto.response.TraineeShortResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerProfileResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerShortResponse;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(
        componentModel = "spring",
        uses = {
                TrainingTypeMapper.class
        }
)
public abstract class TrainerMapper {

    @Autowired
    protected ShortResponseMapper shortResponseMapper;

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization")
    public abstract TrainerShortResponse toTrainerShortResponse(Trainer trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainees", expression = "java(toSortedTraineeShortResponses(trainer.getTrainees()))")
    public abstract TrainerProfileResponse toTrainerProfileResponse(Trainer trainer);

    protected List<TraineeShortResponse> toSortedTraineeShortResponses(Set<Trainee> trainees) {
        if (trainees == null) {
            return List.of();
        }

        return trainees.stream()
                .sorted(Comparator.comparing(trainee -> trainee.getUser().getUsername()))
                .map(shortResponseMapper::toTraineeShortResponse)
                .toList();
    }
}