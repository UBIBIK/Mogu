package mogu.server.mokpowa.repository;

import mogu.server.mokpowa.dto.TripScheduleInfo;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.TripSchedule;

public interface TripScheduleRepository {
    void checkGroupMembership(UserInfo user, TripScheduleInfo tripScheduleInfo);

    TripScheduleInfo insertTripSchedule(TripScheduleInfo tripScheduleInfo, UserInfo user) throws Exception; // 여행 일정 생성
    
    TripSchedule getTripScheduleDetails(String groupKey) throws Exception; // 여행 일정 조회

//    UserInfo updateTripSchedule(TripSchedule tripSchedule, UserInfo user) throws Exception; // 여행 일정 정보 수정

    UserInfo deleteTripSchedule(String deleteGroupKey, UserInfo user) throws Exception; // 여행 일정 삭제
}