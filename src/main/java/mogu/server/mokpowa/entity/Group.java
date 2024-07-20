package mogu.server.mokpowa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.dto.GroupMember;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
public class Group extends GroupInfo {
    private String groupName;
    @NonNull
    private String groupKey;
    @NonNull
    private String gmEmail;
    private String gmName;
    private ArrayList<GroupMember> GroupMember = new ArrayList<>();

    public Group() {}

    public Group(String groupName, @NonNull String groupKey, @NonNull String GM_Email, String GM_Name) {
        this.groupName = groupName;
        this.groupKey = groupKey;
        this.gmEmail = GM_Email;
        this.gmName = GM_Name;
    }
}
