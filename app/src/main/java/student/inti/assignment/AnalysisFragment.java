package student.inti.assignment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.eazegraph.lib.charts.BarChart;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.BarModel;
import org.eazegraph.lib.models.PieModel;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class AnalysisFragment extends Fragment {
    // Create the object of TextView and PieChart class
    PieChart pieChart;
    BarChart barChart;
    ValueLineChart lineChart;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);
        pieChart = view.findViewById(R.id.piechart);
        barChart=view.findViewById(R.id.barchart);
        lineChart=view.findViewById(R.id.linechart);

        // Load data from Firestore
        fetchPieChart();

        fetchBarChart();

        fetchLineChart();

        return view;
    }

    private void fetchPieChart() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("grievance").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int academicCount=0, fiCount=0;
                int adCount=0, hCount=0;
                int fCount=0, itcount=0;
                int foodCount=0, othersCount=0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String category = document.getString("category");
                    if (category != null) {
                        switch (category) {
                            case "Academic":
                                academicCount++;
                                break;
                            case "Administrative":
                                adCount++;
                                break;
                            case "Facilities":
                                fCount++;
                                break;
                            case "Food":
                                foodCount++;
                                break;
                            case "Financial":
                                fiCount++;
                                break;
                            case "Harassment":
                                hCount++;
                                break;
                            case "IT":
                                itcount++;
                                break;
                            case "Others":
                                othersCount++;
                                break;
                        }
                    }
                }
                // Set the data and colors to the pie chart
                pieChart.clearChart(); // Clear the chart before adding new data
                pieChart.addPieSlice(new PieModel("Academic", academicCount, Color.parseColor("#FFA726")));
                pieChart.addPieSlice(new PieModel("Administrative", adCount, Color.parseColor("#66BB6A")));
                pieChart.addPieSlice(new PieModel("Facilities", fCount, Color.parseColor("#EF5350")));
                pieChart.addPieSlice(new PieModel("Food", foodCount, Color.parseColor("#29B6F6")));
                pieChart.addPieSlice(new PieModel("Financial", fiCount, Color.parseColor("#008b8b")));
                pieChart.addPieSlice(new PieModel("Harassment", hCount, Color.parseColor("#808000")));
                pieChart.addPieSlice(new PieModel("IT", itcount, Color.parseColor("#ee82ee")));
                pieChart.addPieSlice(new PieModel("Others", othersCount, Color.parseColor("#b22222")));

                // To animate the pie chart
                pieChart.startAnimation();
            }else {
                Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
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

    private void fetchLineChart() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("grievance").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int janCount = 0, febCount = 0, marchCount = 0, aprilCount = 0, mayCount = 0, juneCount = 0;
                int julyCount = 0, augCount = 0, septCount = 0, octCount = 0, novCount = 0, decCount = 0;
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Timestamp dateTime = document.getTimestamp("dateTime");
                    if (dateTime != null) {
                        Date date = dateTime.toDate();
                        SimpleDateFormat format = new SimpleDateFormat("MMM", Locale.getDefault());
                        String month = format.format(date);
                        switch (month) {
                            case "Jan":
                                janCount++;
                                break;
                            case "Feb":
                                febCount++;
                                break;
                            case "Mar":
                                marchCount++;
                                break;
                            case "Apr":
                                aprilCount++;
                                break;
                            case "May":
                                mayCount++;
                                break;
                            case "Jun":
                                juneCount++;
                                break;
                            case "Jul":
                                julyCount++;
                                break;
                            case "Aug":
                                augCount++;
                                break;
                            case "Sep":
                                septCount++;
                                break;
                            case "Oct":
                                octCount++;
                                break;
                            case "Nov":
                                novCount++;
                                break;
                            case "Dec":
                                decCount++;
                                break;
                        }
                    }
                }
            ValueLineSeries series = new ValueLineSeries();
            series.setColor(0xFF964b00);

            series.addPoint(new ValueLinePoint("Jan", janCount));
            series.addPoint(new ValueLinePoint("Feb", febCount));
            series.addPoint(new ValueLinePoint("Mar", marchCount));
            series.addPoint(new ValueLinePoint("Apr", aprilCount));
            series.addPoint(new ValueLinePoint("May", mayCount));
            series.addPoint(new ValueLinePoint("Jun", juneCount));
            series.addPoint(new ValueLinePoint("Jul", julyCount));
            series.addPoint(new ValueLinePoint("Aug", augCount));
            series.addPoint(new ValueLinePoint("Sep", septCount));
            series.addPoint(new ValueLinePoint("Oct", octCount));
            series.addPoint(new ValueLinePoint("Nov", novCount));
            series.addPoint(new ValueLinePoint("Dec", decCount));

            lineChart.addSeries(series);
            lineChart.startAnimation();
            } else {
                Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

