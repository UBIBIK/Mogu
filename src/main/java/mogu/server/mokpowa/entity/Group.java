package mogu.server.mokpowa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Group {
    @NonNull
    private String groupKey;
    private String groupName;
    private String groupMaterEmail;
    private ArrayList<Map<String, Object>> groupMember = new ArrayList<>();

    public Group(@NotNull String groupKey, String groupName, String groupMaterEmail) {
        this.groupKey = groupKey;
        this.groupName = groupName;
        this.groupMaterEmail = groupMaterEmail;
    }

    public Group(@NotNull String groupKey, String groupName) {
        this.groupKey = groupKey;
        this.groupName = groupName;
    }
}
