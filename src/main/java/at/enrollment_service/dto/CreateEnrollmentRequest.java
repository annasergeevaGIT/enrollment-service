package at.enrollment_service.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
/*
* DTO for creating an enrollment request from the client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateEnrollmentRequest {

    private Set<String> courseNames; //alternative is using Map<String, Integer> nameToQuantity;
    @Valid
    private Address address; // delivery address (for learning materials)
}
