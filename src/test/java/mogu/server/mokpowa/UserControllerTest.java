package mogu.server.mokpowa;

import mogu.server.mokpowa.controller.AndroidController;
import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.dto.LocationInfo;
import mogu.server.mokpowa.dto.TripScheduleInfo;
import mogu.server.mokpowa.dto.TripScheduleRequest.CreateTripScheduleRequest;
import mogu.server.mokpowa.dto.TripScheduleRequest.DeleteTripScheduleRequest;
import mogu.server.mokpowa.dto.TripScheduleRequest.UpdateTripScheduleRequest;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.TripSchedule;
import mogu.server.mokpowa.repository.TripScheduleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    private static final String TEST_GROUP_KEY = "6x6njlutur41ovcv";
    private static final String TEST_GROUP_KEY2 = "4x15torzialiynot";
    private static final String TEST_GROUP_MASTER_NAME = "qwe";
    private static final String TEST_GROUP_MEMBER_EMAIL = "1qwe@123";
    private static final String TEST_GROUP_MEMBER_PASSWORD = "123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AndroidController androidController;
    @Autowired
    private TripScheduleRepository tripScheduleRepository;

    @Test
    public void testLoginUser() throws Exception {
        String loginUserJson = "{ \"userEmail\": \"1qwe@123\", \"password\": \"123\" }";

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("qwe"))
                .andExpect(jsonPath("$.phoneNumber").value("01011115555"))
                .andExpect(jsonPath("$.groupList", hasSize(greaterThan(1))))
                .andExpect(jsonPath("$.groupList[0].groupName").value("mokpogo"))
                .andExpect(jsonPath("$.groupList[0].tripScheduleList", hasSize(greaterThan(0))));
    }

    @Test
    public void testTripCreate() throws Exception {
        // 로그인 사용자 정보 설정
        UserInfo loginUser = new UserInfo();
        loginUser.setUserEmail(TEST_GROUP_MEMBER_EMAIL);
        loginUser.setPassword(TEST_GROUP_MEMBER_PASSWORD);

        // 로그인 사용자 정보 받아오기
        ResponseEntity<UserInfo> response = androidController.loginUser(loginUser);
        if (response == null || response.getBody() == null) {
            throw new Exception("로그인 실패: 사용자 정보를 받아오지 못했습니다.");
        }
        UserInfo userInfo = response.getBody();

        // 테스트 여행 일정 생성
        TripScheduleInfo tripScheduleInfo = new TripSchedule(TEST_GROUP_KEY,
                LocalDate.of(2024, 8, 7), LocalDate.of(2024, 8, 8));

        // TripScheduleDetails와 LocationInfo가 제대로 초기화되었는지 체크
        if (tripScheduleInfo.getTripScheduleDetails().isEmpty()) {
            throw new Exception("TripScheduleDetails가 초기화되지 않았습니다.");
        }

        // 첫 번째 일정 장소 추가
        tripScheduleInfo.getTripScheduleDetails().getFirst().getLocationInfo().addFirst(
                new LocationInfo("갓바위 문화타운", "전라남도 목포시 남농로 135", 34.79841, 126.3693527,
                        "http://tong.visitkorea.or.kr/cms/resource/33/2678633_image2_1.jpg", "첫번째 일정"));

        // 두 번째 일정 장소 추가
        tripScheduleInfo.getTripScheduleDetails().getFirst().getLocationInfo().add(1,
                new LocationInfo("목포 올레", "전라남도 목포시 열린길 18", 34.7927524, 126.3760479,
                        "http://tong.visitkorea.or.kr/cms/resource/33/2678633_image2_1.jpg", "두번째 일정"));

        // 세 번째 일정 장소 추가
        tripScheduleInfo.getTripScheduleDetails().getLast().getLocationInfo().addFirst(
                new LocationInfo("고하도 전망대", "전라남도 목포시 고하도안길 234", 34.7779928, 126.3591099,
                        "http://tong.visitkorea.or.kr/cms/resource/33/2678633_image2_1.jpg", "세번째 일정"));

        // 요청 생성
        CreateTripScheduleRequest request = new CreateTripScheduleRequest(userInfo, tripScheduleInfo);

        // 여행 일정 생성
        ResponseEntity<UserInfo> response2 = androidController.tripCreate(request);
        if (response2 == null || response2.getBody() == null) {
            throw new Exception("일정 생성 실패: 사용자 정보를 받아오지 못했습니다.");
        }
        UserInfo updatedUser = response2.getBody();

        // 결과 검증
        assertNotNull(updatedUser, "업데이트된 사용자 정보가 null입니다.");
        assertNotNull(updatedUser.getGroupList(), "그룹 목록이 null입니다.");
        assertFalse(updatedUser.getGroupList().isEmpty(), "그룹 목록이 비어 있습니다.");

        // 그룹의 첫 번째 일정에서 첫 번째 위치 정보의 주소 확인
        GroupInfo groupInfo = updatedUser.getGroupList().getFirst();
        TripScheduleInfo createdTrip = groupInfo.getTripScheduleList().getFirst();
        LocationInfo firstLocation = createdTrip.getTripScheduleDetails().getFirst().getLocationInfo().getFirst();

        assertNotNull(firstLocation, "첫 번째 위치 정보가 null입니다.");
        assertEquals("전라남도 목포시 남농로 135", firstLocation.getAddress(), "첫 번째 위치의 주소가 예상과 다릅니다.");

        // 로그 대신 검증
        System.out.println("첫 번째 위치의 주소: " + firstLocation.getAddress());
    }

    @Test
    public void testTripDelete() throws Exception {
        UserInfo loginUser = new UserInfo();
        loginUser.setUserEmail(TEST_GROUP_MEMBER_EMAIL);
        loginUser.setPassword(TEST_GROUP_MEMBER_PASSWORD);

        // 로그인 사용자 정보 받아오기
        ResponseEntity<UserInfo> response = androidController.loginUser(loginUser);
        if (response == null || response.getBody() == null) {
            throw new Exception("로그인 실패: 사용자 정보를 받아오지 못했습니다.");
        }
        UserInfo userInfo = response.getBody();

        DeleteTripScheduleRequest request = new DeleteTripScheduleRequest(userInfo, TEST_GROUP_KEY);

        // 일정 삭제 요청
        ResponseEntity<UserInfo> response2 = androidController.tripDelete(request);
        if (response2 == null || response2.getBody() == null) {
            throw new Exception("일정 삭제 실패: 업데이트된 사용자 정보를 받아오지 못했습니다.");
        }
        UserInfo updatedUser = response2.getBody();

        // 그룹 일정 삭제 확인
        assertNotNull(updatedUser);
        assertNotNull(updatedUser.getGroupList());
        for (GroupInfo groupInfo : updatedUser.getGroupList()) {
            assertTrue(groupInfo.getTripScheduleList().isEmpty(), "그룹 일정 정보가 삭제되지 않았습니다");
        }
    }


    @Test
    public void testTripUpdate() throws Exception {
        // 로그인 정보 설정
        UserInfo loginUser = new UserInfo();
        loginUser.setUserEmail(TEST_GROUP_MEMBER_EMAIL);
        loginUser.setPassword(TEST_GROUP_MEMBER_PASSWORD);

        // 로그인 사용자 정보 받아오기
        ResponseEntity<UserInfo> response = androidController.loginUser(loginUser);
        if (response == null || response.getBody() == null) {
            throw new Exception("로그인 실패: 사용자 정보를 받아오지 못했습니다.");
        }
        UserInfo userInfo = response.getBody();

        // 일정 업데이트 정보 가져오기
        TripScheduleInfo updateTrip = tripScheduleRepository.getTripScheduleDetails(TEST_GROUP_KEY);
        if (updateTrip == null || updateTrip.getTripScheduleDetails() == null) {
            throw new Exception("일정 정보를 받아오지 못했습니다.");
        }

        // 여행 일정의 장소 이름 변경
        updateTrip.getTripScheduleDetails().getFirst().getLocationInfo().getFirst()
                .setLocationName("가톨릭 목포 성지");

        // 업데이트 요청 생성
        UpdateTripScheduleRequest request = new UpdateTripScheduleRequest(userInfo, updateTrip);

        // 일정 업데이트 요청
        ResponseEntity<UserInfo> response2 = androidController.tripUpdate(request);
        if (response2 == null || response2.getBody() == null) {
            throw new Exception("일정 업데이트 실패: 사용자 정보를 받아오지 못했습니다.");
        }
        UserInfo updatedUser = response2.getBody();

        // 업데이트된 정보 검증
        assertNotNull(updatedUser);
        assertNotNull(updatedUser.getGroupList());

        // 특정 그룹의 업데이트된 여행 일정 검증
        boolean isTripUpdated = false;
        for (GroupInfo groupInfo : updatedUser.getGroupList()) {
            if (groupInfo.getGroupKey().equals(updateTrip.getGroupKey())) {
                // 실제로 일정 이름이 업데이트되었는지 확인
                String updatedLocationName = groupInfo.getTripScheduleList().getFirst()
                        .getTripScheduleDetails().getFirst().getLocationInfo().getFirst().getLocationName();
                assertEquals("가톨릭 목포 성지", updatedLocationName, "장소 이름이 예상대로 업데이트되지 않았습니다.");
                isTripUpdated = true;
            }
        }

        // 만약 그룹이 일치하는 그룹을 찾지 못한 경우 테스트 실패 처리
        assertTrue(isTripUpdated, "일정이 업데이트된 그룹을 찾지 못했습니다.");
    }
}