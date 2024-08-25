package mogu.server.mokpowa.controller;

import lombok.extern.slf4j.Slf4j;
import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.dto.GroupMember;
import mogu.server.mokpowa.dto.GroupRequest.CreateGroupRequest;
import mogu.server.mokpowa.dto.GroupRequest.DeleteGroupMemberRequest;
import mogu.server.mokpowa.dto.GroupRequest.DeleteGroupRequest;
import mogu.server.mokpowa.dto.GroupRequest.JoinGroupRequest;
import mogu.server.mokpowa.dto.TripScheduleInfo;
import mogu.server.mokpowa.dto.TripScheduleRequest.CreateTripScheduleRequest;
import mogu.server.mokpowa.dto.TripScheduleRequest.DeleteTripScheduleRequest;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.Group;
import mogu.server.mokpowa.entity.TripSchedule;
import mogu.server.mokpowa.entity.User;
import mogu.server.mokpowa.repository.GroupRepository;
import mogu.server.mokpowa.repository.TripScheduleRepository;
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
    private final TripScheduleRepository tripScheduleRepository;

    @Autowired
    public AndroidController(UserRepository userRepository, GroupRepository groupRepository, TripScheduleRepository tripScheduleRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.tripScheduleRepository = tripScheduleRepository;
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

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<UserInfo> loginUser(@RequestBody UserInfo loginUser) throws Exception {
        log.info("입력받은 이메일 = {}", loginUser.getUserEmail());
        log.info("입력받은 비밀번호 = {}", loginUser.getPassword());

        User finduser = userRepository.getUserDetail(loginUser.getUserEmail());
        if (finduser.getPassword().equals(loginUser.getPassword())) {
            loginUser.setUserName(finduser.getUserName());
            loginUser.setPhoneNumber(finduser.getPhoneNumber());
            loginUser.setGroupList(groupRepository.getJoinGroup(finduser));
            log.info("사용자 로그인 성공 이름 : {}", loginUser.getUserName());
            log.info("전화번호 : {}", loginUser.getPhoneNumber());

            if (loginUser.getGroupList() != null) {
                log.info("Group List size: " + loginUser.getGroupList().size());
                for (GroupInfo groupInfo : loginUser.getGroupList()) {
                    log.info("가입된 그룹 정보 : {}", groupInfo.getGroupName());

                    // 각 그룹별로 TripSchedule 추가
                    TripSchedule tripSchedule = tripScheduleRepository.getTripScheduleDetails(groupInfo.getGroupKey());
                    if (tripSchedule != null) {
                        groupInfo.getTripScheduleList().add(tripSchedule); // 그룹에 TripSchedule 리스트 추가
                        log.info("존재하는 여행 일정 정보 : {}", groupInfo.getTripScheduleList().getFirst().getTripScheduleName()); // 해당 여행 일정 이름 출력
                    }
                }
            } else {
                log.info("Group List is null");
            }

            return ResponseEntity.ok(loginUser); // 로그인 성공
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 잘못된 사용자 정보를 입력했을 경우 로그인 실패
        }
    }


    // 그룹 생성
    @PostMapping("/group-create")
    @ResponseBody
    public ResponseEntity<UserInfo> groupCreate(@RequestBody CreateGroupRequest request) throws Exception {
        if(request.getUserInfo() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 사용자 정보가 올바르지 않습니다.
        }
        Group tempGroup;

        do {
            tempGroup = new Group(request.getGroupName(), randomNumber(), request.getUserInfo().getUserEmail(), request.getUserInfo().getUserName());
        }while (groupRepository.groupExists(tempGroup.getGroupKey()));

        request.setUserInfo(groupRepository.insertGroup(tempGroup, request.getUserInfo()));
        log.info("그룹 생성자 : {}", request.getUserInfo().getUserName());
        log.info("그룹 멤버 이메일 : {}", request.getUserInfo().getGroupList().getFirst().getGroupMember().getFirst().getMemberEmail());
        log.info("그룹 멤버 이름 : {}", request.getUserInfo().getGroupList().getFirst().getGroupMember().getFirst().getMemberName());
        for(GroupInfo groupInfo : request.getUserInfo().getGroupList()) {
            log.info("가입된 그룹 정보 : {}", groupInfo.getGroupName());
        }
        return ResponseEntity.ok(request.getUserInfo());
    }

    // 그룹 참가
    @PostMapping("/api/joinGroup")
    public ResponseEntity<UserInfo> joinGroup(@RequestBody JoinGroupRequest request) throws Exception {
        User user = userRepository.getUserDetail(request.getUserInfo().getUserEmail());
        if(user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 사용자 정보가 올바르지 않습니다.
        }

        // 가입된 그룹을 userInfo에 업데이트하여 반환
        GroupInfo JoinGroup = groupRepository.joinGroup(request.getGroupKey(), request.getUserInfo());
        request.getUserInfo().getGroupList().add(JoinGroup);
        user.getGroupKeyList().add(request.getGroupKey());
        userRepository.updateUser(user);

        return ResponseEntity.ok(request.getUserInfo());
    }

    // 그룹 삭제
    @PostMapping("/api/DeleteGroup")
    public ResponseEntity<UserInfo> deleteGroup(@RequestBody DeleteGroupRequest request) throws Exception {
        UserInfo userInfoBeforeDeletion = request.getUserInfo();

        log.info("그룹 삭제 요청 받음 - 사용자: {}, 그룹: {}", userInfoBeforeDeletion.getUserEmail(), request.getGroupName());

        log.info("삭제 전 그룹 목록");
        for (GroupInfo groupInfo : userInfoBeforeDeletion.getGroupList()) {
            log.info("그룹명: {}", groupInfo.getGroupName());
        }

        // 그룹 삭제 수행
        UserInfo updatedUserInfo = groupRepository.deleteGroup(request.getGroupName(), userInfoBeforeDeletion);
        request.setUserInfo(updatedUserInfo);

        log.info("그룹 삭제 완료 - 사용자: {}, 삭제된 그룹: {}", updatedUserInfo.getUserEmail(), request.getGroupName());

        log.info("삭제 후 그룹 목록");
        for (GroupInfo groupInfo : updatedUserInfo.getGroupList()) {
            log.info("그룹명: {}", groupInfo.getGroupName());
        }

        return ResponseEntity.ok(updatedUserInfo);
    }


    // 그룹 멤버 삭제
    @PostMapping("/api/DeleteGroupMember")
    public ResponseEntity<UserInfo> deleteGroupMember(@RequestBody DeleteGroupMemberRequest request) throws Exception {
        log.info("DeleteGroupMemberRequest 수신: 그룹명: {}, 삭제할 멤버 이메일: {}", request.getGroupName(), request.getDeleteMemberEmail());

        UserInfo updatedUserInfo = groupRepository.deleteGroupMember(request.getGroupName(), request.getDeleteMemberEmail(), request.getUserInfo());
        request.setUserInfo(updatedUserInfo);

        log.info("deleteGroupMember 호출 후 업데이트된 UserInfo");
        for(GroupInfo groupInfo : updatedUserInfo.getGroupList()) {
            log.info("그룹: {}", groupInfo.getGroupName());
            for (GroupMember groupMember : groupInfo.getGroupMember()) {
                log.info("그룹 멤버: {}", groupMember.getMemberEmail());
            }
        }

        log.info("업데이트된 UserInfo 반환: 이메일: {}", updatedUserInfo.getUserEmail());
        return ResponseEntity.ok(updatedUserInfo);
    }

    // 여행 일정 생성
    @PostMapping("/api/TripScheduleCreate")
    public ResponseEntity<UserInfo> tripCreate(@RequestBody CreateTripScheduleRequest request) throws Exception {
        User user = userRepository.getUserDetail(request.getUserInfo().getUserEmail());
        if(user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 사용자 정보가 올바르지 않습니다.
        }

        TripSchedule insertTrip = tripScheduleRepository.insertTripSchedule((TripSchedule) request.getTripScheduleInfo(), request.getUserInfo());
        // 생성된 trip을 userInfo에 업데이트하여 반환
        request.getUserInfo().getGroupList().getFirst().getTripScheduleList().add(insertTrip);
        return ResponseEntity.ok(request.getUserInfo());
    }

    // 여행 일정 삭제
    @PostMapping("/api/TripScheduleDelete")
    public ResponseEntity<UserInfo> tripDelete(@RequestBody DeleteTripScheduleRequest request) throws Exception {
        User user = userRepository.getUserDetail(request.getUserInfo().getUserEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 사용자 정보가 올바르지 않습니다.
        }
        // 해당 여행 일정을 삭제한 userInfo에 업데이트하여 반환
        UserInfo updateUser = tripScheduleRepository.deleteTripSchedule(request.getTripScheduleName(), request.getGroupKey(), request.getUserInfo());

        for (GroupInfo group : updateUser.getGroupList()) {
            System.out.println("Group: " + group.getGroupName());
            for (TripScheduleInfo tripSchedule : group.getTripScheduleList()) {
                log.info("tripDelete 후 업데이트된 UserInfo의 TripScheduleName: {}", tripSchedule.getTripScheduleName());
            }
        }

        return ResponseEntity.ok(updateUser);
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