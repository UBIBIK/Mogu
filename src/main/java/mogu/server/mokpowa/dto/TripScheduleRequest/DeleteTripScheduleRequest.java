package mogu.server.mokpowa.dto.TripScheduleRequest;

import lombok.Getter;
import lombok.Setter;
import mogu.server.mokpowa.dto.UserInfo;

@Getter
@Setter
public class DeleteTripScheduleRequest {
    private UserInfo userInfo; // 여행 유저
    private String tripScheduleName; // 여행 일정 이름
    private String groupKey; // 해당 그룹의 그룹 키

    public DeleteTripScheduleRequest() {}

    public DeleteTripScheduleRequest(UserInfo userInfo, String tripScheduleName, String groupKey) {
        this.userInfo = userInfo;
        this.tripScheduleName = tripScheduleName;
        this.groupKey = groupKey;
    }
}
