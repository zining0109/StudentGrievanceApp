package student.inti.assignment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.Context;
import com.google.firebase.Timestamp;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

public class GrievanceFragment extends Fragment {

    private ListView grievanceListView;
    private FirebaseFirestore db;
    private ArrayList<Map<String, String>> grievanceList;
    private LinearLayout menuDropdown;
    private LinearLayout categoryDropdown;
    private LinearLayout statusDropdown;
    private LinearLayout urgencyDropdown;
    private boolean isMenuDropdownVisible = false;
    private boolean isCategoryDropdownVisible = false;
    private boolean isStatusDropdownVisible = false;
    private boolean isUrgencyDropdownVisible=false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grievance, container, false);

        db = FirebaseFirestore.getInstance();
        grievanceListView = view.findViewById(R.id.grievanceListView);
        grievanceList = new ArrayList<>();

        // Clear the red dot on grievance view
        ((HomeActivity) getActivity()).clearRedDot();

        // Menu button to open the dropdown menu
        Button btnMenu = view.findViewById(R.id.btn_menu);
        menuDropdown = view.findViewById(R.id.menu_dropdown);
        categoryDropdown = view.findViewById(R.id.category_dropdown);
        statusDropdown=view.findViewById(R.id.status_dropdown);
        urgencyDropdown=view.findViewById(R.id.urgency_dropdown);

        // Toggle visibility of the menu dropdown on button click
        btnMenu.setOnClickListener(v -> {
            if (isMenuDropdownVisible) {
                menuDropdown.setVisibility(View.GONE);  // Hide the dropdown
            } else {
                menuDropdown.setVisibility(View.VISIBLE);  // Show the dropdown
            }
            isMenuDropdownVisible = !isMenuDropdownVisible;  // Toggle the state
        });

        // Toggle visibility of the category dropdown when Categories button is clicked
        Button btnCategories = view.findViewById(R.id.btn_categories);
        btnCategories.setOnClickListener(v -> {
            if (isCategoryDropdownVisible) {
                categoryDropdown.setVisibility(View.GONE);  // Hide the category options
            } else {
                categoryDropdown.setVisibility(View.VISIBLE);  // Show the category options
            }
            isCategoryDropdownVisible = !isCategoryDropdownVisible;  // Toggle the state
        });

        // Toggle visibility of the status dropdown when Categories button is clicked
        Button btnStatus = view.findViewById(R.id.btn_status);
        btnStatus.setOnClickListener(v -> {
            if (isStatusDropdownVisible) {
                statusDropdown.setVisibility(View.GONE);  // Hide the category options
            } else {
                statusDropdown.setVisibility(View.VISIBLE);  // Show the category options
            }
            isStatusDropdownVisible = !isStatusDropdownVisible;  // Toggle the state
        });

        // Toggle visibility of the urgency dropdown when Categories button is clicked
        Button btnUrgency = view.findViewById(R.id.btn_urgency);
        btnUrgency.setOnClickListener(v -> {
            if (isUrgencyDropdownVisible) {
                urgencyDropdown.setVisibility(View.GONE);  // Hide the category options
            } else {
                urgencyDropdown.setVisibility(View.VISIBLE);  // Show the category options
            }
            isUrgencyDropdownVisible = !isUrgencyDropdownVisible;  // Toggle the state
        });

        // Handle individual category button clicks
        Button btnAcademic = view.findViewById(R.id.btn_academic);
        btnAcademic.setOnClickListener(v -> fetchGrievancesByCategory("Academic"));

        Button btnAdministrative = view.findViewById(R.id.btn_administrative);
        btnAdministrative.setOnClickListener(v -> fetchGrievancesByCategory("Administrative"));

        Button btnFacilities = view.findViewById(R.id.btn_facilities);
        btnFacilities.setOnClickListener(v -> fetchGrievancesByCategory("Facilities"));

        Button btnFood = view.findViewById(R.id.btn_food);
        btnFood.setOnClickListener(v -> fetchGrievancesByCategory("Food"));

        Button btnFinancial = view.findViewById(R.id.btn_financial);
        btnFinancial.setOnClickListener(v -> fetchGrievancesByCategory("Financial"));

        Button btnHarassment = view.findViewById(R.id.btn_harassment);
        btnHarassment.setOnClickListener(v -> fetchGrievancesByCategory("Harassment"));

        Button btnIT = view.findViewById(R.id.btn_it);
        btnIT.setOnClickListener(v -> fetchGrievancesByCategory("IT"));

        Button btnOthers = view.findViewById(R.id.btn_others);
        btnOthers.setOnClickListener(v -> fetchGrievancesByCategory("Others"));

        Button btnPending = view.findViewById(R.id.btn_pending);
        btnPending.setOnClickListener(v -> fetchGrievancesByStatus("status", "Pending"));

        Button btnInprogress = view.findViewById(R.id.btn_inprogress);
        btnInprogress.setOnClickListener(v -> fetchGrievancesByStatus("status", "In Progress"));

        Button btnResolved = view.findViewById(R.id.btn_resolved);
        btnResolved.setOnClickListener(v -> fetchGrievancesByStatus("status", "Resolved"));

        Button btnClosed = view.findViewById(R.id.btn_closed);
        btnClosed.setOnClickListener(v -> fetchGrievancesByStatus("status", "Closed"));

        Button btnHigh = view.findViewById(R.id.btn_h);
        btnHigh.setOnClickListener(v -> fetchGrievancesByUrgency("High"));

        Button btnMedium = view.findViewById(R.id.btn_m);
        btnMedium.setOnClickListener(v -> fetchGrievancesByUrgency("Medium"));

        Button btnLow = view.findViewById(R.id.btn_l);
        btnLow.setOnClickListener(v -> fetchGrievancesByUrgency("Low"));

        // Fetch grievance data from Firestore
        fetchGrievance();

        return view;
    }

    private void fetchGrievance() {
        db.collection("grievance")
                .orderBy("dateTime", Query.Direction.DESCENDING) // Ensure dateTime is a Firestore Timestamp
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        grievanceList.clear(); // Clear old data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, String> grievance = new HashMap<>();
                            // Get the timestamp and put it in the grievance map
                            Timestamp timestamp = document.getTimestamp("dateTime");
                            if (timestamp != null) {
                                grievance.put("dateTime", String.valueOf(timestamp.toDate())); // Convert Timestamp to Date
                            }
                            grievance.put("email", document.getString("email"));
                            grievance.put("category", document.getString("category"));
                            grievance.put("status", document.getString("status"));
                            grievance.put("urgencyLevel", document.getString("urgencyLevel"));
                            grievance.put("description", document.getString("description"));
                            grievance.put("id", document.getId()); // Store Firestore document ID
                            grievanceList.add(grievance);
                        }
                        populateListView(); // Populate the list with fetched data
                    } else {
                        Log.w("TAG", "Error getting documents.", task.getException()); // Log any error
                    }
                });
    }

    private void populateListView() {
        ArrayAdapter<Map<String, String>> adapter = new ArrayAdapter<Map<String, String>>(getContext(), R.layout.list_item_grievance, grievanceList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_grievance, parent, false);
                }

                // Get the user data for the current position
                Map<String, String> grievance = getItem(position);

                // Set the username and password text
                TextView tvDateTime = convertView.findViewById(R.id.tvDateTime);
                TextView tvEmail = convertView.findViewById(R.id.tvEmail);
                TextView tvCategory=convertView.findViewById(R.id.tvCategory);
                TextView tvStatus=convertView.findViewById(R.id.tvStatus);
                TextView tvUrgencyLevel=convertView.findViewById(R.id.tvUrgencyLevel);
                TextView tvDescription = convertView.findViewById(R.id.tvDescription);
                tvDateTime.setText(grievance.get("dateTime"));
                tvEmail.setText(grievance.get("email"));
                tvCategory.setText(grievance.get("category"));
                tvStatus.setText(grievance.get("status"));
                tvUrgencyLevel.setText(grievance.get("urgencyLevel"));
                tvDescription.setText(grievance.get("description"));

                //update button
                Button btnUpdateCategory = convertView.findViewById(R.id.btnUpdateCategory);
                Button btnUpdateStatus = convertView.findViewById(R.id.btnUpdateStatus);
                Button btnUpdateUrgency = convertView.findViewById(R.id.btnUpdateUrgency);

                // Update button listener
                btnUpdateCategory.setOnClickListener(v -> {
                    // Inflate the dialog layout
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    View dialogView = inflater.inflate(R.layout.update_category_dialog, null);
                    builder.setView(dialogView);

                    // Access Spinner from the dialog
                    Spinner spinnerCategories = dialogView.findViewById(R.id.spinnerCategories);

                    // Set the dialog buttons
                    builder.setPositiveButton("Update", (dialog, which) -> {
                        String selectedCategory = spinnerCategories.getSelectedItem().toString();

                        // Ensure the admin selects a category
                        if (selectedCategory.isEmpty()) {
                            // Notify the admin to select a valid category
                            Toast.makeText(getContext(), "Please select a category.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Call method to update grievance data in Firestore
                            updateCategory(grievance.get("id"), selectedCategory);

                            // Dismiss the dialog after the update
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        // Dismiss the dialog if "Cancel" is clicked
                        dialog.dismiss();
                    });
                    // Create and show the dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });

                btnUpdateStatus.setOnClickListener(v -> {
                    // Inflate the dialog layout
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    View dialogView = inflater.inflate(R.layout.update_status_dialog, null);
                    builder.setView(dialogView);

                    // Access Spinner from the dialog
                    Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerStatus);

                    // Set the dialog buttons
                    builder.setPositiveButton("Update", (dialog, which) -> {
                        String selectedStatus = spinnerStatus.getSelectedItem().toString();

                        // Ensure the admin selects a category
                        if (selectedStatus.isEmpty()) {
                            // Notify the admin to select a valid category
                            Toast.makeText(getContext(), "Please select a status.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Call method to update grievance data in Firestore
                            updateStatus(grievance.get("id"), selectedStatus);

                            // Dismiss the dialog after the update
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        // Dismiss the dialog if "Cancel" is clicked
                        dialog.dismiss();
                    });
                    // Create and show the dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });

                btnUpdateUrgency.setOnClickListener(v -> {
                    // Inflate the dialog layout
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    View dialogView = inflater.inflate(R.layout.update_urgency_dialog, null);
                    builder.setView(dialogView);

                    // Access Spinner from the dialog
                    Spinner spinnerUrgency = dialogView.findViewById(R.id.spinnerUrgency);

                    // Set the dialog buttons
                    builder.setPositiveButton("Update", (dialog, which) -> {
                        String selectedUrgency = spinnerUrgency.getSelectedItem().toString();

                        // Ensure the admin selects a category
                        if (selectedUrgency.isEmpty()) {
                            // Notify the admin to select a valid category
                            Toast.makeText(getContext(), "Please select a urgency level.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Call method to update grievance data in Firestore
                            updateUrgency(grievance.get("id"), selectedUrgency);

                            // Dismiss the dialog after the update
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        // Dismiss the dialog if "Cancel" is clicked
                        dialog.dismiss();
                    });
                    // Create and show the dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });
                return convertView;
            }
        };
        grievanceListView.setAdapter(adapter);
    }

    private void updateCategory(String grievanceId, String selectedCategory) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference grievanceRef = db.collection("grievance").document(grievanceId);

        // Update the category field
        grievanceRef.update("category", selectedCategory)
                .addOnSuccessListener(aVoid -> {
                    // Update was successful
                    Toast.makeText(getContext(), "Category updated", Toast.LENGTH_SHORT).show();
                    fetchGrievance();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(getContext(), "Failed to update category", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateStatus(String grievanceId, String selectedStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference grievanceRef = db.collection("grievance").document(grievanceId);

        // Update the grievance document
        grievanceRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                // Retrieve the grievance document
                DocumentSnapshot document = task.getResult();
                String grievanceTitle = document.getString("title"); // Adjust if "description" is not the title field
                String grievanceDescription = document.getString("description"); // Fetch the description
                String grievanceCategory = document.getString("category"); // Fetch the category
                String email = document.getString("email"); // Fetch the email

                // Create a map to hold the update data
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", selectedStatus); // Update the status
                updates.put("updateTime", FieldValue.serverTimestamp()); // Set the update time

                // Update the grievance status
                grievanceRef.update(updates)
                        .addOnSuccessListener(aVoid -> {
                            // Status update successful, now add a notification
                            addNotification(grievanceId, selectedStatus, grievanceTitle, grievanceDescription, grievanceCategory, email);
                            Toast.makeText(getContext(), "Status updated", Toast.LENGTH_SHORT).show();
                            fetchGrievance(); // Refresh the grievance list
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure
                            Toast.makeText(getContext(), "Failed to update Status", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Handle failure to retrieve grievance document
                Toast.makeText(getContext(), "Failed to retrieve grievance details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to add a notification after updating the status
    private void addNotification(String grievanceId, String selectedStatus, String grievanceTitle,  String grievanceDescription, String grievanceCategory, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> notificationData = new HashMap<>();

        // Prepare notification data
        notificationData.put("grievance_submission_id", grievanceId);
        notificationData.put("grievance_submission_status", selectedStatus);
        notificationData.put("grievance_submission_title", grievanceTitle); // Use the retrieved title
        notificationData.put("name", "Updated Status");
        notificationData.put("updated_time", FieldValue.serverTimestamp()); // Use server timestamp for consistency
        notificationData.put("email", email);
        notificationData.put("grievance_submission_description", grievanceDescription);
        notificationData.put("grievance_submission_category", grievanceCategory);

        // Add the notification to the "notification" collection
        db.collection("notification").add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    // Notification added successfully
                    Log.d("Notification", "Notification added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Log.w("Notification", "Error adding notification", e);
                });
    }

    private void updateUrgency(String grievanceId, String selectedUrgency) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference grievanceRef = db.collection("grievance").document(grievanceId);

        // Update the category field
        grievanceRef.update("urgencyLevel", selectedUrgency)
                .addOnSuccessListener(aVoid -> {
                    // Update was successful
                    Toast.makeText(getContext(), "Urgency updated", Toast.LENGTH_SHORT).show();
                    fetchGrievance();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(getContext(), "Failed to update urgency", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchGrievancesByCategory(String category) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("grievance")
                .whereEqualTo("category", category)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        grievanceList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, String> grievance = new HashMap<>();
                            // Get the timestamp and put it in the grievance map
                            Timestamp timestamp = document.getTimestamp("dateTime");
                            if (timestamp != null) {
                                grievance.put("dateTime", String.valueOf(timestamp.toDate())); // Convert Timestamp to Date
                            }
                            grievance.put("email", document.getString("email"));
                            grievance.put("category", document.getString("category"));
                            grievance.put("status", document.getString("status"));
                            grievance.put("urgencyLevel", document.getString("urgencyLevel"));
                            grievance.put("description", document.getString("description"));
                            grievance.put("id", document.getId()); // Store Firestore document ID
                            grievanceList.add(grievance);
                        }
                        populateListView();
                    } else {
                        Toast.makeText(getContext(), "Failed to retrieve grievances", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchGrievancesByStatus(String field, String value) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("grievance")
                .whereEqualTo(field, value)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        grievanceList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, String> grievance = new HashMap<>();
                            // Get the timestamp and put it in the grievance map
                            Timestamp timestamp = document.getTimestamp("dateTime");
                            if (timestamp != null) {
                                grievance.put("dateTime", String.valueOf(timestamp.toDate())); // Convert Timestamp to Date
                            }
                            grievance.put("email", document.getString("email"));
                            grievance.put("category", document.getString("category"));
                            grievance.put("status", document.getString("status"));
                            grievance.put("urgencyLevel", document.getString("urgencyLevel"));
                            grievance.put("description", document.getString("description"));
                            grievance.put("id", document.getId()); // Store Firestore document ID
                            grievanceList.add(grievance);
                        }
                        populateListView();
                    } else {
                        Toast.makeText(getContext(), "Failed to retrieve grievances", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchGrievancesByUrgency(String urgency) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("grievance")
                .whereEqualTo("urgencyLevel", urgency)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        grievanceList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, String> grievance = new HashMap<>();
                            // Get the timestamp and put it in the grievance map
                            Timestamp timestamp = document.getTimestamp("dateTime");
                            if (timestamp != null) {
                                grievance.put("dateTime", String.valueOf(timestamp.toDate())); // Convert Timestamp to Date
                            }
                            grievance.put("email", document.getString("email"));
                            grievance.put("category", document.getString("category"));
                            grievance.put("status", document.getString("status"));
                            grievance.put("urgencyLevel", document.getString("urgencyLevel"));
                            grievance.put("description", document.getString("description"));
                            grievance.put("id", document.getId()); // Store Firestore document ID
                            grievanceList.add(grievance);
                        }
                        populateListView();
                    } else {
                        Toast.makeText(getContext(), "Failed to retrieve grievances", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
