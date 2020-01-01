package com.kevinlu.airquality;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * This Fragment is part of the MainActivity class
 * It is 1 of the 2 tabs available for use.
 * This Fragment displays air quality information based
 * on the user's IP address location.
 */
public class CurrentAirQualityFragment extends Fragment {

    //private final String url = "http://api.airvisual.com/v2/nearest_city?key=5X5FwBMHiD2DDKWBf";
    //private final String url = "http://api.airvisual.com/v2/nearest_city?key=5zbAzdPBu2RftKbus";
    private final String url = "http://api.airvisual.com/v2/nearest_city?key=e664662e-28cc-4b22-b2ab-9a20e1f67354";

    ImageView airQualityPicture;
    TextView cityName, countryName, cityTimeStamp, cityAQI, cityAQIRating;
    LinearLayout currentPanel;
    ProgressBar currentProgressBar;
    SwipeRefreshLayout pullToRefresh;
    RelativeLayout currentLayout;

    String stationJson = "";

    static CurrentAirQualityFragment instance;

    /**
     * This is just to make sure the CurrentAirQualityFragment is not null
     * when it is created.
     *
     * @return - the instance of the CurrentAirQualityFragment
     */
    public static CurrentAirQualityFragment getInstance() {
        if (instance == null) {
            instance = new CurrentAirQualityFragment();
        }
        return instance;
    }

    public CurrentAirQualityFragment() {
        // Empty constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("json", stationJson);
    }

    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState - a Bundle, if the activity is being
     *                           re-initialized after previously being
     *                           shut down then this Bundle contains
     *                           the data it most recently supplied in
     *                           onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //Check if user is connected to the internet
        //If the user is connected, don't show it
        if (!isNetworkAvailable()) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }
            builder.setTitle("No internet connection")
                    .setMessage("You must have an internet connection to receive the latest air quality information.")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.cancel())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    /**
     * Creates and returns the view hierarchy associated with the fragment
     *
     * @param inflater           - The LayoutInflater object that can be used to
     *                           inflate any views in the fragment
     * @param container          - a ViewGroup, if non-null, this is the parent view
     *                           that the fragment's UI should be attached to. The
     *                           fragment should not add the view itself, but this
     *                           can be used to generate the LayoutParams of the view.
     * @param savedInstanceState - a Bundle, if non-null, this fragment
     *                           is being re-constructed from a previous
     *                           saved state as given here.
     * @return - Return the View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_current_air_quality, container, false);

        currentLayout = itemView.findViewById(R.id.current_bg);

        cityName = itemView.findViewById(R.id.cityName);
        cityTimeStamp = itemView.findViewById(R.id.cityTimestamp);
        cityAQI = itemView.findViewById(R.id.cityAQI);
        cityAQIRating = itemView.findViewById(R.id.cityAQIRating);

        currentPanel = itemView.findViewById(R.id.current_panel);
        currentProgressBar = itemView.findViewById(R.id.current_progress_bar);
        pullToRefresh = itemView.findViewById(R.id.current_swipeRefresh);

        pullToRefresh.setOnRefreshListener(() -> {
            Toast.makeText(getContext(), "Refreshed", Toast.LENGTH_SHORT).show();
            getCurrentAirQualityData();
            pullToRefresh.setRefreshing(false);
        });

        //If the Fragment is being created for the first time
        //savedInstanceState will be null
        if (savedInstanceState == null && stationJson.equals("")) {
            getCurrentAirQualityData();
            Log.d("saved", "first time");
        } else {
            //Using Gson to turn JSON to Java object of Station
            //Create new GsonBuilder and Gson objects
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            //Create a new Station object and use Gson to deserialize JSON data
            //into the Station object
            Station station = gson.fromJson(stationJson, Station.class);
            //Call the loadCurrentData method to display the data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                loadCurrentDataTargetApi26(station);
            } else {
                loadCurrentDataTarget(station);
            }
        }

        return itemView;
    }

    /**
     * This method checks if the user is connected to the internet.
     *
     * @return - true, if the user is connected.
     * false, if the user is not connected.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * This function sends a request to the API to get the current data.
     */
    private void getCurrentAirQualityData() {
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        //Request a string response from the provided URL, create a new StringRequest object
        /*
         * @param response - This is the response (JSON file) from the API
         */
        //This is what will happen when there is an error during the response
        /*
         * @param error - This is the error that Volley encountered
         */
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    //Using Gson to turn JSON to Java object of Station
                    //Create new GsonBuilder and Gson objects
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    //Create a new Station object and use Gson to deserialize JSON data
                    //into the Station object
                    Station station = gson.fromJson(response, Station.class);
                    stationJson = response;
                    //Call the loadCurrentData method to display the data
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        loadCurrentDataTargetApi26(station);
                    } else {
                        loadCurrentDataTarget(station);
                    }
                    //Write the API response data to the log console
                    Log.d("API RESPONSE", response);
                }, error -> {
            //Write the error from Volley to the log console
            //TODO: add a try - catch block in case too many API requests
            Log.d("VOLLEY ERROR", error.toString());
        });
        //Add the request to the RequestQueue
        requestQueue.add(stringRequest);
    }

    /**
     * This function loads in the current air quality information based on the
     * user's IP address location. Only if API level is 26 or higher.
     *
     * @param station - a Station object that contains information from the API
     */
    @TargetApi(26)
    private void loadCurrentDataTargetApi26(Station station) {
        cityName.setText(station.getData().getCity());
        cityTimeStamp.setText(decodeTimestamp(station.getData().getCurrent().getPollution().getTs()));
        cityAQI.setText(station.getData().getCurrent().getPollution().getAqius().toString());
        cityAQIRating.setText(rankAQIUS(station.getData().getCurrent().getPollution().getAqius()));

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        Log.d("Time", "24 hours: " + hour);

        //Changing the background image depending on the time of day
        //Also changes the status bar color if the device has
        //Android Lollipop or above
        if (getContext() != null) {
            if (hour >= 0 && hour < 5) {
                currentLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_night));
                Window window = Objects.requireNonNull(getActivity()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#041b20"));
            } else if (hour >= 5 && hour < 7) {
                currentLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_sunrise));
                Window window = Objects.requireNonNull(getActivity()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#060f18"));
            } else if (hour >= 7 && hour < 17) {
                currentLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_sunny));
                Window window = Objects.requireNonNull(getActivity()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#08253b"));
            } else if (hour >= 17 && hour < 20) {
                currentLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_sunset));
                Window window = Objects.requireNonNull(getActivity()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#20244c"));
            } else if (hour >= 20 && hour < 24) {
                currentLayout.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.bg_night));
                Window window = Objects.requireNonNull(getActivity()).getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.parseColor("#041b20"));
            }
        }

        //Display the information after it's been loaded
        //Hide the progress bar and show the info.
        currentPanel.setVisibility(View.VISIBLE);
        currentProgressBar.setVisibility(View.GONE);
    }

    /**
     * This function loads in the current air quality information based on the
     * user's IP address location. Only if API level is lower than 26.
     *
     * @param station - a Station object that contains information from the API
     */
    private void loadCurrentDataTarget(Station station) {
        cityName.setText(station.getData().getCity());
        cityTimeStamp.setText(station.getData().getCurrent().getPollution().getTs());
        cityAQI.setText(station.getData().getCurrent().getPollution().getAqius().toString());
        cityAQIRating.setText(rankAQIUS(station.getData().getCurrent().getPollution().getAqius()));

        int hour = new Time(System.currentTimeMillis()).getHours();

        Log.d("Time", "24 hours: " + hour);

        //Changing the background image depending on the time of day
        //Also changes the status bar color if the device has
        //Android Lollipop or above
        if (getContext() != null) {
            if (hour >= 0 && hour < 5) {
                currentLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_night));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.parseColor("#041b20"));
                }
            } else if (hour >= 5 && hour < 7) {
                currentLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_sunrise));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.parseColor("#060f18"));

                }
            } else if (hour >= 7 && hour < 17) {
                currentLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_sunny));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.parseColor("#08253b"));

                }
            } else if (hour >= 17 && hour < 20) {
                currentLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_sunset));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.parseColor("#20244c"));

                }
            } else if (hour >= 20 && hour < 24) {
                currentLayout.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_night));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getActivity().getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(Color.parseColor("#041b20"));

                }
            }
        }

        //Display the information after it's been loaded
        //Hide the progress bar and show the info.
        currentPanel.setVisibility(View.VISIBLE);
        currentProgressBar.setVisibility(View.GONE);
    }

    /**
     * This method converts a ISO-8601 timestamp to a readable date.
     *
     * @param timestamp - a String of ISO-8601 compliant timestamp
     * @return - a String of the formatted date in words
     */
    @TargetApi(26)
    private String decodeTimestamp(String timestamp) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(timestamp, inputFormatter);
        String formattedDate = outputFormatter.format(date);
        return formattedDate;
    }

    /**
     * This method converts a numerical AQI value to its severity ranking in words
     *
     * @param aqius - This is the air quality index by U.S. EPA standards
     * @return the rank of the air quality index, a String
     */
    private String rankAQIUS(int aqius) {
        if (aqius >= 0 && aqius <= 50) {
            return "Good";
        } else if (aqius >= 51 && aqius <= 100) {
            return "Moderate";
        } else if (aqius >= 101 && aqius <= 150) {
            return "Unhealthy for Sensitive Groups";
        } else if (aqius >= 151 && aqius <= 200) {
            return "Unhealthy";
        } else if (aqius >= 201 && aqius <= 300) {
            return "Very Unhealthy";
        } else if (aqius >= 301) {
            return "Hazardous";
        } else {
            return "ERROR";
        }
    }
}