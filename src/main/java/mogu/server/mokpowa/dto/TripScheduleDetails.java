package mogu.server.mokpowa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
public class TripScheduleDetails {
    private String date; // 날짜
    private String day; // x일차(1일차, 2일차...)
    private ArrayList<LocationInfo> locations = new ArrayList<>(); // 장소 정보
    private String note; // 설명

    public TripScheduleDetails() {}

    public TripScheduleDetails(String date, String day) {
        this.date = date;
        this.day = day;
    }
}