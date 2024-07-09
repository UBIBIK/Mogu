package mogu.server.mokpowa.repository.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import mogu.server.mokpowa.entity.Group;
import mogu.server.mokpowa.entity.UserInfo;
import mogu.server.mokpowa.repository.GroupRepository;
import mogu.server.mokpowa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class GroupRepositoryImpl implements GroupRepository {
    public static final String COLLECTION_NAME = "groups";
    Firestore firestore = FirestoreClient.getFirestore();
    private final UserRepository userRepository;

    @Autowired
    public GroupRepositoryImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String insertGroup(Group group, UserInfo user) throws ExecutionException, InterruptedException {
        if (group == null) {
            throw new IllegalArgumentException("Group 객체가 null입니다.");
        }

        // 파이어베이스에 동일한 groupname을 가진 그룹이 있는지 확인
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(group.getGroupName());
        ApiFuture<DocumentSnapshot> futureSnapshot = docRef.get();
        DocumentSnapshot documentSnapshot = futureSnapshot.get();

        if (documentSnapshot.exists()) {
            // 동일한 groupname을 가진 사용자가 이미 존재하면 예외
            throw new IllegalArgumentException("동일한 그룹 이름을 가진 그룹이 이미 존재합니다.");
        }

        // 그룹 정보 저장
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME).document(group.getGroupName()).set(group);

        // 성공적으로 저장되었을 때의 시간을 반환
        return future.get().getUpdateTime().toString();
    }

    @Override
    public String updateGroup(Group group) throws Exception {
        ApiFuture<WriteResult> future =
                firestore.collection(COLLECTION_NAME).document(group.getGroupName()).set(group);
        return future.get().getUpdateTime().toString();
    }

    // 그룳 멤버 추가
    @Override
    public Group addGroupMember(String groupKey, UserInfo user) throws Exception {
        // Firestore에서 그룹 컬렉션을 참조
        CollectionReference groupCollection = firestore.collection(COLLECTION_NAME);

        // 그룹 키로 그룹 문서를 조회
        ApiFuture<QuerySnapshot> future = groupCollection.whereEqualTo("groupKey", groupKey).get();

        // 문서 결과를 가져옴
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        // 문서가 존재하는 경우
        if (!documents.isEmpty()) {
            // 첫 번째 문서를 가져옴
            QueryDocumentSnapshot document = documents.getFirst();

            // 문서를 Group 객체로 변환
            Group group = document.toObject(Group.class);

            // 그룹 멤버 리스트를 가져옴
            List<Map<String, Object>> groupMembers = group.getGroupMember();

            // 그룹 멤버 리스트를 순회하며 중복 멤버를 검사
            for (Map<String, Object> member : groupMembers) {
                if (member.get("groupMemberName").equals(user.getUserEmail())) {
                    throw new Exception("해당 이름의 그룹 멤버가 이미 존재합니다.");
                }
            }

            // 새로운 멤버 정보를 생성
            Map<String, Object> newMemberInfo = new HashMap<>();
            newMemberInfo.put("groupMemberEmail", user.getUserEmail());

            // 그룹 멤버 리스트에 새로운 멤버 정보를 추가
            group.getGroupMember().add(newMemberInfo);

            // 그룹 정보를 업데이트
            updateGroup(group);

            // 업데이트된 그룹 정보를 반환
            return group;
        }

        // 문서가 존재하지 않는 경우 예외를 발생
        throw new Exception("해당 유저의 그룹 키가 존재하지 않습니다.");
    }
}
