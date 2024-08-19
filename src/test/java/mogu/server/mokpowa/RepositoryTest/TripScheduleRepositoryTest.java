package mogu.server.mokpowa.RepositoryTest;

import mogu.server.mokpowa.controller.AndroidController;
import mogu.server.mokpowa.dto.DeleteTripScheduleRequest;
import mogu.server.mokpowa.dto.LocationInfo;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.Group;
import mogu.server.mokpowa.entity.TripSchedule;
import mogu.server.mokpowa.repository.TripScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TripScheduleRepositoryTest {
    private static final String TEST_GROUP_KEY = "6x6njlutur41ovcv";
    private static final String TEST_GROUP_KEY2 = "4x15torzialiynot";
    private static final String TEST_TRIP_SCHEDULE_NAME = "제주 여행";
    private static final String TEST_GROUP_MASTER_NAME = "qwe";
    private static final String TEST_GROUP_MEMBER_EMAIL = "1qwe@123";
    private static final String TEST_GROUP_MEMBER_PASSWORD = "123";

    @Autowired
    private TripScheduleRepository tripScheduleRepository;
    @Autowired
    private AndroidController androidController;

    @Test
    public void insertTripScheduleTest() throws Exception {
        TripSchedule tripSchedule =
                new TripSchedule(TEST_GROUP_KEY, TEST_TRIP_SCHEDULE_NAME,
                "제주 여행 설명",
                LocalDate.of(2024,8,7),
                LocalDate.of(2024,8,9));
        tripSchedule.getTripScheduleDetails().getFirst().getLocations().addFirst(
                new LocationInfo("제주흑돼지", 34.23, 126.453));

        UserInfo member = new UserInfo();
        member.setUserEmail(TEST_GROUP_MEMBER_EMAIL);
        member.setUserName(TEST_GROUP_MASTER_NAME);
        Group group = new Group(TEST_TRIP_SCHEDULE_NAME, TEST_GROUP_KEY, member.getUserEmail(), member.getUserName());
        member.getGroupList().add(group);

        tripScheduleRepository.insertTripSchedule(tripSchedule, member);
    }

    @Test
    public void getTripScheduleDetailTest() throws Exception {
        TripSchedule tripSchedule = tripScheduleRepository.getTripScheduleDetails(TEST_GROUP_KEY);
        System.out.println(tripSchedule.getDescription());
    }

    @Test
    public void updateTripScheduleTest() throws Exception {
        TripSchedule updateTrip =
                tripScheduleRepository.getTripScheduleDetails(TEST_GROUP_KEY);
        updateTrip.getTripScheduleDetails().getFirst().getLocations().getFirst().
                setLocationName("제주도해녀라면");
        tripScheduleRepository.updateTripSchedule(updateTrip);
    }

    @Test
    public void deleteTripScheduleTest() throws Exception {
        // 로그인 사용자 정보 설정
        UserInfo loginUser = new UserInfo();
        loginUser.setUserEmail(TEST_GROUP_MEMBER_EMAIL);
        loginUser.setPassword(TEST_GROUP_MEMBER_PASSWORD);

        // 로그인 사용자 정보 받아오기
        ResponseEntity<UserInfo> response = androidController.loginUser(loginUser);
        UserInfo userInfo = response.getBody();

        DeleteTripScheduleRequest request = new DeleteTripScheduleRequest(userInfo, TEST_TRIP_SCHEDULE_NAME, TEST_GROUP_KEY);

        // 여행 일정 삭제
        ResponseEntity<UserInfo> response2 = androidController.tripDelete(request);
        UserInfo updatedUser = response2.getBody();

        // 결과 검증
        assertNotNull(updatedUser);
        assertTrue(updatedUser.getGroupList().stream()
                .filter(group -> group.getGroupKey().equals(TEST_GROUP_KEY))
                .allMatch(group -> group.getTripScheduleList().stream()
                        .noneMatch(trip -> trip.getTripScheduleName().equals(TEST_TRIP_SCHEDULE_NAME))));
    }
}