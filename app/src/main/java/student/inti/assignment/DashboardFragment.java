package student.inti.assignment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.models.BarModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DashboardFragment extends Fragment {
    BarChart barChart;
    private ListView grievanceListView;
    private FirebaseFirestore db;
    private ArrayList<Map<String, String>> grievanceList;
    private Button viewAllButton;
    private static final String CHANNEL_ID = "GrievanceChannel";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize the button
        viewAllButton = view.findViewById(R.id.viewAll);

        // Set up the click listener for the "View All" button
        viewAllButton.setOnClickListener(v -> {
            // Navigate to GrievanceFragment
            Fragment grievanceFragment = new GrievanceFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.flFragment, grievanceFragment); // R.id.fragment_container is the FrameLayout in your main activity
            transaction.addToBackStack(null); // Add the transaction to the back stack
            transaction.commit();
        });

        db = FirebaseFirestore.getInstance();
        grievanceListView = view.findViewById(R.id.dashboardGrievanceListView);
        grievanceList = new ArrayList<>();
        barChart=view.findViewById(R.id.barchart);

        grievanceList();

        fetchBarChart();

        return view;
    }

    private void grievanceList() {
        db.collection("grievance")
                .orderBy("dateTime", Query.Direction.DESCENDING) // Ensure dateTime is a Firestore Timestamp
                .limit(5) // Limit the results to the latest 5 grievances
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
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_dashboard, parent, false);
                }

                // Get the grievance data for the current position
                Map<String, String> grievance = getItem(position);

                // Set the details of the grievance
                TextView tvDateTime = convertView.findViewById(R.id.tvDateTime);
                TextView tvEmail = convertView.findViewById(R.id.tvEmail);
                TextView tvCategory = convertView.findViewById(R.id.tvCategory);
                TextView tvStatus = convertView.findViewById(R.id.tvStatus);
                TextView tvUrgencyLevel = convertView.findViewById(R.id.tvUrgencyLevel);
                TextView tvDescription = convertView.findViewById(R.id.tvDescription);
                tvDateTime.setText(grievance.get("dateTime"));
                tvEmail.setText(grievance.get("email"));
                tvCategory.setText(grievance.get("category"));
                tvStatus.setText(grievance.get("status"));
                tvUrgencyLevel.setText(grievance.get("urgencyLevel"));
                tvDescription.setText(grievance.get("description"));

                return convertView;
            }
        };

        grievanceListView.setAdapter(adapter); // Set the adapter to the ListView
    }

    private void fetchBarChart(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("grievance").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int pendingCount=0;
                int inprogressCount=0;
                int resolvedCount=0;
                int closedCount=0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String status = document.getString("status");
                    if (status != null) {
                        switch (status) {
                            case "Pending":
                                pendingCount++;
                                break;
                            case "In Progress":
                                inprogressCount++;
                                break;
                            case "Resolved":
                                resolvedCount++;
                                break;
                            case "Closed":
                                closedCount++;
                                break;
                        }
                    }
                }
                // Set the data and colors to the pie chart
                barChart.clearChart(); // Clear the chart before adding new data
                barChart.addBar(new BarModel("Pending", pendingCount, 0xFFd8bfd8 ));
                barChart.addBar(new BarModel("In Progress", inprogressCount,  0xFFd8bfd8 ));
                barChart.addBar(new BarModel("Resolved", resolvedCount, 0xFFd8bfd8 ));
                barChart.addBar(new BarModel("Closed", closedCount, 0xFFd8bfd8 ));

                // To animate the pie chart
                barChart.startAnimation();
            }else {
                Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

}