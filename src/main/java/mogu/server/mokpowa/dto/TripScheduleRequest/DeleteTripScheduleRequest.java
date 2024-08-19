package mogu.server.mokpowa.dto.TripScheduleRequest;

import lombok.Getter;
import lombok.Setter;
import mogu.server.mokpowa.dto.UserInfo;

@Getter
@Setter
public class DeleteTripScheduleRequest {
    private UserInfo userInfo;
    private String tripScheduleName;
    private String groupKey;

    public DeleteTripScheduleRequest() {}

    public DeleteTripScheduleRequest(UserInfo userInfo, String tripScheduleName, String groupKey) {
        this.userInfo = userInfo;
        this.tripScheduleName = tripScheduleName;
        this.groupKey = groupKey;
    }
}
