package student.inti.assignment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChangePasswordFragment extends Fragment {

    private FirebaseFirestore db;
    private Button changePasswordButton;
    private String adminId = "TCLLziFRszejsTEe55qK"; // Replace this with your admin document ID

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        // Set up the toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // Show the back arrow on the toolbar
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Handle back arrow click
        toolbar.setNavigationOnClickListener(v -> {
            // Use the fragment manager to go back to the previous fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        db = FirebaseFirestore.getInstance();

        EditText currentPassword = view.findViewById(R.id.currentPassword);
        EditText newPassword = view.findViewById(R.id.newPassword);

        changePasswordButton.setOnClickListener(v -> {
            String enteredCurrentPassword = currentPassword.getText().toString();
            String enteredNewPassword = newPassword.getText().toString();

            if (!enteredCurrentPassword.isEmpty() && !enteredNewPassword.isEmpty()) {
                changeAdminPassword(enteredCurrentPassword, enteredNewPassword);
            } else {
                Toast.makeText(getContext(), "Please enter all fields", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void changeAdminPassword(String currentPassword, String newPassword) {
        // Get the current Firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Get the user's email
            String email = user.getEmail();
            if (email != null) {
                // Re-authenticate the user with current password
                AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);

                user.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("ChangePasswordFragment", "User re-authenticated.");

                        // Now update the password in Firebase Authentication
                        user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                Log.d("ChangePasswordFragment", "Password updated in Firebase Authentication.");
                                // Update the password in Firestore
                                updateFirestorePassword(newPassword);
                            } else {
                                Log.e("ChangePasswordFragment", "Error updating password in Firebase Authentication: ", updateTask.getException());
                                Toast.makeText(getContext(), "Error updating password in Firebase Authentication", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.w("ChangePasswordFragment", "Re-authentication failed: ", task.getException());
                        Toast.makeText(getContext(), "Re-authentication failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "User email not found. Please log in again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFirestorePassword(String newPassword) {
        // Reference to the admin document
        DocumentReference adminRef = db.collection("admin").document(adminId);

        adminRef.update("password", newPassword)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                    Log.d("ChangePasswordFragment", "Password updated in Firestore.");
                })
                .addOnFailureListener(e -> {
                    Log.e("ChangePasswordFragment", "Error updating password in Firestore: ", e);
                    Toast.makeText(getContext(), "Error updating password", Toast.LENGTH_SHORT).show();
                });
    }
}
