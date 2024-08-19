package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Getter
@Setter
public class TripScheduleInfo {
    private String groupKey; // 그룹 키
    private String tripScheduleName; // 여행 일정 이름
    private String description; // 여행 일정 설명
    private ArrayList<TripScheduleDetails> tripScheduleDetails = new ArrayList<>();
    private String startDate; // LocalDate를 String으로 저장
    private String endDate; // LocalDate를 String으로 저장

    public TripScheduleInfo() {}

    public TripScheduleInfo(String groupKey, String tripScheduleName, String description, LocalDate startDate, LocalDate endDate) {
        this.groupKey = groupKey;
        this.tripScheduleName = tripScheduleName;
        this.description = description;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.startDate = startDate.format(formatter);
        this.endDate = endDate.format(formatter);
        this.tripScheduleDetails = new ArrayList<>();

        generateTripDetails(startDate, endDate);
    }

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
