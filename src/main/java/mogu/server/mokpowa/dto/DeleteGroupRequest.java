package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteGroupRequest {
    private UserInfo userInfo;
    private String groupName;

    public DeleteGroupRequest() {}

    public DeleteGroupRequest(UserInfo userInfo, String groupName) {
        this.userInfo = userInfo;
        this.groupName = groupName;
    }
}