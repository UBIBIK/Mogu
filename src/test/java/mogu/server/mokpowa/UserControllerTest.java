package mogu.server.mokpowa;

import mogu.server.mokpowa.controller.AndroidController;
import mogu.server.mokpowa.dto.LocationInfo;
import mogu.server.mokpowa.dto.TripScheduleInfo;
import mogu.server.mokpowa.dto.TripScheduleRequest.CreateTripScheduleRequest;
import mogu.server.mokpowa.dto.TripScheduleRequest.DeleteTripScheduleRequest;
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
        UserInfo userInfo = response.getBody();

        // 테스트 여행 일정 생성
        TripScheduleInfo tripScheduleInfo =
                new TripSchedule(TEST_GROUP_KEY,
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

        CreateTripScheduleRequest request = new CreateTripScheduleRequest(userInfo, tripScheduleInfo);

        // 여행 일정 생성
        ResponseEntity<UserInfo> response2 = androidController.tripCreate(request);
        UserInfo updatedUser = response2.getBody();

        // 결과 검증
        assert updatedUser != null;
        System.out.println(updatedUser.getGroupList().getFirst().getTripScheduleList().getFirst().getTripScheduleDetails().getFirst().getLocationInfo().getFirst().getAddress());
    }

    @Test
    public void testTripDelete() throws Exception {
        UserInfo loginUser = new UserInfo();
        loginUser.setUserEmail(TEST_GROUP_MEMBER_EMAIL);
        loginUser.setPassword(TEST_GROUP_MEMBER_PASSWORD);

        // 로그인 사용자 정보 받아오기
        ResponseEntity<UserInfo> response = androidController.loginUser(loginUser);
        UserInfo userInfo = response.getBody();

        DeleteTripScheduleRequest request = new DeleteTripScheduleRequest(userInfo, TEST_GROUP_KEY);

        ResponseEntity<UserInfo> response2 = androidController.tripDelete(request);
        UserInfo updatedUser = response2.getBody();
    }
}