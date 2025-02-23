package mogu.server.mokpowa.repository.impl;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import mogu.server.mokpowa.dto.UserInfo;
import mogu.server.mokpowa.entity.User;
import mogu.server.mokpowa.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserRepositoryImpl implements UserRepository {
    public static final String COLLECTION_NAME = "users";
    Firestore firestore = FirestoreClient.getFirestore();

    // 사용자 추가
    @Override
    public String insertUser(UserInfo user) throws ExecutionException, InterruptedException {
        if (user == null) {
            throw new IllegalArgumentException("User 객체가 null입니다.");
        }

        User newuser = new User(user.getUserEmail(), user.getPassword(), user.getUserName(), user.getPhoneNumber());

        // 파이어베이스에 동일한 userEmail을 가진 사용자가 있는지 확인
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(newuser.getUserEmail());
        ApiFuture<DocumentSnapshot> futureSnapshot = docRef.get();
        DocumentSnapshot documentSnapshot = futureSnapshot.get();

        if (documentSnapshot.exists()) {
            // 동일한 userEmail을 가진 사용자가 이미 존재하면
            throw new IllegalArgumentException("동일한 userEmail을 가진 사용자가 이미 존재합니다.");
        }

        // 사용자 정보를 저장
        ApiFuture<WriteResult> future = docRef.set(newuser);

        // 정상적으로 해당 유저가 저장되면 사용자의 이메일 반환
        return newuser.getUserEmail();
    }

    // 사용자 정보 조회
    @Override
    public User getUserDetail(String email) throws Exception {
        DocumentReference documentReference =
                firestore.collection(COLLECTION_NAME).document(email);
        ApiFuture<DocumentSnapshot> apiFuture = documentReference.get();
        DocumentSnapshot documentSnapshot = apiFuture.get();
        if(documentSnapshot.exists()){
            return documentSnapshot.toObject(User.class);
        } else {
            throw new Exception("해당하는 유저가 존재하지 않습니다.");
        }
    }

    // 사용자 정보 수정
    @Override
    public String updateUser(User user) throws ExecutionException, InterruptedException {
        ApiFuture<WriteResult> future = firestore.collection(COLLECTION_NAME).document(user.getUserEmail()).set(user);
        return future.get().getUpdateTime().toString();
    }

    // 모든 사용자 조회
    @Override
    public  List<User> getUsers() throws Exception {
        List<User> list = new ArrayList<>();
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            list.add(document.toObject(User.class));
        }
        return list;
    }

    @Override
    public String deleteUser(String email) throws Exception {
        // 해당 유저가 존재하는지 확인
        ApiFuture<QuerySnapshot> querySnapshot = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("userEmail", email)
                .select("userName") // 필요한 필드만 조회
                .get();

        // 사용자가 존재하지 않으면 예외 처리
        if (querySnapshot.get().getDocuments().isEmpty()) {
            throw new Exception("해당 사용자가 존재하지 않습니다.");
        }

        DocumentSnapshot document = querySnapshot.get().getDocuments().getFirst(); // 첫 번째 문서
        if (document.exists()) {
            // 해당 사용자 정보를 삭제
            document.getReference().delete();
        }

        return "사용자 삭제 완료";
    }

    @Override
    public String findUserId(String username, String phoneNumber) throws Exception {
        List<User> list = getUsers();
        for (User user : list) {
            if (user.getUserName().equals(username) && user.getPhoneNumber().equals(phoneNumber)) {
                return user.getUserEmail();
            }
        }
        throw new Exception("해당하는 사용자가 존재하지 않습니다.");
    }
}