package at.enrollment_service.testdata;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

public class TestConstants {
    public static final int DELAY_MILLIS = 1500;
    public static final int RETRY_COUNT = 3;
    public static final double RETRY_JITTER = 0.75;
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1);
    public static final String DEFAULT_TIMEOUT_STR = "1s";
    public static final Duration RETRY_BACKOFF = Duration.ofMillis(10);

    public static final String BASE_URL = "/v1/course-enrollments";
    public static final String COURSE_INFO_PATH = "/v1/courses/course-info";

    public static final String CITY_ONE = "CityOne";
    public static final String STREET_ONE = "StreetOne";
    public static final int HOUSE_ONE = 1;
    public static final int APARTMENT_ONE = 1;
    public static final String COURSE_ONE = "One";
    public static final String COURSE_TWO = "Two";
    public static final String COURSE_THREE = "Three";
    public static final String COURSE_ONE_LANGUAGE = "English";
    public static final String COURSE_TWO_LANGUAGE = "English";
    public static final String COURSE_THREE_LANGUAGE = "English";
    public static final BigDecimal COURSE_ONE_PRICE = BigDecimal.valueOf(4);
    public static final BigDecimal COURSE_TWO_PRICE = BigDecimal.valueOf(3);
    public static final BigDecimal COURSE_THREE_PRICE = BigDecimal.valueOf(3);
    public static final String COURSE_CREATE_ONE_LANGUAGE = "English";
    public static final String COURSE_CREATE_TWO_LANGUAGE = "English";
    public static final String COURSE_CREATE_THREE_LANGUAGE = "English";
    public static final String USERNAME_ONE = "username1";
    public static final BigDecimal SUCCESS_TOTAL_PRICE = BigDecimal.valueOf(60.6);
    public static final BigDecimal COURSE_CREATE_ONE_PRICE = BigDecimal.valueOf(10.1);
    public static final BigDecimal COURSE_CREATE_TWO_PRICE = BigDecimal.valueOf(20.2);
    public static final BigDecimal COURSE_CREATE_THREE_PRICE = BigDecimal.valueOf(30.3);
    public static final LocalDateTime ENROLLMENT_ONE_DATE = LocalDateTime.of(2024, Month.FEBRUARY, 18, 10, 23, 54);
    public static final LocalDateTime ENROLLMENT_TWO_DATE = LocalDateTime.of(2024, Month.FEBRUARY, 20, 10, 23, 54);
    public static final LocalDateTime ENROLLMENT_THREE_DATE = LocalDateTime.of(2024, Month.FEBRUARY, 22, 10, 23, 54);
}