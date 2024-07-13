package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import mogu.server.mokpowa.dto.UserInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// 사용자 객체
@Getter
@Setter
@NoArgsConstructor
public class User extends UserInfo {
    @NonNull
    private String userEmail;
    private String password;
    private String userName;
    private String phoneNumber;
    private List<String> groupKeyList = new ArrayList<>();

    public User(@NotNull String userEmail, String phoneNumber, String userName, String password) {
        this.userEmail = userEmail;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.password = password;
    }
}