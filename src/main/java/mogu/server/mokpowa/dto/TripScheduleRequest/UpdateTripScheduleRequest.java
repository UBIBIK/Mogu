package mogu.server.mokpowa.dto.TripScheduleRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mogu.server.mokpowa.dto.TripScheduleInfo;
import mogu.server.mokpowa.dto.UserInfo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTripScheduleRequest {
    private UserInfo userInfo;
    private TripScheduleInfo tripScheduleInfo;

    /*여행 일정 업데이트 작동 메커니즘: 수정할 userInfo와 업데이트할 tripScheduleInfo를 담은 UpdateTripScheduleRequest 객체를 tripUpdate 메서드를 통해 서버로 전송하면
    해당 tripScheduleInfo를 파이어베이스에 업데이트하고 해당 tripScheduleInfo로 업데이트된 userInfo를 클라이언트로 반환한다.*/
}