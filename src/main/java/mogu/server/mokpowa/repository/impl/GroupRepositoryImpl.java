package mogu.server.mokpowa.repository.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import mogu.server.mokpowa.dto.GroupInfo;
import mogu.server.mokpowa.dto.GroupMember;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.Group;
import mogu.server.mokpowa.entity.User;
import mogu.server.mokpowa.repository.GroupRepository;
import mogu.server.mokpowa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    public UserInfo insertGroup(Group group, UserInfo user) throws Exception {
        if (group == null) {
            throw new IllegalArgumentException("Group 객체가 null입니다.");
        }

        CollectionReference groupCollection = firestore.collection(COLLECTION_NAME);

        // 파이어베이스에 동일한 groupname을 가진 그룹이 있는지 확인
        ApiFuture<QuerySnapshot> future = groupCollection.whereEqualTo("groupName", group.getGroupName()).get();

        // 해당 그룹이 존재할 경우 예외처리
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if (!documents.isEmpty()) {
            throw new Exception("해당 그룹 이름이 이미 존재합니다.");
        }

        // 그룹 멤버 리스트에 새로운 멤버 정보를 추가
        group.getGroupMember().add(new GroupMember(user.getUserEmail(), user.getUserName(), null, null));

        // 그룹 정보 저장
        ApiFuture<WriteResult> resultApiFuture = firestore.collection(COLLECTION_NAME).document(group.getGroupKey()).set(group);

        // 해당 사용자 그룹키 정보 업데이트
        User updateUser = userRepository.getUserDetail(user.getUserEmail());
        updateUser.getGroupKeyList().add(group.getGroupKey());
        userRepository.updateUser(updateUser);

        user.getGroupList().add(group);
        
        // 해당 userInfo 반환
        return user;
    }

    @Override
    public Group getGroupDatail(String groupKey) throws Exception {
        DocumentReference documentReference =
                firestore.collection(COLLECTION_NAME).document(groupKey);

        ApiFuture<DocumentSnapshot> apiFuture = documentReference.get();
        DocumentSnapshot documentSnapshot = apiFuture.get();
        if(documentSnapshot.exists()){
            return documentSnapshot.toObject(Group.class);
        } else {
            throw new Exception("해당하는 그룹이 존재하지 않습니다.");
        }
    }

    @Override
    public boolean groupExists(String groupKey) {
        try {
            getGroupDatail(groupKey);
            return true; // 그룹이 존재하는 경우
        } catch (Exception e) {
            return false; // 그룹이 존재하지 않는 경우
        }
    }

    @Override
    public ArrayList<GroupInfo> getJoinGroup(User user) throws Exception {
        ArrayList<GroupInfo> joinGroup = new ArrayList<>();

        for(String groupKey : user.getGroupKeyList()) {
            Group group = getGroupDatail(groupKey);
            if(group != null) {
                joinGroup.add(group);
            }
        }

        return joinGroup;
    }

    @Override
    public String updateGroup(Group group) throws Exception {
        ApiFuture<WriteResult> future =
                firestore.collection(COLLECTION_NAME).document(group.getGroupKey()).set(group);
        return future.get().getUpdateTime().toString();
    }

    // 그룳 멤버 추가
    @Override
    public Group joinGroup(String groupKey, UserInfo user) throws Exception {
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
            ArrayList<GroupMember> groupMembers = group.getGroupMember();

            // 그룹 멤버 리스트를 순회하며 중복 멤버를 검사
            for (GroupMember member : groupMembers) {
                if (member.getMemberEmail().equals(user.getUserEmail())) {
                    throw new Exception("해당 그룹 멤버가 이미 존재합니다.");
                }
            }

            // 그룹 멤버 리스트에 새로운 멤버 정보를 추가
            group.getGroupMember().add(new GroupMember(user.getUserEmail(), user.getUserName(), null, null));

            // 그룹 정보를 업데이트
            updateGroup(group);

            // 업데이트된 그룹 정보를 반환
            return group;
        }

        // 문서가 존재하지 않는 경우 예외를 발생
        throw new Exception("해당 그룹이 존재하지 않습니다");
    }

    @Override
    public UserInfo deleteGroup(String deleteGroupName, UserInfo user) throws Exception {
        // 그룹이 존재하는지 확인
        ApiFuture<QuerySnapshot> querySnapshot = firestore.collection(COLLECTION_NAME).whereEqualTo("groupName", deleteGroupName).get();

        // 그룹이 존재하지 않으면 예외처리
        if (querySnapshot.get().getDocuments().isEmpty()) {
            throw new Exception("해당 그룹이 존재하지 않습니다.");
        }

        DocumentSnapshot document = querySnapshot.get().getDocuments().get(0);
        if (document.exists()) {
            String groupKey = (String) document.get("groupKey");

            // 해당 그룹 유저들 정보 업데이트
            List<User> userList = userRepository.getUsers();
            for (User findUser : userList) {
                Iterator<String> iterator = findUser.getGroupKeyList().iterator();
                while (iterator.hasNext()) {
                    String findGroupKey = iterator.next();
                    if (findGroupKey.equals(groupKey)) {
                        iterator.remove();
                        userRepository.updateUser(findUser);
                    }
                }
            }

            // 그룹 삭제
            document.getReference().delete();

            // userIn업데이트
            Iterator<GroupInfo> groupIterator = user.getGroupList().iterator();
            while (groupIterator.hasNext()) {
                GroupInfo groupInfo = groupIterator.next();
                if (groupInfo.getGroupKey().equals(groupKey)) {
                    groupIterator.remove();
                }
            }

            return user;
        }
        throw new Exception("해당 문서가 존재하지 않습니다.");
    }


    @Override
    public UserInfo deleteGroupMember(String deleteGroupName, String deleteGroupMemberEmail, UserInfo user) throws Exception {
        // 삭제하려는 멤버가 존재하는지 확인
        ApiFuture<QuerySnapshot> userQuerySnapshot = firestore.collection("users").whereEqualTo("userEmail", deleteGroupMemberEmail).get();
        if (userQuerySnapshot.get().getDocuments().isEmpty()) {
            throw new Exception("해당 유저는 존재하지 않습니다.");
        }

        // 그룹이 존재하는지 확인
        ApiFuture<QuerySnapshot> groupQuerySnapshot = firestore.collection(COLLECTION_NAME).whereEqualTo("groupName", deleteGroupName).get();
        if (groupQuerySnapshot.get().getDocuments().isEmpty()) {
            throw new Exception("해당 그룹은 존재하지 않습니다.");
        }

        DocumentSnapshot document = groupQuerySnapshot.get().getDocuments().getFirst();
        if (document.exists()) {
            String groupKey = document.getString("groupKey");

            // Firestore Transaction를 사용하여 원자적으로 각 문서 업데이트
            firestore.runTransaction(transaction -> {
                // 해당 그룹 유저 정보 업데이트
                List<User> userList = userRepository.getUsers();
                for (User findUser : userList) {
                    if (findUser.getUserEmail().equals(deleteGroupMemberEmail)) {
                        findUser.getGroupKeyList().removeIf(key -> key.equals(groupKey));
                        userRepository.updateUser(findUser); // 모든 사용자를 업데이트
                    }
                }

                // 그룹 멤버 삭제
                for (GroupInfo groupInfo : user.getGroupList()) {
                    if (groupInfo.getGroupName().equals(deleteGroupName)) {
                        groupInfo.getGroupMember().removeIf(member -> member.getMemberEmail().equals(deleteGroupMemberEmail));
                        Group group = new Group(groupInfo.getGroupName(), groupInfo.getGroupKey(), groupInfo.getGmEmail(), groupInfo.getGmName(), groupInfo.getGroupMember());
                        updateGroup(group);
                        break;
                    }
                }
                return null;
            }).get();

            return user;
        }
        throw new Exception("해당 문서가 존재하지 않습니다.");
    }
}