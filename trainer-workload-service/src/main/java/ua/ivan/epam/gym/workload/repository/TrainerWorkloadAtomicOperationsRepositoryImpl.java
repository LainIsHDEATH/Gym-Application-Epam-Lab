package ua.ivan.epam.gym.workload.repository;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.exception.exceptions.SubtractDurationException;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadAtomicOperationsRepositoryImpl
        implements TrainerWorkloadAtomicOperationsRepository {

    private static final int MAX_UPSERT_ATTEMPTS = 3;

    private final MongoTemplate mongoTemplate;

    @Override
    public void addDuration(TrainerWorkloadRequest request, int year, int month) {
        if (incrementExistingMonth(request, year, month)) {
            return;
        }

        log.debug("Month={} does not exist. Upserting with full pipeline.", month);

        upsertWithPipeline(request, year, month);
    }

    private boolean incrementExistingMonth(TrainerWorkloadRequest request, int year, int month) {
        Criteria targetYear = Criteria.where("year")
                .is(year)
                .and("months")
                .elemMatch(
                        Criteria.where("month").is(month)
                );

        Query query = Query.query(
                Criteria.where("trainerUsername")
                        .is(request.trainerUsername())
                        .and("years")
                        .elemMatch(targetYear)
        );

        Update update = new Update()
                .set("trainerFirstName", request.trainerFirstName())
                .set("trainerLastName", request.trainerLastName())
                .set("isActive", request.isActive())
                .inc(
                        "years.$[year].months.$[month].trainingSummaryDuration",
                        request.trainingDuration()
                )
                .filterArray(Criteria.where("year.year").is(year))
                .filterArray(Criteria.where("month.month").is(month));

        UpdateResult result = mongoTemplate.updateFirst(
                query,
                update,
                TrainerWorkload.class
        );

        return result.getModifiedCount() != 0;
    }

    private void upsertWithPipeline(TrainerWorkloadRequest request, int year, int month) {
        List<Bson> pipeline =
                TrainerWorkloadPipelines.buildAddDurationPipeline(
                        request,
                        year,
                        month
                );

        String collectionName = mongoTemplate.getCollectionName(TrainerWorkload.class);

        for (int attempt = 1; attempt <= MAX_UPSERT_ATTEMPTS; attempt++) {
            try {
                mongoTemplate.execute(
                        collectionName,
                        collection -> collection.updateOne(
                                Filters.eq(
                                        "trainerUsername",
                                        request.trainerUsername()
                                ),
                                pipeline,
                                new UpdateOptions().upsert(true)
                        )
                );

                return;
            } catch (DuplicateKeyException exception) {
                log.warn("Failed to upsert with pipeline. Attempt={}", attempt);
                if (attempt == MAX_UPSERT_ATTEMPTS) {
                    throw exception;
                }
            }
        }
    }

    @Override
    public void subtractDuration(TrainerWorkloadRequest request, int year, int month) {
        Criteria targetMonth = Criteria.where("month")
                .is(month)
                .and("trainingSummaryDuration")
                .gte(request.trainingDuration());

        Criteria targetYear = Criteria.where("year")
                .is(year)
                .and("months")
                .elemMatch(targetMonth);

        Query query = Query.query(
                Criteria.where("trainerUsername")
                        .is(request.trainerUsername())
                        .and("years")
                        .elemMatch(targetYear)
        );

        Update update = new Update()
                .set("trainerFirstName", request.trainerFirstName())
                .set("trainerLastName", request.trainerLastName())
                .set("isActive", request.isActive())
                .inc(
                        "years.$[year].months.$[month].trainingSummaryDuration",
                        -request.trainingDuration()
                )
                .filterArray(Criteria.where("year.year").is(year))
                .filterArray(
                        Criteria.where("month.month")
                                .is(month)
                                .and("month.trainingSummaryDuration")
                                .gte(request.trainingDuration())
                );

        UpdateResult result = mongoTemplate.updateFirst(
                query,
                update,
                TrainerWorkload.class
        );

        if (result.getModifiedCount() == 0) {
            throw new SubtractDurationException(
                    "Cannot subtract training duration. "
                            + "Trainer, year or month does not exist, "
                            + "or accumulated duration is insufficient. "
                            + "trainerUsername=%s, year=%d, month=%d, duration=%d"
                            .formatted(
                                    request.trainerUsername(),
                                    year,
                                    month,
                                    request.trainingDuration()
                            )
            );
        }
    }
}