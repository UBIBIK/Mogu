package mogu.server.mokpowa.controller;

import lombok.extern.slf4j.Slf4j;
import mogu.server.mokpowa.dto.CreateGroupRequest;
import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.dto.UserInfo;
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
        if(user == null) {
            return "입력한 정보가 올바르지 않습니다.";
        }
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
        log.info("입력받은 비밀번호 = {}", loginUser.getPassword());

        User finduser = userRepository.getUserDetail(loginUser.getUserEmail());
        if(finduser.getPassword().equals(loginUser.getPassword())) {
            loginUser.setGroupList(groupRepository.getJoinGroup(finduser));
            log.info("사용자 로그인 성공 : {}", loginUser.getUserName());
            for(GroupInfo groupInfo : loginUser.getGroupList()) {
                log.info("가입된 그룹 정보 : {}", groupInfo.getGroupName());
            }

            return ResponseEntity.ok(loginUser); // 로그인 성공
        }
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 잘못된 사용자 정보를 입력했을 경우 로그인 실패
        }
    }

    // 그룹 생성
    @PostMapping("/group-create")
    @ResponseBody
    public ResponseEntity<UserInfo> groupCreate(@RequestBody CreateGroupRequest request) throws Exception {
        User user = userRepository.getUserDetail(request.getUserInfo().getUserEmail());
        if(user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 사용자 정보가 올바르지 않습니다.
        }
        Group tempGroup;

        do {
            tempGroup = new Group(request.getGroupName(), randomNumber(), user.getUserEmail(), user.getUserName());
        }while (groupRepository.groupExists(tempGroup.getGroupKey()));
        groupRepository.insertGroup(tempGroup, request.getUserInfo());
        user.getGroupKeyList().add(tempGroup.getGroupKey());
        userRepository.updateUser(user);

        request.getUserInfo().getGroupList().add(tempGroup);
        log.info("그룹 생성자 : {}", user.getUserName());
        log.info("그룹 멤버 이메일 : {}", request.getUserInfo().getGroupList().getFirst().getGroupMember().getFirst().getMemberEmail());
        log.info("그룹 멤버 이름 : {}", request.getUserInfo().getGroupList().getFirst().getGroupMember().getFirst().getMemberName());
        for(GroupInfo groupInfo : request.getUserInfo().getGroupList()) {
            log.info("가입된 그룹 정보 : {}", groupInfo.getGroupName());
        }
        return ResponseEntity.ok(request.getUserInfo());
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