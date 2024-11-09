package student.inti.assignment;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    Button loginButton;
    Button forgotPasswordButton;
    FirebaseFirestore db;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        loginButton.setOnClickListener(v -> {
            String enteredEmail = email.getText().toString().trim();
            String enteredPassword = password.getText().toString();

            if (!enteredEmail.isEmpty() && !enteredPassword.isEmpty()) {
                checkCredentials(enteredEmail, enteredPassword);
            } else {
                Toast.makeText(MainActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPasswordButton.setOnClickListener(v -> {
            // Intent to navigate to ForgotPasswordActivity
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void checkCredentials(String enteredEmail, String enteredPassword) {
        // Authenticate the user with Firebase Authentication
        mAuth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User is authenticated
                        syncPasswordToFirestore(enteredEmail, enteredPassword);
                    } else {
                        // Authentication failed
                        Toast.makeText(MainActivity.this, "Email or Password incorrect", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void syncPasswordToFirestore(String enteredEmail, String enteredPassword) {
        // Assuming 'admin' is the Firestore collection
        db.collection("admin")
                .whereEqualTo("email", enteredEmail)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String uid = document.getId(); // Get the document ID

                            // Update the Firestore document with the new password
                            db.collection("admin").document(uid)
                                    .update("password", enteredPassword)
                                    .addOnSuccessListener(aVoid -> {
                                        // Password successfully updated in Firestore
                                        Toast.makeText(MainActivity.this, "Login successfully", Toast.LENGTH_SHORT).show();
                                        // Redirect to HomeActivity
                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure
                                        Log.e("FirestoreSync", "Error updating password in Firestore", e);
                                        Toast.makeText(MainActivity.this, "Error updating password", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(MainActivity.this, "Email not found in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("MainActivity", "Error getting documents: ", task.getException());
                        Toast.makeText(MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
