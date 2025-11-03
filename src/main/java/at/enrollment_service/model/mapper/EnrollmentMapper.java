package at.enrollment_service.model.mapper;

import at.enrollment_service.dto.*;
import at.enrollment_service.exception.EnrollmentServiceException;
import at.enrollment_service.model.CourseEnrollment;
import at.enrollment_service.model.CourseLineItem;
import at.enrollment_service.model.EnrollmentStatus;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

@Component
public class EnrollmentMapper {

    public CourseEnrollment mapToEnrollment(CreateEnrollmentRequest request, String username, GetCourseInfoResponse infoResponse) {
        var infos = infoResponse.getCourseInfos();
        throwIfHasUnavailableMenuItems(infos);

        List<CourseLineItem> courseLineItems = getCourseLineItems(request, infos);
        var totalPrice = calculateTotalPrice(courseLineItems);

        return CourseEnrollment.builder()
                .totalPrice(totalPrice)
                .city(request.getAddress().getCity())
                .street(request.getAddress().getStreet())
                .house(request.getAddress().getHouse())
                .apartment(request.getAddress().getApartment())
                .status(EnrollmentStatus.NEW)
                .createdBy(username)
                .courseLineItems(courseLineItems)
                .build();
    }

    public EnrollmentResponse mapToResponse(CourseEnrollment enrollment) {
        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getId())
                .totalPrice(enrollment.getTotalPrice())
                .courseLineItems(enrollment.getCourseLineItems())
                .address(Address.builder()
                        .city(enrollment.getCity())
                        .street(enrollment.getStreet())
                        .house(enrollment.getHouse())
                        .apartment(enrollment.getApartment())
                        .build())
                .status(enrollment.getStatus())
                .createdAt(enrollment.getCreatedAt())
                .build();
    }

    private void throwIfHasUnavailableMenuItems(List<CourseInfo> infos) {
        boolean hasUnavailable = infos.stream().anyMatch(m -> !m.getIsAvailable());
        if (hasUnavailable) {
            var msg = String.format("Cannot create enrollment, because some courses are not available: %s",
                    infos);
            throw new EnrollmentServiceException(msg, HttpStatus.NOT_FOUND);
        }
    }

    private List<CourseLineItem> getCourseLineItems(CreateEnrollmentRequest request, List<CourseInfo> infos) {
        return infos.stream()
                .filter(info -> request.getCourseNames().contains(info.getName())) // in case of Set<String>
                .map(info -> CourseLineItem.builder()
                    //int quantity = request.getNameToQuantity().get(info.getName()); in case of Map<String, Integer>
                        .courseItemName(info.getName())
                        .price(info.getPrice())
                        .language(info.getLanguage())
                        .build())
                .toList();
    }

    private BigDecimal calculateTotalPrice(List<CourseLineItem> courseLineItems) {
        return courseLineItems.stream()
                .map(item -> item.getPrice())  //in case of quantity use: .multiply(BigDecimal.valueOf(item.getQuantity())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
