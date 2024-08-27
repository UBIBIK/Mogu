package mogu.server.mokpowa.RepositoryTest;

import mogu.server.mokpowa.controller.AndroidController;
import mogu.server.mokpowa.dto.LocationInfo;
import mogu.server.mokpowa.dto.TripScheduleInfo;
import mogu.server.mokpowa.dto.TripScheduleRequest.DeleteTripScheduleRequest;
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
    private static final String TEST_TRIP_SCHEDULE_NAME = "목포 여행";
    private static final String TEST_GROUP_MASTER_NAME = "qwe";
    private static final String TEST_GROUP_MEMBER_EMAIL = "1qwe@123";
    private static final String TEST_GROUP_MEMBER_PASSWORD = "123";

    @Autowired
    private TripScheduleRepository tripScheduleRepository;
    @Autowired
    private AndroidController androidController;

    @Test
    public void insertTripScheduleTest() throws Exception {
        // 테스트 여행 일정 생성
        TripScheduleInfo tripScheduleInfo =
                new TripSchedule(TEST_GROUP_KEY, TEST_TRIP_SCHEDULE_NAME,
                        LocalDate.of(2024,8,7),
                        LocalDate.of(2024,8,8));
        tripScheduleInfo.getTripScheduleDetails().getFirst().getLocationInfo().addFirst(
                new LocationInfo("갓바위 문화타운", "전라남도 목포시 남농로 135", 34.79841, 126.3693527, "첫번째 일정")
        );
        tripScheduleInfo.getTripScheduleDetails().getFirst().getLocationInfo().add(1,
                new LocationInfo("목포 올레", "전라남도 목포시 열린길 18", 34.7927524, 126.3760479, "두번째 일정")
        );
        tripScheduleInfo.getTripScheduleDetails().getLast().getLocationInfo().addFirst(
                new LocationInfo("고하도 전망대", "전라남도 목포시 고하도안길 234", 34.7779928, 126.3591099, "세번째 일정")
        );

        UserInfo member = new UserInfo();
        member.setUserEmail(TEST_GROUP_MEMBER_EMAIL);
        member.setUserName(TEST_GROUP_MASTER_NAME);
        Group group = new Group(TEST_TRIP_SCHEDULE_NAME, TEST_GROUP_KEY, member.getUserEmail(), member.getUserName());
        member.getGroupList().add(group);

        tripScheduleRepository.insertTripSchedule((TripSchedule) tripScheduleInfo, member);
    }

    @Test
    public void getTripScheduleDetailTest() throws Exception {
        TripSchedule tripSchedule = tripScheduleRepository.getTripScheduleDetails(TEST_GROUP_KEY);
        System.out.println(tripSchedule.getTripScheduleDetails().getFirst().getLocationInfo().getFirst().getLocationName());
    }

    @Test
    public void updateTripScheduleTest() throws Exception {
        TripSchedule updateTrip =
                tripScheduleRepository.getTripScheduleDetails(TEST_GROUP_KEY);
        updateTrip.getTripScheduleDetails().getFirst().getLocationInfo().getFirst().
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