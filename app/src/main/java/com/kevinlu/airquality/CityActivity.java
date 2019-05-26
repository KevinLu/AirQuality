package com.kevinlu.airquality;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.kc.unsplash.Unsplash;
import com.kc.unsplash.models.Download;
import com.kc.unsplash.models.Photo;
import com.kc.unsplash.models.SearchResults;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.kevinlu.airquality.ListFragment.EXTRA_COORDINATES;
import static com.kevinlu.airquality.ListFragment.EXTRA_STATION_JSON;

/**
 * The CityActivity class extends the AppCompatActivity.
 * It shows crucial air quality information to the user based
 * on the selected Station object.
 *
 * @author Kevin Lu <649859 @ pdsb.net>
 * @version 1.0
 * @since JDK 1.8
 */
public class CityActivity extends AppCompatActivity {

    private TextView textViewAirQualityComment;
    private TextView textViewAirQualitySuggestion;
    private TextView textViewPhotoDetails;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        Gson gson = new Gson();

        //Create an Intent object to get details on which
        //Station was clicked.

        Intent intent = getIntent();

        String stationJSON = intent.getStringExtra(EXTRA_STATION_JSON);
        Station station = gson.fromJson(stationJSON, Station.class);

        String countryName = station.getData().getCountry();
        String cityName = station.getData().getCity();
        String coordinates = intent.getStringExtra(EXTRA_COORDINATES);
        String timestamp = station.getData().getCurrent().getPollution().getTs();
        String aqiUS = station.getData().getCurrent().getPollution().getAqius() + "";
        String mainPollutantUS = station.getData().getCurrent().getPollution().getMainus();
        String aqiCN = station.getData().getCurrent().getPollution().getAqicn() + "";
        String mainPollutantCN = station.getData().getCurrent().getPollution().getMaincn();

        ImageView imageView = findViewById(R.id.cityImage);
        TextView textViewCoordinates = findViewById(R.id.cityCoordinates);
        TextView textViewTimestamp = findViewById(R.id.cityTimestamp);
        TextView textViewAQIUS = findViewById(R.id.cityAQIUS);
        TextView textViewMainPollutantUS = findViewById(R.id.cityMainPollutantUS);
        TextView textViewAQICN = findViewById(R.id.cityAQICN);
        TextView textViewMainPollutantCN = findViewById(R.id.cityMainPollutantCN);
        TextView textViewAirQualityWarning = findViewById(R.id.airquality_warning);
        textViewAirQualityComment = findViewById(R.id.airquality_comment);
        textViewAirQualitySuggestion = findViewById(R.id.airquality_suggestion);
        textViewPhotoDetails = findViewById(R.id.cityPhotoDetails);

        Toolbar toolbar = findViewById(R.id.cityToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitle(cityName);

        textViewCoordinates.setText(coordinates);
        textViewTimestamp.setText(timestamp);
        textViewAQIUS.setText("U.S. AQI: " + aqiUS);
        textViewMainPollutantUS.setText("U.S. Main Pollutant: " + decodePollutant(mainPollutantUS));
        textViewAQICN.setText("China AQI: " + aqiCN);
        textViewMainPollutantCN.setText("China Main Pollutant: " + decodePollutant(mainPollutantCN));
        textViewAirQualityWarning.setText(rankAQIUS(Integer.valueOf(aqiUS)));
        setAirQualityComment(Integer.valueOf(aqiUS));

        loadHeaderImageFromUnsplash(cityName, countryName, imageView);
    }

    private void setAirQualityComment(int aqius) {
        if (aqius >= 0 && aqius <= 50) {
            textViewAirQualityComment.setText("Air quality is considered satisfactory, and air pollution poses little or no risk.");
        } else if (aqius >= 51 && aqius <= 100) {
            textViewAirQualityComment.setText("Air quality is acceptable; however, for some pollutants there may be a moderate" +
                    " health concern for a very small number of people. For example, people who are unusually sensitive to ozone " +
                    "may experience respiratory symptoms.");
        } else if (aqius >= 101 && aqius <= 150) {
            textViewAirQualityComment.setText("Although general public is not likely to be affected at this AQI range, " +
                    "people with lung disease, older adults and children are at a greater risk from exposure to ozone, " +
                    "whereas persons with heart and lung disease, older adults and children are at greater risk from the " +
                    "presence of particles in the air.");
        } else if (aqius >= 151 && aqius <= 200) {
            textViewAirQualityComment.setText("Everyone may begin to experience some adverse health effects, and members of " +
                    "the sensitive groups may experience more serious effects.");
        } else if (aqius >= 201 && aqius <= 300) {
            textViewAirQualityComment.setText("This would trigger a health alert signifying that everyone may experience more serious health effects.");
        } else if (aqius >= 301) {
            textViewAirQualityComment.setText("This would trigger a health warnings of emergency conditions. " +
                    "The entire population is more likely to be affected. Please visit the link below for assistance!");
            textViewAirQualitySuggestion.setVisibility(View.VISIBLE);
        } else {
            textViewAirQualityComment.setText("The air quality cannot be determined.");
        }
    }

    /**
     * This method converts a numerical AQI value to its severity ranking in words
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

    /**
     * This method navigates back to the previous activity.
     *
     * @return - true, when the back button on the toolbar
     * is pressed.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * This function uses an Unsplash library to load a header image
     * corresponding to the city of interest.
     *
     * @param countryName  - a String, the name of the city.
     * @param imageView - the ImageView that should hold this image.
     */
    private void loadHeaderImageFromUnsplash(String cityName, String countryName, ImageView imageView) {
        Unsplash unsplash = new Unsplash("2ef4adbbc6aa68fb62acbf7170933cbc25526420aa6da426c16cb0c08ce5cffd");
        unsplash.searchPhotos(cityName, new Unsplash.OnSearchCompleteListener() {
            @Override
            public void onComplete(SearchResults results) {
                Log.d("City Photos", "Total Results Found " + results.getTotal());
                List<Photo> cityPhotos = results.getResults();

                //If can't find any images of the city, switch to find images of its country
                if (cityPhotos.size() == 0) {
                    unsplash.searchPhotos(countryName, new Unsplash.OnSearchCompleteListener() {
                        @Override
                        public void onComplete(SearchResults results) {
                            List<Photo> countryPhotos = results.getResults();
                            int random = (int) (Math.random() * cityPhotos.size());
                            String imageLink = countryPhotos.get(random).getUrls().getRegular() + "&utm_source=AirQuality&utm_medium=referral&utm_campaign=api-credit";
                            String photographer = countryPhotos.get(random).getUser().getName();
                            String photographerLink = "https://unsplash.com/@" + countryPhotos.get(random).getUser().getUsername() + "&utm_source=AirQuality&utm_medium=referral&utm_campaign=api-credit";
                            Log.d("LINK", photographerLink);
                            Log.d("LINK", imageLink);
                            Picasso.get().load(imageLink).fit().centerCrop().into(imageView);
                            textViewPhotoDetails.setText(Html.fromHtml("<a href=" + photographerLink + "> " + photographer));
                            textViewPhotoDetails.setMovementMethod(LinkMovementMethod.getInstance());
                        }

                        @Override
                        public void onError(String error) {
                            Log.d("Unsplash", error);
                        }
                    });
                } else {
                    int random = (int) (Math.random() * cityPhotos.size());
                    String imageLink = cityPhotos.get(random).getUrls().getRegular() + "&utm_source=AirQuality&utm_medium=referral&utm_campaign=api-credit";
                    String photographer = cityPhotos.get(random).getUser().getName();
                    String photographerLink = "https://unsplash.com/@" + cityPhotos.get(random).getUser().getUsername() + "&utm_source=AirQuality&utm_medium=referral&utm_campaign=api-credit";
                    Log.d("LINK", photographerLink);
                    Log.d("LINK", imageLink);
                    Picasso.get().load(imageLink).fit().centerCrop().into(imageView);
                    textViewPhotoDetails.setText(Html.fromHtml("<a href=" + photographerLink + "> " + photographer));
                    textViewPhotoDetails.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }

            @Override
            public void onError(String error) { Log.d("Unsplash", error); }
        });
    }

    /**
     * This method converts short form of pollutant names to its full form
     *
     * @param mainPollutant - This is a String of the short form of the pollutant name
     * @return Full form of the short form pollutant
     */
    private String decodePollutant(String mainPollutant) {
        switch (mainPollutant) {
            case "p2":
                return "PM 2.5";
            case "p1":
                return "PM 10";
            case "o3":
                return "Ozone";
            case "n2":
                return "Nitrogen Dioxide";
            case "s2":
                return "Sulfur Dioxide";
            case "co":
                return "Carbon Monoxide";
            default:
                return "Unknown Pollutant";
        }
    }
}
