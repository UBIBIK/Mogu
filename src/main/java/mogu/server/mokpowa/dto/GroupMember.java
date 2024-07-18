package mogu.server.mokpowa.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupMember {
    private String MemberEmail;
    private String MemberName;
    private Double MemberLongitude;
    private Double MemberLatitude;

    public GroupMember() {}

    public GroupMember(String memberEmail, String memberName, Double memberLongitude, Double memberLatitude) {
        MemberEmail = memberEmail;
        MemberName = memberName;
        MemberLongitude = memberLongitude;
        MemberLatitude = memberLatitude;
    }
}
