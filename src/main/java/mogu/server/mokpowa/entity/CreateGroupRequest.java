package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.Setter;
import mogu.server.mokpowa.dto.UserInfo;

@Getter
@Setter
public class CreateGroupRequest {
    private UserInfo userInfo;
    private String groupName;

    public CreateGroupRequest() {}
}