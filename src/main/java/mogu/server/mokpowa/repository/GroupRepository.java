package mogu.server.mokpowa.repository;

import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.entity.Group;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.User;

import java.util.ArrayList;

public interface GroupRepository {
    String insertGroup(Group group, UserInfo user) throws Exception; // 그룹 추가

    Group getGroupDatail(String groupKey) throws Exception; // 그룹 정보 조회
    
    boolean groupExists(String groupKey); // 그룹 존재 여부 확인

    ArrayList<GroupInfo> getJoinGroup(User user) throws Exception; // 사용자 가입 그룹 조회

    Group addGroupMember(String groupKey, UserInfo user) throws Exception; // 그룹 멤버 추가

    String updateGroup(Group group) throws Exception; // 그룹 정보 수정
}
