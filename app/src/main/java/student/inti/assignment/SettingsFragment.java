package student.inti.assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {
    private ListView settingsListView;
    private Switch darkModeSwitch;
    private Switch alertSwitch; // Notification switch

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsListView = view.findViewById(R.id.settingsListView);

        String[] settingsOptions = {"User Management", "Change Password", "Notification Settings", "App Preference", "Log out"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.list_item_settings, settingsOptions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_settings, parent, false);
                }

                TextView settingName = convertView.findViewById(R.id.settingName);
                LinearLayout darkModeLayout = convertView.findViewById(R.id.darkModeLayout);
                LinearLayout alertLayout = convertView.findViewById(R.id.alertLayout); // Alert layout
                darkModeSwitch = convertView.findViewById(R.id.darkModeSwitch);
                alertSwitch = convertView.findViewById(R.id.alertSwitch); // Alert switch

                settingName.setText(getItem(position));

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

                // For App Preference - Dark Mode
                if (position == 3) {
                    darkModeLayout.setVisibility(View.VISIBLE);
                    darkModeSwitch.setChecked(preferences.getBoolean("dark_mode", false));
                    darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("dark_mode", isChecked);
                        editor.apply();
                        requireActivity().recreate();
                    });
                }
                // For Notification Settings
                else if (position == 2) {
                    alertLayout.setVisibility(View.VISIBLE);
                    alertSwitch.setChecked(preferences.getBoolean("alert_enabled", true));
                    alertSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("alert_enabled", isChecked);
                        editor.apply();
                    });
                } else {
                    darkModeLayout.setVisibility(View.GONE);
                    alertLayout.setVisibility(View.GONE);
                }

                return convertView;
            }
        };

        settingsListView.setAdapter(adapter);

        settingsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Fragment selectedFragment = null;

            switch (position) {
                case 0:
                    selectedFragment = new UserManagementFragment();
                    break;
                case 1:
                    selectedFragment = new ChangePasswordFragment();
                    break;
                case 2:
                    // Handle notification settings click if needed
                    break;
                case 3:
                    // Handle the App Preference click if needed
                    break;
                case 4:
                    logOut();
                    break;
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }
        });

        return view;
    }

    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.flFragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    // Method to log out the user
    private void logOut() {
        FirebaseAuth.getInstance().signOut();  // Log out from Firebase
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();  // Show logout message
        // Navigate back to the MainActivity (Login screen)
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clear activity stack
        startActivity(intent);
        getActivity().finish();  // End the current activity
    }
}
