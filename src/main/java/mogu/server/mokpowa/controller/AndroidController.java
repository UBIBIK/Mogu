package mogu.server.mokpowa.controller;

import lombok.extern.slf4j.Slf4j;
import mogu.server.mokpowa.entity.UserInfo;
import mogu.server.mokpowa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AndroidController {

    private final UserRepository userRepository;

    @Autowired
    public AndroidController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // 회원가입
    @PostMapping("/api/signup")
    @ResponseBody
    public String saveUser(@RequestBody UserInfo user) throws Exception {
        log.info("username={}", user.getUserName());
        log.info("Phone_number={}", user.getPhoneNumber());
        log.info("useremail={}", user.getUserEmail());

        return userRepository.insertUser(user);
    }

    // 로그인
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<UserInfo> loginUser(@RequestBody UserInfo loginUser) throws Exception {
        log.info("입력받은 이메일 = {}", loginUser.getUserEmail());
        log.info("입력받은 비밀번호 = {}", loginUser.getUserEmail());

        UserInfo finduser = userRepository.getUserDetail(loginUser.getUserEmail());
        if(finduser.getPassword().equals(loginUser.getPassword())) {
            log.info("사용자 로그인 성공 : {}", finduser.getUserName());
            return ResponseEntity.ok(finduser); // 로그인 성공
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 잘못된 사용자 정보를 입력했을 경우 로그인 실패
        }
    }
}