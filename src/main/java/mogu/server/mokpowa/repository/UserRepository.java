package mogu.server.mokpowa.repository;

import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.User;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface UserRepository {
    String insertUser(UserInfo user) throws Exception; // 사용자 추가

    User getUserDetail(String email) throws Exception; // 사용자 정보 조회

    String updateUser(User user) throws ExecutionException, InterruptedException; // 사용자 정보 수정

    List<User> getUsers() throws Exception; // 모든 사용자 조회
}
