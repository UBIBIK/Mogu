package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mogu.server.mokpowa.dto.TripScheduleDetails;
import mogu.server.mokpowa.dto.TripScheduleInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
public class TripSchedule extends TripScheduleInfo {
    private String groupKey;
    private ArrayList<TripScheduleDetails> tripScheduleDetails = new ArrayList<>();
    private String startDate; // LocalDate를 String으로 저장
    private String endDate; // LocalDate를 String으로 저장

    // 매개 변수를 받는 생성자
    public TripSchedule(String groupKey, LocalDate startDate, LocalDate endDate) {
        this.groupKey = groupKey;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.startDate = startDate.format(formatter);
        this.endDate = endDate.format(formatter);
        this.tripScheduleDetails = new ArrayList<>();

        generateTripDetails(startDate, endDate);
    }

    // 각 일자별 일자, 일차 생성 함수
    private void generateTripDetails(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;
        int dayCount = 1;
        while (!currentDate.isAfter(endDate)) {
            TripScheduleDetails details = new TripScheduleDetails(currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), dayCount + "일차");
            tripScheduleDetails.add(details);
            currentDate = currentDate.plusDays(1);
            dayCount++;
        }
    }
}