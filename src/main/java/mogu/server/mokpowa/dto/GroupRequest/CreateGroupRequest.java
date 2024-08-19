package mogu.server.mokpowa.dto.GroupRequest;

import lombok.Getter;
import lombok.Setter;
import mogu.server.mokpowa.dto.UserInfo;

@Getter
@Setter
public class CreateGroupRequest {
    private UserInfo userInfo;
    private String groupName;

    public CreateGroupRequest() {}

    public CreateGroupRequest(UserInfo userInfo, String groupName) {
        this.userInfo = userInfo;
        this.groupName = groupName;
    }
}