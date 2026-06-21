package ua.ivan.epam.gym.workload.repository;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;

import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TrainerWorkloadPipelines {

    public static List<Bson> buildAddDurationPipeline(TrainerWorkloadRequest request, int year, int month) {
        Document yearsListExistsOrReplaceWithEmpty = ifNull(
                "$years",
                List.of()
        );

        Document yearsNumbersList = map(
                "$$years",
                "existingYear",
                "$$existingYear.year"
        );

        Document yearExistsInList = in(year, yearsNumbersList);

        Document updateYearWithMonthAndDurationIfTrue = map(
                "$$years",
                "existingYear",
                cond(
                        eq(
                                "$$existingYear.year",
                                year
                        ),
                        mergeObjects(
                                "$$existingYear",
                                new Document(
                                        "months",
                                        buildMonthsExpression(
                                                month,
                                                request.trainingDuration()
                                        )
                                )
                        ),
                        "$$existingYear"
                )
        );

        Document newMonthCreated = new Document("month", month)
                .append("trainingSummaryDuration", request.trainingDuration());

        Document newYearWithMonthCreated = new Document("year", year)
                .append("months", List.of(newMonthCreated));

        Document yearsWithNewYearAndMonthCreated = concatArrays(
                "$$years",
                List.of(newYearWithMonthCreated)
        );

        Document resultingYears = let(
                "years",
                yearsListExistsOrReplaceWithEmpty,
                cond(
                        yearExistsInList,
                        updateYearWithMonthAndDurationIfTrue,
                        yearsWithNewYearAndMonthCreated
                )
        );

        Document setFields = new Document()
                .append("trainerUsername", request.trainerUsername())
                .append("trainerFirstName", request.trainerFirstName())
                .append("trainerLastName", request.trainerLastName())
                .append("isActive", request.isActive())
                .append("years", resultingYears);

        return List.of(
                new Document("$set", setFields)
        );
    }

    private static Document buildMonthsExpression(int month, long duration) {
        Document monthsListExistsOrReplaceWithEmpty = ifNull(
                "$$existingYear.months",
                List.of()
        );

        Document monthNumbersList = map(
                "$$months",
                "existingMonth",
                "$$existingMonth.month"
        );

        Document monthExistsInList = in(month, monthNumbersList);

        Document currentDuration = ifNull(
                "$$existingMonth.trainingSummaryDuration",
                0
        );

        Document incrementMonthDuration = mergeObjects(
                "$$existingMonth",
                new Document(
                        "trainingSummaryDuration",
                        add(currentDuration, duration)
                )
        );

        Document updateMonthWithDuration = map(
                "$$months",
                "existingMonth",
                cond(
                        eq(
                                "$$existingMonth.month",
                                month
                        ),
                        incrementMonthDuration,
                        "$$existingMonth"
                )
        );

        Document newMonthCreated = new Document("month", month)
                .append("trainingSummaryDuration", duration);

        Document monthsWithNewMonthCreated = concatArrays(
                "$$months",
                List.of(newMonthCreated)
        );

        return let(
                "months",
                monthsListExistsOrReplaceWithEmpty,
                cond(
                        monthExistsInList,
                        updateMonthWithDuration,
                        monthsWithNewMonthCreated
                )
        );
    }

    private static Document ifNull(Object value, Object replacement) {
        return new Document(
                "$ifNull",
                List.of(value, replacement)
        );
    }

    private static Document map(Object input, String variable, Object expression) {
        return new Document(
                "$map",
                new Document("input", input)
                        .append("as", variable)
                        .append("in", expression)
        );
    }

    private static Document in(Object value, Object array) {
        return new Document(
                "$in",
                List.of(value, array)
        );
    }

    private static Document eq(Object first, Object second) {
        return new Document(
                "$eq",
                List.of(first, second)
        );
    }

    private static Document cond(Object condition, Object whenTrue, Object whenFalse) {
        return new Document(
                "$cond",
                List.of(condition, whenTrue, whenFalse)
        );
    }

    private static Document add(Object... values) {
        return new Document(
                "$add",
                Arrays.asList(values)
        );
    }

    private static Document concatArrays(Object... arrays) {
        return new Document(
                "$concatArrays",
                Arrays.asList(arrays)
        );
    }

    private static Document mergeObjects(Object... objects) {
        return new Document(
                "$mergeObjects",
                Arrays.asList(objects)
        );
    }

    private static Document let(String variable, Object value, Object expression) {
        return new Document(
                "$let",
                new Document(
                        "vars",
                        new Document(variable, value)
                ).append(
                        "in",
                        expression
                )
        );
    }
}