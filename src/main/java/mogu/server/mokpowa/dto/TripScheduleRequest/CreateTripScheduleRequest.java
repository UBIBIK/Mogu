package mogu.server.mokpowa.dto.TripScheduleRequest;

import lombok.Getter;
import lombok.Setter;
import mogu.server.mokpowa.dto.TripScheduleInfo;
import mogu.server.mokpowa.dto.UserInfo;

@Getter
@Setter
public class CreateTripScheduleRequest {
    private UserInfo userInfo;
    private TripScheduleInfo tripScheduleInfo;

    public CreateTripScheduleRequest() {}

    public CreateTripScheduleRequest(UserInfo userInfo, TripScheduleInfo tripScheduleInfo) {
        this.userInfo = userInfo;
        this.tripScheduleInfo = tripScheduleInfo;
    }
}
