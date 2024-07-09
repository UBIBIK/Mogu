package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// 사용자 객체
@Getter
@Setter
public class User {
    private String userEmail;
    private String password;
    private String userName;
    private String phoneNumber;
    private List<String> groupKeyList = new ArrayList<>();

    public User() {}

    public User(String userEmail, String password, String userName, String phoneNumber) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.userEmail = userEmail;
        this.password = password;
    }
}