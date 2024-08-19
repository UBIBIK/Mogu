package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.Setter;

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
