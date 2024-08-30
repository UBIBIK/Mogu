package mogu.server.mokpowa.entity;

import lombok.*;
import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.dto.GroupMember;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Group extends GroupInfo {
    @NonNull
    private String groupName;
    @NonNull
    private String groupKey;
    @NonNull
    private String gmEmail;
    @NonNull
    private String gmName;
    private ArrayList<GroupMember> groupMember = new ArrayList<>();

    public Group(@NonNull String groupName, @NonNull String groupKey, @NonNull String GM_Email, @NonNull String GM_Name) {
        this.groupName = groupName;
        this.groupKey = groupKey;
        this.gmEmail = GM_Email;
        this.gmName = GM_Name;
    }
}
