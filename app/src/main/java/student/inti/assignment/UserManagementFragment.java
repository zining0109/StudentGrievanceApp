package student.inti.assignment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserManagementFragment extends Fragment {

    private ListView userListView;
    private FirebaseFirestore db;
    private ArrayList<Map<String, String>> userList; // List to hold user data
    private Button btnAddUser;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);
        userListView = view.findViewById(R.id.userListView);
        btnAddUser = view.findViewById(R.id.btnAddUser);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userList = new ArrayList<>();
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

        // Fetch user data from Firestore
        fetchUsers();

        // Add button click listener
        btnAddUser.setOnClickListener(v -> showAddUserDialog());

        return view;
    }

    private void fetchUsers() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear(); // Clear old data
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Map<String, String> user = new HashMap<>();
                    user.put("email", document.getString("email"));
                    user.put("password", document.getString("password"));
                    user.put("id", document.getId()); // Store Firestore document ID
                    userList.add(user);
                }
                populateListView();
            } else {
                Log.w("TAG", "Error getting documents.", task.getException());
            }
        });
    }

    private void populateListView() {
        ArrayAdapter<Map<String, String>> adapter = new ArrayAdapter<Map<String, String>>(getContext(), R.layout.list_item_user, userList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_user, parent, false);
                }

                // Get the user data for the current position
                Map<String, String> user = getItem(position);

                // Set the username and password text
                TextView tvEmail = convertView.findViewById(R.id.tvEmail);
                TextView tvPassword = convertView.findViewById(R.id.tvPassword);
                tvEmail.setText(user.get("email"));
                tvPassword.setText(user.get("password"));

                Button btnDelete = convertView.findViewById(R.id.btnDelete);

                // Delete button listener with confirmation dialog
                btnDelete.setOnClickListener(v -> {
                    // Show confirmation dialog before deletion
                    new AlertDialog.Builder(getContext())
                            .setTitle("Delete User")
                            .setMessage("Are you sure you want to delete this user?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // User confirmed, delete the user
                                deleteUser(user.get("id"));
                            })
                            .setNegativeButton("No", null) // Dismiss the dialog if "No" is clicked
                            .show();
                });

                return convertView;
            }
        };

        userListView.setAdapter(adapter);
    }

    private void showAddUserDialog() {
        // Create a dialog to input new username and password
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New User");

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.add_user_dialog, null);
        builder.setView(dialogView);

        // Get references to the EditTexts
        EditText etEmail = dialogView.findViewById(R.id.etEmail);
        EditText etPassword = dialogView.findViewById(R.id.etPassword);
        EditText etUsername = dialogView.findViewById(R.id.Username);
        EditText etPhoneNo=dialogView.findViewById(R.id.etPassword);

        // Set the dialog buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();
            String username=etUsername.getText().toString();
            String phoneNo=etPhoneNo.getText().toString();

            // Check if email and password are not empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all field ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(), "Password at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            addUser(email, password, username, phoneNo);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.create().show();
    }

    private void addUser(String email, String password, String username, String phoneNo) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User creation successful, now save user details in Firestore
                        String userId = mAuth.getCurrentUser().getUid();

                        // Step 2: Create a user map with email and password
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("password", password); // Note: Storing passwords in plain text is insecure!
                        user.put("username", username);
                        user.put("phone", phoneNo);

                        // Step 3: Save the user information in Firestore
                        db.collection("users")
                                .document(userId) // Use the user's UID as the document ID
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("TAG", "User added to Firestore with ID: " + userId);
                                    fetchUsers(); // Refresh the list after successful addition
                                })
                                .addOnFailureListener(e -> Log.w("TAG", "Error adding user to Firestore", e));

                    } else {
                        // User creation failed, handle the error
                        Log.w("TAG", "Error creating user in Firebase Authentication", task.getException());
                        Toast.makeText(getContext(), "Error creating user: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteUser(String userId) {
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("TAG", "User document deleted from Firestore");

                    // Step 2: Get the user from Firebase Authentication and delete the user account
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null) {
                        user.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("TAG", "User account deleted from Firebase Authentication");
                                        Toast.makeText(getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                                        fetchUsers(); // Refresh the user list
                                    } else {
                                        Log.w("TAG", "Error deleting user from Firebase Authentication", task.getException());
                                        Toast.makeText(getContext(), "Error deleting user", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("TAG", "Error deleting user from Firestore", e);
                    Toast.makeText(getContext(), "Error deleting user from Firestore", Toast.LENGTH_SHORT).show();
                });
    }
}
