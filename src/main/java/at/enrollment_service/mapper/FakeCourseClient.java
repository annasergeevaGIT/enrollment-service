package at.enrollment_service.mapper;

import at.enrollment_service.dto.CourseInfo;
import at.enrollment_service.dto.GetCourseInfoRequest;
import at.enrollment_service.dto.GetCourseInfoResponse;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Primary
@Profile("stress")
public class FakeCourseClient extends CourseClient {

    public FakeCourseClient() {
        super(null, null); // not used
    }

    @Override
    public GetCourseInfoResponse getCourseInfo(GetCourseInfoRequest request) {

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

        return response;
    }
}
