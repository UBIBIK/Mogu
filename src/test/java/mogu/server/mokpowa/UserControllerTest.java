package mogu.server.mokpowa;

import mogu.server.mokpowa.controller.AndroidController;
import mogu.server.mokpowa.dto.LocationInfo;
import mogu.server.mokpowa.dto.TripScheduleRequest.CreateTripScheduleRequest;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.TripSchedule;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    private static final String TEST_GROUP_KEY = "6x6njlutur41ovcv";
    private static final String TEST_GROUP_KEY2 = "4x15torzialiynot";
    private static final String TEST_TRIP_SCHEDULE_NAME = "제주 여행";
    private static final String TEST_GROUP_MASTER_NAME = "qwe";
    private static final String TEST_GROUP_MEMBER_EMAIL = "1qwe@123";
    private static final String TEST_GROUP_MEMBER_PASSWORD = "123";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AndroidController androidController;
    
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
                .andExpect(jsonPath("$.groupList[0].tripScheduleList", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.groupList[0].tripScheduleList[0].tripScheduleName").value("목포 여행"));
    }

    @Test
    public void testTripCreate() throws Exception {
        // 로그인 사용자 정보 설정
        UserInfo loginUser = new UserInfo();
        loginUser.setUserEmail(TEST_GROUP_MEMBER_EMAIL);
        loginUser.setPassword(TEST_GROUP_MEMBER_PASSWORD);

        // 로그인 사용자 정보 받아오기
        ResponseEntity<UserInfo> response = androidController.loginUser(loginUser);
        UserInfo userInfo = response.getBody();

        // 테스트 여행 일정 생성
        TripSchedule tripSchedule =
                new TripSchedule(TEST_GROUP_KEY, TEST_TRIP_SCHEDULE_NAME,
                        "목포 여행 설명",
                        LocalDate.of(2024,8,7),
                        LocalDate.of(2024,8,8));
        tripSchedule.getTripScheduleDetails().getFirst().getLocations().addFirst(
                new LocationInfo("목포해상케이블카", 34.79841, 126.3693527)
        );
        tripSchedule.getTripScheduleDetails().getFirst().getLocations().addFirst(
                new LocationInfo("목포 호텔 드메르", 34.7971005, 126.4265178)
        );
        tripSchedule.getTripScheduleDetails().getLast().getLocations().addFirst(
                new LocationInfo("고하도 전망대", 34.7779928, 126.3591099)
        );

        CreateTripScheduleRequest request = new CreateTripScheduleRequest(userInfo, tripSchedule);

        // 여행 일정 생성
        ResponseEntity<UserInfo> response2 = androidController.tripCreate(request);
        UserInfo updatedUser = response2.getBody();

        // 결과 검증
        assertNotNull(updatedUser);
        assertTrue(updatedUser.getGroupList().stream()
                .filter(group -> group.getGroupKey().equals(TEST_GROUP_KEY))
                .allMatch(group -> group.getTripScheduleList().stream()
                        .noneMatch(trip -> trip.getTripScheduleName().equals(TEST_TRIP_SCHEDULE_NAME))));
    }
}