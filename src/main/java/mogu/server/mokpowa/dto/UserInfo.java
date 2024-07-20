package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;

@Getter
@Setter
public class UserInfo {
    private String userEmail;
    private String password;
    private String userName;
    private String phoneNumber;
    private ArrayList<GroupInfo> groupList = new ArrayList<>();

    public UserInfo() {}

    public UserInfo(String userEmail, String userName, String password, String phoneNumber) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.groupList = new ArrayList<>(); // 빈 리스트로 초기화
    }

    public UserInfo(String userEmail, String password, String userName, String phoneNumber, ArrayList<GroupInfo> groupInfo) {
        this.userEmail = userEmail;
        this.password = password;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.groupList = groupInfo;
    }
}
