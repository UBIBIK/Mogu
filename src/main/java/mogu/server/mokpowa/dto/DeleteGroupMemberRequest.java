package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.Setter;

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
