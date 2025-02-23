package mogu.server.mokpowa.RepositoryTest;


import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.User;
import mogu.server.mokpowa.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private static final String TEST_USER_EMAIL = "test2@example.com";
    private static final String TEST_USER_NAME = "test2";

    @Test
    void insertUser_Test() throws Exception {
        // 새로운 사용자 데이터 정보가 저장되는지 확인
        UserInfo user = new UserInfo(TEST_USER_EMAIL, "123qwe", TEST_USER_NAME, "01012341234");

        userRepository.insertUser(user);
    }

    @Test
    void getUserDatail_Test() throws Exception {
        User finduser = userRepository.getUserDetail(TEST_USER_EMAIL);
        System.out.println(finduser.getUserEmail() + ", " + finduser.getUserName() + ", " + finduser.getPassword());
    }

    @Test
    void deleteUser_Test() throws Exception {
        userRepository.deleteUser(TEST_USER_EMAIL);
    }

    @Test
    void findUserId_Test() throws Exception {
        String findUserId = userRepository.findUserId("오민성", "01012341234");
        System.out.println("찾은 아이디 : " + findUserId);
    }
}