package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {
    private String email;
    private String name;
    private String password;
    private String phone;

    public UserInfo() {}

    public UserInfo(String email, String name, String password, String phone) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.phone = phone;
    }
}
