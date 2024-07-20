package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

// 사용자 객체
@Getter
@Setter
@NoArgsConstructor
public class User {
    @NonNull
    private String userEmail;
    @NonNull
    private String password;
    private String userName;
    private String phoneNumber;
    private List<String> groupKeyList = new ArrayList<>();

    public User(@NotNull String userEmail, @NonNull String password, String userName, String phoneNumber) {
        this.userEmail = userEmail;
        this.password = password;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
    }
}