package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class GroupInfo {
    private String groupName;
    private String groupKey;
    private String gmEmail;
    private String gmName;
    private ArrayList<GroupMember> groupMember = new ArrayList<>();

    public GroupInfo() {}

    public GroupInfo(String groupName, String groupKey, String gmEmail, String gmName, ArrayList<GroupMember> groupMember) {
        this.groupName = groupName;
        this.groupKey = groupKey;
        this.gmEmail = gmEmail;
        this.gmName = gmName;
        this.groupMember = groupMember;
    }
}
