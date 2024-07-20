package mogu.server.mokpowa.RepositoryTest;

import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.entity.Group;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.User;
import mogu.server.mokpowa.repository.GroupRepository;
import mogu.server.mokpowa.repository.impl.UserRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;

@SpringBootTest
public class GroupRepositoryTest {
    private static final String TEST_GROUP_NAME = "test1";
    private static final String TEST_GROUP_MASTER_EMAIL = "3jdnqls@naver.com";
    private static final String TEST_GROUP_MASTER_NAME = "test1";
    private static final String TEST_GROUP_MEMBER_EMAIL = "test2";
    private static final String TEST_GROUP_MEMBER_NAME = "test2";
    private static final String TEST_GROUP_KEY = "8et62mcnqqp5qk66";

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    // 그룹 정보 추가 테스트
    @Test
    public void insertGroupTest() throws Exception {
        UserInfo master = new UserInfo();
        master.setUserEmail(TEST_GROUP_MASTER_EMAIL);
        master.setUserName(TEST_GROUP_MASTER_NAME);
        Group group = new Group(TEST_GROUP_NAME, TEST_GROUP_KEY, master.getUserEmail(), master.getUserName());
        groupRepository.insertGroup(group, master);
    }

    // 사용자 가입 그룹 조회 테스트
    @Test
    public void getJoinGroupTest() throws Exception {
        User user = userRepositoryImpl.getUserDetail(TEST_GROUP_MASTER_EMAIL);

        UserInfo master = new UserInfo();
        master.setGroupList(groupRepository.getJoinGroup(user));
        for (GroupInfo groupInfo : master.getGroupList()) {
            System.out.println(master.getGroupList().getFirst().getGroupName());
        }
    }
}
