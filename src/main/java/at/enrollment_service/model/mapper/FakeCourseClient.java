package at.enrollment_service.model.mapper;

import at.enrollment_service.dto.*;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Component
@Primary
@Profile("stress")
public class FakeCourseClient extends CourseClient {

    public FakeCourseClient() {
        super(null, null);
    }

    @Override
    public Mono<GetCourseInfoResponse> getCourseInfo(GetCourseInfoRequest request) {

        List<CourseInfo> infos = request.getCourseNames().stream()
                .map(name -> {
                    CourseInfo info = new CourseInfo();
                    info.setName(name);
                    info.setPrice(BigDecimal.TEN);
                    info.setLanguage("EN");
                    info.setIsAvailable(true);
                    return info;
                })
                .toList();

        GetCourseInfoResponse response = new GetCourseInfoResponse();
        response.setCourseInfos(infos);

        return Mono.just(response);
    }
}
