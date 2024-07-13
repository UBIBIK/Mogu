package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserInfo {
    @NonNull
    private String userEmail;
    @NonNull
    private String password;
    private String userName;
    private String phoneNumber;
    private List<String> groupKeyList = new ArrayList<>();

    public UserInfo(@NonNull String userEmail, String phoneNumber, String userName) {
        this.userEmail = userEmail;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
    }
}
