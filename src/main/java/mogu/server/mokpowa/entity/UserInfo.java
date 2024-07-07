package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {
    private String status;
    private String groupKey;
    private String userEmail;
    private String userName;
    private String phoneNumber;

    public UserInfo() {}

    public UserInfo(String groupKey, String userEmail, String phoneNumber, String userName) {
        this.groupKey = groupKey;
        this.userEmail = userEmail;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
    }
}
