package mogu.server.mokpowa.dto.TripScheduleRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mogu.server.mokpowa.dto.UserInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteTripScheduleRequest {
    private UserInfo userInfo; // 여행 유저
    private String tripScheduleName; // 여행 일정 이름
    private String groupKey; // 해당 그룹의 그룹 키
}
