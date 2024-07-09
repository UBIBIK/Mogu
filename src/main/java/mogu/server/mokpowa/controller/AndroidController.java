package mogu.server.mokpowa.controller;

import lombok.extern.slf4j.Slf4j;
import mogu.server.mokpowa.entity.*;
import mogu.server.mokpowa.repository.GroupRepository;
import mogu.server.mokpowa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController
public class AndroidController {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public AndroidController(UserRepository userRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
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

    // 그룹 생성
    @PostMapping("/group-create")
    @ResponseBody
    public ResponseEntity<UserInfo> groupCreate(@RequestBody UserInfo user) throws Exception {
        List<User> userList = userRepository.getUsers();
        Group tempGroup = null;

        for (User finduser : userList) {
            if (finduser.getUserEmail().equals(user.getUserEmail())) {
                tempGroup = new Group(user.getUserName(), randomNumber());
                tempGroup.setGroupMaterEmail(user.getUserEmail());
                finduser.getGroupKeyList().add(tempGroup.getGroupKey());
                user.getGroupKeyList().add(tempGroup.getGroupKey());
                groupRepository.insertGroup(tempGroup, user);
                log.info("그룹 생성 : {}", user.getUserName());
                groupRepository.addGroupMember(tempGroup.getGroupKey(), user);
                userRepository.updateUser(finduser);
                return ResponseEntity.ok(user);
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(user);
    }

    // 난수 생성 함수
    public String randomNumber() {
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }
        return result.toString();
    }
}