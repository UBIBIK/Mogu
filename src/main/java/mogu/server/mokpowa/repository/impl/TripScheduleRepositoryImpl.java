package mogu.server.mokpowa.repository.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.dto.TripScheduleInfo;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.TripSchedule;
import mogu.server.mokpowa.repository.TripScheduleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TripScheduleRepositoryImpl implements TripScheduleRepository {
    public static final String COLLECTION_NAME = "groups";
    Firestore firestore = FirestoreClient.getFirestore();

    @Override
    public void checkGroupMembership(UserInfo user, TripScheduleInfo tripScheduleInfo) {
        boolean isMember = user.getGroupList().stream()
                .anyMatch(group -> group.getGroupKey().equals(tripScheduleInfo.getGroupKey()));

        if (!isMember) {
            throw new IllegalArgumentException("가입된 그룹이 아닙니다.");
        }
    }

    @Override
    public UserInfo insertTripSchedule(TripScheduleInfo tripScheduleInfo, UserInfo user) throws Exception {
        if (tripScheduleInfo == null) {
            throw new IllegalArgumentException("trip 객체가 null입니다.");
        }

        checkGroupMembership(user, tripScheduleInfo);

        // 그룹 컬렉션 내의 하위 컬렉션에서 여행 일정이 존재하는지 확인하여 있을 경우 예외처리
        CollectionReference groupSchedules = firestore.collection(COLLECTION_NAME)
                .document(tripScheduleInfo.getGroupKey())
                .collection("tripSchedule");
        ApiFuture<QuerySnapshot> future = groupSchedules.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        if (!documents.isEmpty()) {
            throw new Exception("해당 그룹의 여행 일정이 이미 존재합니다.");
        }

        // 하위 컬렉션에 여행 일정 저장
        ApiFuture<WriteResult> resultApiFuture = groupSchedules.document(tripScheduleInfo.getGroupKey()).set(tripScheduleInfo);

        // 생성된 여행 일정을 userInfo에 업데이트하여 반환
        for (GroupInfo groupInfo : user.getGroupList()) {
            if(groupInfo.getGroupKey().equals(tripScheduleInfo.getGroupKey())) {
                groupInfo.getTripScheduleList().add(tripScheduleInfo);
            }
        }
        return user;
    }

    @Override
    public TripSchedule getTripScheduleDetails(String groupKey) throws Exception {
        // 그룹 컬렉션 내의 하위 컬렉션에서 여행 일정을 조회
        CollectionReference tripSchedules = firestore.collection(COLLECTION_NAME)
                .document(groupKey)
                .collection("tripSchedule");

        ApiFuture<QuerySnapshot> querySnapshot = tripSchedules.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        if (!documents.isEmpty()) {
            return documents.getFirst().toObject(TripSchedule.class);
        } else {
            return null; // 해당하는 여행 일정이 존재하지 않을 경우 null 반환
        }
    }

    @Override
    public UserInfo updateTripSchedule(TripScheduleInfo tripSchedule, UserInfo user) throws Exception {
        // groupKey에 해당하는 그룹에서 서브컬렉션인 tripSchedule 문서를 가져옴
        CollectionReference tripScheduleCollection = firestore.collection(COLLECTION_NAME)
                .document(tripSchedule.getGroupKey())
                .collection("tripSchedule");

        // 여행 일정이 존재하는지 확인
        ApiFuture<QuerySnapshot> querySnapshot = tripScheduleCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        if (documents.isEmpty()) {
            throw new Exception("해당 그룹의 여행 일정이 존재하지 않습니다.");
        }

        // 해당 TripSchedule 업데이트
        ApiFuture<WriteResult> writeResult =
                tripScheduleCollection.document(tripSchedule.getGroupKey()).set(tripSchedule);
        writeResult.get();

        // userInfo에 해당 여행 일정 업데이트 후 반환
        for (GroupInfo group : user.getGroupList()) {
            if (group.getGroupKey().equals(tripSchedule.getGroupKey())) {
                group.getTripScheduleList().set(0, tripSchedule);
            }
        }
        return user;
    }

    public UserInfo deleteTripSchedule(String deleteGroupKey, UserInfo user) throws Exception {
        // groupKey에 해당하는 그룹에서 서브컬렉션인 tripSchedule 문서를 가져옴
        CollectionReference tripScheduleCollection = firestore.collection(COLLECTION_NAME)
                .document(deleteGroupKey)
                .collection("tripSchedule");

        // 여행 일정이 존재하는지 확인
        ApiFuture<QuerySnapshot> querySnapshot = tripScheduleCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        if (documents.isEmpty()) {
            throw new Exception("해당 그룹의 여행 일정이 존재하지 않습니다.");
        }

        // 여행 일정 삭제
        for(QueryDocumentSnapshot document : documents) {
            ApiFuture<WriteResult> writeResult = document.getReference().delete();
            writeResult.get();
        }

        // userInfo에서 해당하는 TripScheduleInfo 삭제
        for (GroupInfo group : user.getGroupList()) {
            if (group.getGroupKey().equals(deleteGroupKey)) {
                group.getTripScheduleList().set(0, null);
                break;
            }
        }
        return user;
    }
}
