package mogu.server.mokpowa.dto.TripScheduleRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mogu.server.mokpowa.dto.TripScheduleInfo;
import mogu.server.mokpowa.dto.UserInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripScheduleRequest {
    private UserInfo userInfo;
    private TripScheduleInfo tripScheduleInfo;
}
