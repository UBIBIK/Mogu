package mogu.server.mokpowa.RepositoryTest;

import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.dto.GroupMember;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.Group;
import mogu.server.mokpowa.entity.User;
import mogu.server.mokpowa.repository.GroupRepository;
import mogu.server.mokpowa.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GroupRepositoryTest {
    private static final String TEST_GROUP_NAME = "jejugo2";
    private static final String TEST_GROUP_MASTER_EMAIL = "3jdnqls@naver.com";
    private static final String TEST_GROUP_MASTER_NAME = "test1";
    private static final String TEST_GROUP_MEMBER_EMAIL = "dhalstjd123@naver.com";
    private static final String TEST_GROUP_MEMBER_NAME = "오민성";
    private static final String TEST_GROUP_KEY = "groupKey2";

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private UserRepository userRepository;

    // 그룹 정보 추가 테스트
    @Test
    public void insertGroupTest() throws Exception {
        UserInfo master = new UserInfo();
        master.setUserEmail(TEST_GROUP_MASTER_EMAIL);
        master.setUserName(TEST_GROUP_MASTER_NAME);
        Group group = new Group(TEST_GROUP_NAME, TEST_GROUP_KEY, master.getUserEmail(), master.getUserName());
        groupRepository.insertGroup(group, master);
    }

    // 그룹 참가 테스트
    @Test
    public void joinGroupTest() throws Exception {
        UserInfo member = new UserInfo();
        member.setUserEmail(TEST_GROUP_MEMBER_EMAIL);
        member.setUserName(TEST_GROUP_MEMBER_NAME);
        Group group = groupRepository.joinGroup(TEST_GROUP_KEY, member);
        for(GroupMember groupMember : group.getGroupMember()) {
            System.out.println(groupMember.getMemberName());
        }
    }

    // 사용자 가입 그룹 조회 테스트
    @Test
    public void getJoinGroupTest() throws Exception {
        User user = userRepository.getUserDetail(TEST_GROUP_MASTER_EMAIL);

        UserInfo master = new UserInfo();
        master.setGroupList(groupRepository.getJoinGroup(user));
        for (GroupInfo groupInfo : master.getGroupList()) {
            System.out.println(groupInfo.getGroupName());
        }
    }

    @Test
    public void deleteGroupTest() throws Exception {
        UserInfo master = new UserInfo();
        master.setUserEmail(TEST_GROUP_MASTER_EMAIL);
        master.setUserName(TEST_GROUP_MASTER_NAME);

        UserInfo result = groupRepository.deleteGroup(TEST_GROUP_NAME, master);

        assertNotNull(result, "Result should not be null");
        assertEquals(TEST_GROUP_MASTER_EMAIL, result.getUserEmail(), "Emails should match");
        assertTrue(result.getGroupList().stream().noneMatch(groupInfo -> groupInfo.getGroupKey().equals(TEST_GROUP_KEY)), "Group list should not contain the deleted group");
    }

    @Test
    public void deleteGroupMemberTest() throws Exception {
        UserInfo master = new UserInfo();
        master.setUserEmail(TEST_GROUP_MASTER_EMAIL);
        master.setUserName(TEST_GROUP_MASTER_NAME);
        UserInfo result = groupRepository.deleteGroupMember(TEST_GROUP_NAME, TEST_GROUP_MEMBER_EMAIL, master);
        // 결과 검증
        System.out.println("deleteGroupMember 호출 후 업데이트된 UserInfo");
        for(GroupInfo groupInfo : result.getGroupList()) {
            System.out.println("그룹: " + groupInfo.getGroupName());
            for (GroupMember groupMember : groupInfo.getGroupMember()) {
                System.out.println("그룹 멤버: " + groupMember.getMemberEmail());
            }
        }
    }
}