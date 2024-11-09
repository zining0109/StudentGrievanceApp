package student.inti.assignment;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class HomeActivity extends AppCompatActivity {

    private ImageView redDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnDashboard = findViewById(R.id.btnDashboard);
        Button btnGrievance = findViewById(R.id.btnGrievance);
        Button btnAnalysis = findViewById(R.id.btnAnalysis);
        Button btnSettings = findViewById(R.id.btnSettings);
        redDot = findViewById(R.id.redDot);

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(new DashboardFragment());
        }

        // Set button click listeners
        btnDashboard.setOnClickListener(view -> replaceFragment(new DashboardFragment()));
        btnGrievance.setOnClickListener(view -> {
            replaceFragment(new GrievanceFragment());
            redDot.setVisibility(View.GONE); // Hide red dot when grievances are viewed
        });
        btnAnalysis.setOnClickListener(view -> replaceFragment(new AnalysisFragment()));
        btnSettings.setOnClickListener(view -> replaceFragment(new SettingsFragment()));

        listenForNewGrievances();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flFragment, fragment);
        fragmentTransaction.commit();
    }

    private void receiveNewGrievance() {
        // Show red dot when a new grievance is received
        redDot.setVisibility(View.VISIBLE);
    }

    public void clearRedDot() {
        redDot.setVisibility(View.GONE);
    }

    private void listenForNewGrievances() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference grievancesRef = db.collection("grievance");

        grievancesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e("HomeActivity", "Firestore listener error: " + e.getMessage());
                    return;
                }

                if (snapshots != null && !snapshots.isEmpty()) {
                    Log.d("HomeActivity", "Grievances updated, checking for changes.");
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Log.d("HomeActivity", "New grievance detected.");
                            // Show the red dot when a new grievance is added
                            receiveNewGrievance();
                        }
                    }
                } else {
                    Log.d("HomeActivity", "No grievances found or snapshot is empty.");
                }
            }
        });
    }

}
