package at.enrollment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    @NotBlank(message = "City name cannot be empty.")
    private String city;
    @NotBlank(message = "Street name cannot be empty.")
    private String street;
    @Positive(message = "House number must be > 0")
    private int house;
    @Positive(message = "Flat number must be > 0")
    private int apartment;
}

