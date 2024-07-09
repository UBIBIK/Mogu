package mogu.server.mokpowa.RepositoryTest;

import mogu.server.mokpowa.entity.Group;
import mogu.server.mokpowa.entity.UserInfo;
import mogu.server.mokpowa.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GroupRepositoryTest {
    private static final String TEST_GROUP_NAME = "test1";
    private static final String TEST_GROUP_MASTER_EMAIL = "test1@example.com";
    private static final String TEST_GROUP_MASTER_NAME = "test1";
    private static final String TEST_GROUP_MEMBER_EMAIL = "test2";
    private static final String TEST_GROUP_MEMBER_NAME = "test2";
    private static final String TEST_GROUP_KEY = "8et62mcnqqp5qk66";

    @Autowired
    private GroupRepository groupRepository;

    // 그룹 정보 추가 테스트
    @Test
    public void insertGroupTest() throws Exception {
        Group group = new Group(TEST_GROUP_NAME, TEST_GROUP_KEY);
        UserInfo master = new UserInfo();
        master.setUserEmail(TEST_GROUP_MASTER_EMAIL);
        master.setUserName(TEST_GROUP_MASTER_NAME);
        groupRepository.insertGroup(group, master);
    }
}
