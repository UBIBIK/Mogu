package mogu.server.mokpowa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {
    private String memberEmail;
    private String memberName;
    private Double memberLongitude;
    private Double memberLatitude;
}
