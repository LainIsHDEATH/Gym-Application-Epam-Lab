package ua.ivan.epam.gym.workload.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.exception.exceptions.SubtractDurationException;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;
import ua.ivan.epam.gym.workload.model.WorkloadActionType;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadAtomicOperationsRepositoryImplTest {

    private static final String COLLECTION_NAME = "trainer_workloads";

    private static final String USERNAME = "Mike.Brown";

    private static final int YEAR = 2026;
    private static final int MONTH = 5;
    private static final int DURATION = 60;

    private static final String DURATION_PATH =
            "years.$[year].months.$[month]"
                    + ".trainingSummaryDuration";

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private MongoCollection<Document> mongoCollection;

    @InjectMocks
    private TrainerWorkloadAtomicOperationsRepositoryImpl repository;

    @Test
    void addDurationShouldUseIncrementWhenMonthExists() {
        TrainerWorkloadRequest request = createRequest(
                WorkloadActionType.ADD
        );

        when(
                mongoTemplate.updateFirst(
                        any(Query.class),
                        any(Update.class),
                        eq(TrainerWorkload.class)
                )
        ).thenReturn(
                UpdateResult.acknowledged(1, 1L, null)
        );

        repository.addDuration(request, YEAR, MONTH);

        ArgumentCaptor<Query> queryCaptor =
                ArgumentCaptor.forClass(Query.class);

        ArgumentCaptor<Update> updateCaptor =
                ArgumentCaptor.forClass(Update.class);

        verify(mongoTemplate).updateFirst(
                queryCaptor.capture(),
                updateCaptor.capture(),
                eq(TrainerWorkload.class)
        );

        assertIncrementQuery(
                queryCaptor.getValue(),
                false
        );

        assertAddUpdate(updateCaptor.getValue());

        verify(mongoTemplate, never()).getCollectionName(TrainerWorkload.class);
    }

    @Test
    void addDurationShouldExecutePipelineWhenMonthDoesNotExist() {
        TrainerWorkloadRequest request = createRequest(
                WorkloadActionType.ADD
        );

        when(
                mongoTemplate.updateFirst(
                        any(Query.class),
                        any(Update.class),
                        eq(TrainerWorkload.class)
                )
        ).thenReturn(
                UpdateResult.acknowledged(0, 0L, null)
        );

        when(
                mongoTemplate.getCollectionName(
                        TrainerWorkload.class
                )
        ).thenReturn(COLLECTION_NAME);

        mockExecuteCallback();

        when(
                mongoCollection.updateOne(
                        any(Bson.class),
                        ArgumentMatchers
                                .<List<? extends Bson>>any(),
                        any(UpdateOptions.class)
                )
        ).thenReturn(
                UpdateResult.acknowledged(0, 0L, null)
        );

        repository.addDuration(request, YEAR, MONTH);

        verify(mongoTemplate).execute(
                eq(COLLECTION_NAME),
                ArgumentMatchers
                        .<CollectionCallback<UpdateResult>>any()
        );

        ArgumentCaptor<UpdateOptions> optionsCaptor =
                ArgumentCaptor.forClass(UpdateOptions.class);

        verify(mongoCollection).updateOne(
                any(Bson.class),
                ArgumentMatchers
                        .<List<? extends Bson>>any(),
                optionsCaptor.capture()
        );

        assertTrue(optionsCaptor.getValue().isUpsert());
    }

    @Test
    void addDurationShouldRetryAfterDuplicateKeyException() {
        TrainerWorkloadRequest request = createRequest(
                WorkloadActionType.ADD
        );

        mockFastPathMiss();

        DuplicateKeyException duplicateKeyException =
                new DuplicateKeyException(
                        "Concurrent upsert conflict"
                );

        when(
                mongoTemplate.execute(
                        eq(COLLECTION_NAME),
                        ArgumentMatchers
                                .<CollectionCallback<UpdateResult>>any()
                )
        )
                .thenThrow(duplicateKeyException)
                .thenReturn(
                        UpdateResult.acknowledged(
                                1,
                                1L,
                                null
                        )
                );

        repository.addDuration(request, YEAR, MONTH);

        verify(mongoTemplate, times(2)).execute(
                eq(COLLECTION_NAME),
                ArgumentMatchers
                        .<CollectionCallback<UpdateResult>>any()
        );
    }

    @Test
    void addDurationShouldRethrowAfterMaximumUpsertAttempts() {
        TrainerWorkloadRequest request = createRequest(
                WorkloadActionType.ADD
        );

        mockFastPathMiss();

        DuplicateKeyException expectedException =
                new DuplicateKeyException(
                        "Concurrent upsert conflict"
                );

        when(
                mongoTemplate.execute(
                        eq(COLLECTION_NAME),
                        ArgumentMatchers
                                .<CollectionCallback<UpdateResult>>any()
                )
        ).thenThrow(expectedException);

        DuplicateKeyException actualException = assertThrows(
                DuplicateKeyException.class,
                () -> repository.addDuration(
                        request,
                        YEAR,
                        MONTH
                )
        );

        assertSame(expectedException, actualException);

        verify(mongoTemplate, times(3)).execute(
                eq(COLLECTION_NAME),
                ArgumentMatchers
                        .<CollectionCallback<UpdateResult>>any()
        );
    }

    @Test
    void subtractDurationShouldExecuteAtomicDecrement() {
        TrainerWorkloadRequest request = createRequest(
                WorkloadActionType.DELETE
        );

        when(
                mongoTemplate.updateFirst(
                        any(Query.class),
                        any(Update.class),
                        eq(TrainerWorkload.class)
                )
        ).thenReturn(
                UpdateResult.acknowledged(1, 1L, null)
        );

        repository.subtractDuration(request, YEAR, MONTH);

        ArgumentCaptor<Query> queryCaptor =
                ArgumentCaptor.forClass(Query.class);

        ArgumentCaptor<Update> updateCaptor =
                ArgumentCaptor.forClass(Update.class);

        verify(mongoTemplate).updateFirst(
                queryCaptor.capture(),
                updateCaptor.capture(),
                eq(TrainerWorkload.class)
        );

        assertIncrementQuery(
                queryCaptor.getValue(),
                true
        );

        assertSubtractUpdate(updateCaptor.getValue());
    }

    @Test
    void subtractDurationShouldThrowWhenNothingWasModified() {
        TrainerWorkloadRequest request = createRequest(
                WorkloadActionType.DELETE
        );

        when(
                mongoTemplate.updateFirst(
                        any(Query.class),
                        any(Update.class),
                        eq(TrainerWorkload.class)
                )
        ).thenReturn(
                UpdateResult.acknowledged(0, 0L, null)
        );

        SubtractDurationException exception = assertThrows(
                SubtractDurationException.class,
                () -> repository.subtractDuration(
                        request,
                        YEAR,
                        MONTH
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "trainerUsername=" + USERNAME
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "year=" + YEAR
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "month=" + MONTH
                )
        );

        assertTrue(
                exception.getMessage().contains(
                        "duration=" + DURATION
                )
        );
    }

    private void assertIncrementQuery(
            Query query,
            boolean shouldCheckDuration
    ) {
        Document queryObject = query.getQueryObject();

        assertEquals(
                USERNAME,
                queryObject.getString("trainerUsername")
        );

        Document yearsCondition = queryObject.get(
                "years",
                Document.class
        );

        assertNotNull(yearsCondition);

        Document yearElement = yearsCondition.get(
                "$elemMatch",
                Document.class
        );

        assertEquals(YEAR, yearElement.getInteger("year"));

        Document monthsCondition = yearElement.get(
                "months",
                Document.class
        );

        Document monthElement = monthsCondition.get(
                "$elemMatch",
                Document.class
        );

        assertEquals(
                MONTH,
                monthElement.getInteger("month")
        );

        if (shouldCheckDuration) {
            Document durationCondition = monthElement.get(
                    "trainingSummaryDuration",
                    Document.class
            );

            assertEquals(
                    DURATION,
                    durationCondition.getInteger("$gte")
            );
        }
    }

    private void assertAddUpdate(Update update) {
        Document updateObject = update.getUpdateObject();

        Document setOperation = updateObject.get(
                "$set",
                Document.class
        );

        assertEquals(
                "Mike",
                setOperation.getString("trainerFirstName")
        );

        assertEquals(
                "Brown",
                setOperation.getString("trainerLastName")
        );

        assertEquals(
                true,
                setOperation.getBoolean("isActive")
        );

        Document incrementOperation = updateObject.get(
                "$inc",
                Document.class
        );

        assertEquals(
                DURATION,
                incrementOperation.getInteger(DURATION_PATH)
        );

        assertArrayFilters(
                update,
                List.of(
                        new Document("year.year", YEAR),
                        new Document("month.month", MONTH)
                )
        );
    }

    private void assertSubtractUpdate(Update update) {
        Document updateObject = update.getUpdateObject();

        Document incrementOperation = updateObject.get(
                "$inc",
                Document.class
        );

        assertEquals(
                -DURATION,
                incrementOperation.getInteger(DURATION_PATH)
        );

        assertArrayFilters(
                update,
                List.of(
                        new Document("year.year", YEAR),
                        new Document()
                                .append("month.month", MONTH)
                                .append(
                                        "month.trainingSummaryDuration",
                                        new Document(
                                                "$gte",
                                                DURATION
                                        )
                                )
                )
        );
    }

    private void assertArrayFilters(
            Update update,
            List<Document> expectedFilters
    ) {
        List<Document> actualFilters = update
                .getArrayFilters()
                .stream()
                .map(UpdateDefinition.ArrayFilter::asDocument)
                .toList();

        assertEquals(expectedFilters, actualFilters);
    }

    private void mockFastPathMiss() {
        when(
                mongoTemplate.updateFirst(
                        any(Query.class),
                        any(Update.class),
                        eq(TrainerWorkload.class)
                )
        ).thenReturn(
                UpdateResult.acknowledged(0, 0L, null)
        );

        when(
                mongoTemplate.getCollectionName(
                        TrainerWorkload.class
                )
        ).thenReturn(COLLECTION_NAME);
    }

    private void mockExecuteCallback() {
        when(
                mongoTemplate.execute(
                        eq(COLLECTION_NAME),
                        ArgumentMatchers
                                .<CollectionCallback<UpdateResult>>any()
                )
        ).thenAnswer(invocation -> {
            CollectionCallback<UpdateResult> callback =
                    invocation.getArgument(1);

            return callback.doInCollection(mongoCollection);
        });
    }

    private TrainerWorkloadRequest createRequest(
            WorkloadActionType actionType
    ) {
        return new TrainerWorkloadRequest(
                USERNAME,
                "Mike",
                "Brown",
                true,
                LocalDate.of(YEAR, MONTH, 10),
                DURATION,
                actionType
        );
    }
}