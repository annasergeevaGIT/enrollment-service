package at.enrollment_service.controller;

import at.enrollment_service.dto.CreateEnrollmentRequest;
import at.enrollment_service.dto.EnrollmentResponse;
import at.enrollment_service.dto.SortBy;
import at.enrollment_service.service.CourseEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CourseEnrollmentController", description = "REST API for managing course enrollments.")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/course-enrollments")
public class CourseEnrollmentController {

    public static final String USER_HEADER = "X-User-Name";
    private final CourseEnrollmentService courseEnrollmentService;

    @Operation(
            summary = "${api.submit-enrollment.summary}",
            description = "${api.submit-enrollment.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "${api.response.submitOk}"),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.submitBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "${api.response.submitNotFound}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "${api.response.submitInternalError}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse submitCourseEnrollment(@RequestBody @Valid CreateEnrollmentRequest request,
                                                           @RequestHeader(USER_HEADER) String username) {
        log.info("Received POST request to submit enrollment: {}", request);
        return courseEnrollmentService.createEnrollment(request, username);
    }

    @Operation(
            summary = "${api.get-enrollments.summary}",
            description = "${api.get-enrollments.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response.getOk}"),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.getBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping
    public List<EnrollmentResponse> getEnrollmentsOfUser(
            @RequestParam(value= "from", defaultValue = "0")
            @PositiveOrZero(message = "From index must be zero or positive.")
            int from,
            @RequestParam(value= "size", defaultValue = "10")
            @Positive(message = "Size must be positive.")
            int size,
            @RequestParam(value = "sortBy", defaultValue = "DATE_ASC")
            @NotBlank(message = "SortBy parameter must not be blank.")
            String sortBy,
            @RequestHeader(USER_HEADER) String username) {
        log.info("Received request to GET enrollments of user with name={}", username);
        return courseEnrollmentService.getEnrollmentsOfUser(username, SortBy.fromString(sortBy), from, size);
    }
}
