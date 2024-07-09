package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserInfo {
    private String userEmail;
    private String password;
    private String userName;
    private String phoneNumber;
    private List<String> groupKeyList = new ArrayList<>();


    public UserInfo() {}

    public UserInfo(String userEmail, String phoneNumber, String userName, String password) {
        this.userEmail = userEmail;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.password = password;
    }
}
