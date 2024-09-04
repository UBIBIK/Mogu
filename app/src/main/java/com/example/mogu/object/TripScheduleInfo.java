package com.example.mogu.object;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TripScheduleInfo {
    private String groupKey; // 그룹 키
    private ArrayList<TripScheduleDetails> tripScheduleDetails = new ArrayList<>();
    private String startDate; // LocalDate를 String으로 저장
    private String endDate; // LocalDate를 String으로 저장

    // 기본 생성자
    public TripScheduleInfo() {}

    // 매개변수를 받는 생성자
    public TripScheduleInfo(String groupKey, LocalDate startDate, LocalDate endDate) {
        this.groupKey = groupKey;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.startDate = startDate.format(formatter);
        this.endDate = endDate.format(formatter);
        this.tripScheduleDetails = new ArrayList<>();

        generateTripDetails(startDate, endDate);
    }

    public ArrayList<TripScheduleDetails> getTripScheduleDetails() {
        return tripScheduleDetails;
    }

    public void setTripScheduleDetails(ArrayList<TripScheduleDetails> tripScheduleDetails) {
        this.tripScheduleDetails = tripScheduleDetails;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public long getStartDateMillis() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(startDate, formatter);
        return localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;
    }

    public long getEndDateMillis() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(endDate, formatter);
        return localDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000;
    }


    // 내부 메서드: 일정 세부 정보를 생성하는 메서드
    private void generateTripDetails(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;
        int dayCount = 1;
        while (!currentDate.isAfter(endDate)) {
            TripScheduleDetails details = new TripScheduleDetails(
                    currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    dayCount + "일차"
            );
            tripScheduleDetails.add(details);
            currentDate = currentDate.plusDays(1);
            dayCount++;
        }
    }
}