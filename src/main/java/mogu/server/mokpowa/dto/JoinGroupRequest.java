package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinGroupRequest {
    private UserInfo userInfo;
    private String groupKey;

    public JoinGroupRequest() {    }

    public JoinGroupRequest(UserInfo userInfo, String groupKey) {
        this.userInfo = userInfo;
        this.groupKey = groupKey;
    }
}
