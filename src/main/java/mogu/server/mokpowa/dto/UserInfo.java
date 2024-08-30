package mogu.server.mokpowa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private String userEmail;
    private String password;
    private String userName;
    private String phoneNumber;
    private ArrayList<GroupInfo> groupList = new ArrayList<>();

    public UserInfo(String userEmail, String userName) {
        this.userEmail = userEmail;
        this.userName = userName;
    }

    public UserInfo(String userEmail, String userName, String password, String phoneNumber) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.groupList = new ArrayList<>(); // 빈 리스트로 초기화
    }
}
