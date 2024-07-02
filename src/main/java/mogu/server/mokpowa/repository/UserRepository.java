package mogu.server.mokpowa.repository;

import mogu.server.mokpowa.entity.UserInfo;

public interface UserRepository {
    String insertUser(UserInfo user) throws Exception; // 사용자 추가
}
