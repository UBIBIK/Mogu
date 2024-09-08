package mogu.server.mokpowa.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    @PostConstruct
    public void init() {
        try {
            // 클래스패스에서 serviceAccountKey.json 파일을 로드
            InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");

            if (serviceAccount == null) {
                throw new IllegalArgumentException("serviceAccountKey.json 파일을 찾을 수 없습니다.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
