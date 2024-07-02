package mogu.server.mokpowa.controller;

import lombok.extern.slf4j.Slf4j;
import mogu.server.mokpowa.entity.UserInfo;
import mogu.server.mokpowa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class AndroidController {

    @Value("${server.host}")
    private String serverHost;

    @Value("${server.port}")
    private int serverPort;

    private final UserRepository userRepository;

    @Autowired
    public AndroidController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // 회원가입
    @PostMapping("/api/signup")
    @ResponseBody
    public String saveUser(@RequestBody UserInfo user) throws Exception {
        log.info("username={}", user.getName());
        log.info("Phone_number={}", user.getPhone());
        log.info("useremail={}", user.getEmail());

        return userRepository.insertUser(user);
    }
}
