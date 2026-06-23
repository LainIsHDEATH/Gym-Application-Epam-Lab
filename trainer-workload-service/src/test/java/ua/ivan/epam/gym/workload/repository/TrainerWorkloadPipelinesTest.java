package ua.ivan.epam.gym.workload.repository;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.model.WorkloadActionType;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrainerWorkloadPipelinesTest {

    @Test
    void buildAddDurationPipelineShouldCreateSetPipeline() {
        TrainerWorkloadRequest request =
                new TrainerWorkloadRequest(
                        "Mike.Brown",
                        "Mike",
                        "Brown",
                        true,
                        LocalDate.of(2026, 5, 10),
                        60,
                        WorkloadActionType.ADD
                );

        List<Bson> pipeline =
                TrainerWorkloadPipelines
                        .buildAddDurationPipeline(
                                request,
                                2026,
                                5
                        );

        assertEquals(1, pipeline.size());

        Document setStage = assertInstanceOf(
                Document.class,
                pipeline.getFirst()
        );

        Document setFields = setStage.get(
                "$set",
                Document.class
        );

        assertNotNull(setFields);

        assertEquals(
                "Mike.Brown",
                setFields.getString("trainerUsername")
        );

        assertEquals(
                "Mike",
                setFields.getString("trainerFirstName")
        );

        assertEquals(
                "Brown",
                setFields.getString("trainerLastName")
        );

        assertTrue(setFields.getBoolean("isActive"));

        Document yearsExpression = setFields.get(
                "years",
                Document.class
        );

        Document letExpression = yearsExpression.get(
                "$let",
                Document.class
        );

        assertNotNull(letExpression);

        Document variables = letExpression.get(
                "vars",
                Document.class
        );

        assertEquals(
                new Document(
                        "$ifNull",
                        List.of("$years", List.of())
                ),
                variables.get("years")
        );

        Document conditionalExpression =
                letExpression.get(
                        "in",
                        Document.class
                );

        assertTrue(
                conditionalExpression.containsKey("$cond")
        );
    }
}