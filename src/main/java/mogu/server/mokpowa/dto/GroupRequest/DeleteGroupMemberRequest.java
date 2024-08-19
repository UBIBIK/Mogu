package mogu.server.mokpowa.dto.GroupRequest;

import lombok.Getter;
import lombok.Setter;
import mogu.server.mokpowa.dto.UserInfo;

@Getter
@Setter
public class DeleteGroupMemberRequest {
    private UserInfo userInfo;
    private String groupName;
    private String deleteMemberEmail;

    public DeleteGroupMemberRequest() {}

    public DeleteGroupMemberRequest(UserInfo userInfo, String groupName, String deleteMemberEmail) {
        this.userInfo = userInfo;
        this.groupName = groupName;
        this.deleteMemberEmail = getDeleteMemberEmail();
    }
}
