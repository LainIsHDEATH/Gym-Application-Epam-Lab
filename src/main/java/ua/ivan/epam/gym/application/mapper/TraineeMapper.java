package ua.ivan.epam.gym.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ua.ivan.epam.gym.application.dto.response.TraineeProfileResponse;
import ua.ivan.epam.gym.application.dto.response.TraineeShortResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerShortResponse;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class TraineeMapper {

    @Autowired
    protected ShortResponseMapper shortResponseMapper;

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    public abstract TraineeShortResponse toTraineeShortResponse(Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "trainers", expression = "java(toSortedTrainerShortResponses(trainee.getTrainers()))")
    public abstract TraineeProfileResponse toTraineeProfileResponse(Trainee trainee);

    protected List<TrainerShortResponse> toSortedTrainerShortResponses(Set<Trainer> trainers) {
        if (trainers == null) {
            return List.of();
        }

        return trainers.stream()
                .sorted(Comparator.comparing(trainer -> trainer.getUser().getUsername()))
                .map(shortResponseMapper::toTrainerShortResponse)
                .toList();
    }
}