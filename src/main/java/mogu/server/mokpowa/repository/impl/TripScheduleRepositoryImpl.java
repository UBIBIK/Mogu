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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

@Service
public class TripScheduleRepositoryImpl implements TripScheduleRepository {
    public static final String COLLECTION_NAME = "groups";
    Firestore firestore = FirestoreClient.getFirestore();

    @Override
    public void checkGroupMembership(UserInfo user, TripSchedule trip) {
        boolean isMember = user.getGroupList().stream()
                .anyMatch(group -> group.getGroupKey().equals(trip.getGroupKey()));

        if (!isMember) {
            throw new IllegalArgumentException("가입된 그룹이 아닙니다.");
        }
    }

    @Override
    public TripSchedule insertTripSchedule(TripSchedule tripSchedule, UserInfo user) throws Exception {
        if (tripSchedule == null) {
            throw new IllegalArgumentException("trip 객체가 null입니다.");
        }

        checkGroupMembership(user, tripSchedule);

        // 그룹 컬렉션 내의 하위 컬렉션에서 여행 일정이 존재하는지 확인하여 있을 경우 예외처리
        CollectionReference groupSchedules = firestore.collection(COLLECTION_NAME)
                .document(tripSchedule.getGroupKey())
                .collection("tripSchedule");
        ApiFuture<QuerySnapshot> future = groupSchedules.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        if (!documents.isEmpty()) {
            throw new Exception("해당 그룹의 여행 일정이 이미 존재합니다.");
        }

        // 하위 컬렉션에 여행 일정 저장
        ApiFuture<WriteResult> resultApiFuture = groupSchedules.document().set(tripSchedule);
        return tripSchedule;
    }

    @Override
    public TripSchedule getTripScheduleDetails(String groupKey) throws Exception {
        // 그룹 컬렉션 내의 하위 컬렉션에서 여행 일정을 조회
        CollectionReference groupSchedules = firestore.collection("groups")
                .document(groupKey)
                .collection("tripSchedule");

        ApiFuture<QuerySnapshot> querySnapshot = groupSchedules.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        if (!documents.isEmpty()) {
            return documents.getFirst().toObject(TripSchedule.class);
        } else {
            return null; // 해당하는 여행 일정이 존재하지 않을 경우 null 반환
        }
    }

    @Override
    public String updateTripSchedule(TripSchedule tripSchedule) throws Exception {
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME)
                .document(tripSchedule.getGroupKey())
                .collection("tripSchedule")
                .document()
                .set(tripSchedule);

        return future.get().getUpdateTime().toString();
    }

    /*public UserInfo deleteTripSchedule(String deleteTripScheduleName, String deleteGroupKey, UserInfo user) throws Exception {
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
        for (QueryDocumentSnapshot document : documents) {
            if (Objects.equals(document.getString("tripScheduleName"), deleteTripScheduleName)) {
                document.getReference().delete();
                break;
            }
        }

        // userInfo에서 해당하는 TripScheduleInfo 삭제
        for (GroupInfo group : user.getGroupList()) {
            if (group.getGroupKey().equals(deleteGroupKey)) {
                Iterator<TripScheduleInfo> iterator = group.getTripScheduleList().iterator();
                while (iterator.hasNext()) {
                    TripScheduleInfo tripSchedule = iterator.next();
                    if (tripSchedule.getTripScheduleDetails().removeIf(equals())).equals(deleteTripScheduleName)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return user;
    }*/
}
