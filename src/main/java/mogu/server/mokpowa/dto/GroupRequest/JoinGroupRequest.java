package mogu.server.mokpowa.dto.GroupRequest;

import lombok.Getter;
import lombok.Setter;
import mogu.server.mokpowa.dto.UserInfo;

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
