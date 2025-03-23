import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.dailydasoha.models.User;

public class AuthViewModel extends ViewModel {
    private final FirebaseAuth auth;
    private final MutableLiveData<User> currentUser = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AuthViewModel() {
        auth = FirebaseAuth.getInstance();
    }

    public void login(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                // Handle successful login
                FirebaseUser user = authResult.getUser();
                currentUser.setValue(new User(user.getUid(), user.getEmail(), "", ""));
            })
            .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    public void register(String email, String password, String name, String schoolName) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                // Handle successful registration
                FirebaseUser firebaseUser = authResult.getUser();
                User user = new User(firebaseUser.getUid(), email, name, schoolName);
                currentUser.setValue(user);
                // Save additional user info to Firebase/local database
            })
            .addOnFailureListener(e -> error.setValue(e.getMessage()));
    }

    // Getters for LiveData
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getError() {
        return error;
    }
} 