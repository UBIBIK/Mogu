package mogu.server.mokpowa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationInfo {
    private String locationName; // 장소 이름
    private String address; // 도로명 주소
    private Double latitude; // 장소 위도 추후 수정 가능
    private Double longitude; // 장소 경도 추후 수정 가능
    private String note; // 메모
}
