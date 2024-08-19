package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.Setter;
import mogu.server.mokpowa.dto.TripScheduleDetails;
import mogu.server.mokpowa.dto.TripScheduleInfo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Getter
@Setter
public class TripSchedule extends TripScheduleInfo {
    private String groupKey;
    private String tripScheduleName;
    private String description;
    private ArrayList<TripScheduleDetails> tripScheduleDetails;
    private String startDate; // LocalDate를 String으로 저장
    private String endDate; // LocalDate를 String으로 저장

    public TripSchedule() {}

    public TripSchedule(String groupKey, String tripScheduleName, String description, LocalDate startDate, LocalDate endDate) {
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