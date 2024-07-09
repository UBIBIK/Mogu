package mogu.server.mokpowa.repository;

import mogu.server.mokpowa.entity.Group;
import mogu.server.mokpowa.entity.UserInfo;

import java.util.concurrent.ExecutionException;

public interface GroupRepository {
    String insertGroup(Group group, UserInfo user) throws ExecutionException, InterruptedException; // 그룹 추가

    Group addGroupMember(String groupKey, UserInfo user) throws Exception; // 그룹 멤버 추가

    String updateGroup(Group group) throws Exception; // 그룹 정보 수정
}
