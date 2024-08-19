package mogu.server.mokpowa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$.groupList[0].groupName").value("jejugo"))
                .andExpect(jsonPath("$.groupList[0].tripScheduleList", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.groupList[0].tripScheduleList[0].tripScheduleName").value("제주 여행"));
    }
}