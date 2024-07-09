package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Map;

@Getter
@Setter
public class Group {
    private String groupKey;
    private String groupName;
    private String groupMaterEmail;
    private ArrayList<Map<String, Object>> groupMember = new ArrayList<>();

    public Group() {}

    public Group(String groupName, String groupKey) {
        this.groupName = groupName + "의 그룹";
        this.groupKey = groupKey;
    }
}
