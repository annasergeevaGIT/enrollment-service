package at.enrollment_service.testdata;

import at.enrollment_service.dto.Address;
import at.enrollment_service.dto.CreateEnrollmentRequest;
import at.enrollment_service.model.CourseLineItem;
import okhttp3.mockwebserver.MockResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.ResourceUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static at.enrollment_service.testdata.TestConstants.*;

public class TestDataProvider {
    public static CreateEnrollmentRequest createEnrollmentRequest() {
        return CreateEnrollmentRequest.builder()
                .address(Address.builder()
                        .city(CITY_ONE)
                        .street(STREET_ONE)
                        .house(HOUSE_ONE)
                        .apartment(APARTMENT_ONE)
                        .build())
                .courseNames(Set.of(
                        COURSE_ONE,
                        COURSE_TWO,
                        COURSE_THREE
                )).build();
    }

    public static CreateEnrollmentRequest createEnrollmentInvalidRequest() {
        return CreateEnrollmentRequest.builder()
                .address(Address.builder()
                        .city("")
                        .street("")
                        .house(-1)
                        .apartment(-1)
                        .build())
                .courseNames(Set.of(
                        COURSE_ONE,
                        COURSE_TWO,
                        COURSE_THREE
                )).build();
    }

    public static CourseLineItem firstExisting() {
        return CourseLineItem.builder()
                .courseItemName(COURSE_ONE)
                .price(COURSE_ONE_PRICE)
                .language(COURSE_ONE_LANGUAGE)
                .build();
    }

    public static CourseLineItem secondExisting() {
        return CourseLineItem.builder()
                .courseItemName(COURSE_TWO)
                .price(COURSE_TWO_PRICE)
                .language(COURSE_TWO_LANGUAGE)
                .build();
    }

    public static CourseLineItem thirdExisting() {
        return CourseLineItem.builder()
                .courseItemName(COURSE_THREE)
                .price(COURSE_THREE_PRICE)
                .language(COURSE_THREE_LANGUAGE)
                .build();
    }

    public static List<CourseLineItem> existingItems() {
        return List.of(
                firstExisting(),
                secondExisting(),
                thirdExisting()
        );
    }

    public static CourseLineItem firstCreatedItem() {
        return CourseLineItem.builder()
                .courseItemName(COURSE_ONE)
                .price(COURSE_CREATE_ONE_PRICE)
                .language(COURSE_CREATE_ONE_LANGUAGE)
                .build();
    }

    public static CourseLineItem secondCreatedItem() {
        return CourseLineItem.builder()
                .courseItemName(COURSE_TWO)
                .price(COURSE_CREATE_TWO_PRICE)
                .language(COURSE_CREATE_TWO_LANGUAGE)
                .build();
    }

    public static CourseLineItem thirdCreatedItem() {
        return CourseLineItem.builder()
                .courseItemName(COURSE_THREE)
                .price(COURSE_CREATE_THREE_PRICE)
                .language(COURSE_CREATE_THREE_LANGUAGE)
                .build();
    }

    public static List<CourseLineItem> createdItems() {
        return List.of(
                firstCreatedItem(),
                secondCreatedItem(),
                thirdCreatedItem()
        );
    }

    /* Returns a result of the request about available courses and prices
    * One of the returned courses is not available for enrollment
    */
    public static MockResponse partialSuccessResponse() {
        return new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(readPartiallySuccessfulResponse());
    }

    public static MockResponse successResponse() {
        return new MockResponse()
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setResponseCode(HttpStatus.OK.value())
                .setBody(readSuccessfulResponse());
    }

    public static String readSuccessfulResponse() {
        return readFileToString("wiremock/success-response.json");
    }

    public static String readPartiallySuccessfulResponse() {
        return readFileToString("wiremock/partially-success-response.json");
    }

    private static String readFileToString(String filePath) {
        try {
            Path path = ResourceUtils.getFile("classpath:" + filePath).toPath();
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
