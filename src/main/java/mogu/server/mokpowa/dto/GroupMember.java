package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupMember {
    private String memberEmail;
    private String memberName;
    private Double memberLongitude;
    private Double memberLatitude;

    public GroupMember() {}

    public GroupMember(String memberEmail, String memberName, Double memberLongitude, Double memberLatitude) {
        this.memberEmail = memberEmail;
        this.memberName = memberName;
        this.memberLongitude = memberLongitude;
        this.memberLatitude = memberLatitude;
    }
}
