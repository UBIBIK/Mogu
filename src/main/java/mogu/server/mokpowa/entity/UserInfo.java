package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {
    private String userEmail;
    private String password;
    private String userName;
    private String phoneNumber;

    public UserInfo() {}

    public UserInfo(String userEmail, String phoneNumber, String userName, String password) {
        this.userEmail = userEmail;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.password = password;
    }
}
