package mogu.server.mokpowa.dto.GroupRequest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mogu.server.mokpowa.dto.UserInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteGroupRequest {
    private UserInfo userInfo;
    private String groupName;
}