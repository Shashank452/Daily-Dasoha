public class User {
    private String uid;
    private String email;
    private String name;
    private String schoolName;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String uid, String email, String name, String schoolName) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.schoolName = schoolName;
    }

    // Getters and setters
} 