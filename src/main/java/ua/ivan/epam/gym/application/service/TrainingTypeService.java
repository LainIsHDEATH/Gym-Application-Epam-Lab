package ua.ivan.epam.gym.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.mapper.RestResponseMapper;
import ua.ivan.epam.gym.application.repository.TrainingTypeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    private final RestResponseMapper mapper;

    @Transactional(readOnly = true)
    public List<TrainingTypeResponse> getAll(){
        return trainingTypeRepository.findAll().stream()
                .map(mapper::toTrainingTypeResponse)
                .toList();
    }
}
