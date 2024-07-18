package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class GroupInfo {
    private String GroupName;
    private String GroupKey;
    private String GM_Email;
    private String GM_Name;
    private ArrayList<GroupMember> GroupMember = new ArrayList<>();

    public GroupInfo() {}

    public GroupInfo(String groupName, String groupKey, String GM_Email, String GM_Name, ArrayList<GroupMember> groupMember) {
        GroupName = groupName;
        GroupKey = groupKey;
        this.GM_Email = GM_Email;
        this.GM_Name = GM_Name;
        GroupMember = groupMember;
    }
}
