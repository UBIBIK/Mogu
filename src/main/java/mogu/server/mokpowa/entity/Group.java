package mogu.server.mokpowa.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.dto.GroupMember;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
public class Group extends GroupInfo {
    private String GroupName;
    @NonNull
    private String GroupKey;
    @NonNull
    private String GM_Email;
    private String GM_Name;
    private ArrayList<GroupMember> GroupMember = new ArrayList<>();

    public Group(String groupName, @NonNull String groupKey, @NonNull String GM_Email, String GM_Name) {
        GroupName = groupName;
        GroupKey = groupKey;
        this.GM_Email = GM_Email;
        this.GM_Name = GM_Name;
    }
}
