package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Map;

@Getter
@Setter
public class Group {
    private String groupName;
    private String groupKey;
    private String groupMaster;
    private ArrayList<Map<String, Object>> groupMember = new ArrayList<>();

    public Group() {}
}