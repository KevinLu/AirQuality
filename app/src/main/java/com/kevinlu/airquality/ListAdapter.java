package com.kevinlu.airquality;

import android.content.Context;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * The ListAdapter class extends the RecyclerView.ListAdapter class.
 * It adds and updates the RecyclerView with data from a ArrayList
 *
 * @author Kevin Lu <649859 @ pdsb.net>
 * @since JDK 1.8
 * @version 1.0
 *
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Station> stationList;
    private OnItemClickListener onItemClickListener;

    /**
     * The OnItemClickListener interface specifies that the method
     * onItemClick must be implemented when this interface is used.
     *
     * The onItemClick method should take which item in the RecyclerView
     * is clicked and transfer its information to a new activity.
     */
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    /**
     * Sets the OnItemClickListener.
     * @param listener - an OnItemClickListener interface
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    /**
     * This is the constructor for the ListAdapter class.
     * @param context - This is a Context object
     * @param stationList - This is an ArrayList of Station objects
     */
    public ListAdapter(Context context, ArrayList<Station> stationList) {
        this.context = context;
        this.stationList = stationList;
    }

    /**
     * @param parent ViewGroup object
     * @param viewType an integer
     * @return the ViewHolder object
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_listitem, null);
        return new ViewHolder(view);
    }

    /**
     * This function sets the information from the ArrayList of Station objects
     * @param holder - This is a ViewHolder object
     * @param position - This is the index of the Station in the ArrayList
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Grab the station from the station list
        Station station = stationList.get(position);

        //Create a String for the location of the station (City, Province, Country)
        String stationLocation = station.getData().getCity() + ", " + station.getData().getState() + ", " + station.getData().getCountry();
        //Setting the texts and images based on the AQI
        holder.textViewTitle.setText(stationLocation);
        //BUG: AirVisual API no longer returns the Main Pollutant data from their Community API.
        //holder.textViewShortDesc.setText(decodePollutant(station.getData().getCurrent().getPollution().getMainus()));
        holder.textViewShortDesc.setText("");
        int aqius = station.getData().getCurrent().getPollution().getAqius();
        setIconAQIUS(holder, aqius);
        holder.textViewRating.setText(aqius + "");
        setColorAQIUS(holder, aqius);
        holder.textViewPrice.setText(rankAQIUS(aqius));
        //DEPRECATED. NOW USING AN INTERFACE TO LAUNCH NEW ACTIVITY.
//        holder.itemView.setOnClickListener(view -> {
//            Intent intent = new Intent(context, CityActivity.class);
//            context.startActivity(intent);
//        });
    }

    /**
     * This method converts short form of pollutant names to its full form
     * @param mainPollutant - This is a String of the short form of the pollutant name
     * @return Full form of the short form pollutant
     */
    private String decodePollutant(String mainPollutant) {
        switch (mainPollutant) {
            case "p2": return "PM 2.5";
            case "p1": return "PM 10";
            case "o3": return "Ozone";
            case "n2": return "Nitrogen Dioxide";
            case "s2": return "Sulfur Dioxide";
            case "co": return "Carbon Monoxide";
            default: return "Unknown Pollutant";
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
     * This function sets the color corresponding to the AQI
     * @param holder - A ViewHolder object
     * @param aqius - This is the air quality index by U.S. EPA standards
     */
    private void setColorAQIUS(ViewHolder holder, int aqius) {
        if (aqius >= 0 && aqius <= 50) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_good));
        } else if (aqius >= 51 && aqius <= 100) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_moderate));
        } else if (aqius >= 101 && aqius <= 150) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_unhealthyforsensitivegroups));
        } else if (aqius >= 151 && aqius <= 200) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_unhealthy));
        } else if (aqius >= 201 && aqius <= 300) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_veryunhealthy));
        } else if (aqius >= 301) {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.aqius_hazardous));
        } else {
            holder.textViewRating.setBackgroundColor(ContextCompat.getColor(context, R.color.colorError));
        }
    }

    /**
     * This function sets the icon corresponding to the AQI
     * @param holder - A ViewHolder object
     * @param aqius - This is the air quality index by U.S. EPA standards
     */
    private void setIconAQIUS(ViewHolder holder, int aqius) {
        if (aqius >= 0 && aqius <= 50) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_good));
        } else if (aqius >= 51 && aqius <= 100) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_moderate));
        } else if (aqius >= 101 && aqius <= 150) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_unhealthy_for_sensitive_groups));
        } else if (aqius >= 151 && aqius <= 200) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_unhealthy));
        } else if (aqius >= 201 && aqius <= 300) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_very_unhealthy));
        } else if (aqius >= 301) {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_face_hazardous));
        } else {
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_map_marker_error));
        }
    }

    /**
     * This method returns the size of the ArrayList
     * @return size of ArrayList, an Integer
     */
    @Override
    public int getItemCount() {
        return stationList.size();
    }

    public void addItem(Station station) {
        stationList.add(station);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemInserted(stationList.size());
    }

    public void restoreItem(Station station, int position) {
        stationList.add(position, station);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        Station station = stationList.get(position);
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        dbHelper.deleteStationRecord(station.getData().getCity(), context);
        stationList.remove(position);
        // notify item added by position
        notifyItemRemoved(position);
        //notifyItemRangeChanged(position, stationList.size());
    }

    /**
     * This ViewHolder class finds all the fields used in the RecyclerView
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewTitle, textViewShortDesc, textViewRating, textViewPrice;
        RelativeLayout listViewForeground, listViewBackground;

        //LinearLayout linearLayout;

        ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.listCityImage);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewShortDesc = itemView.findViewById(R.id.textViewShortDesc);
            textViewRating = itemView.findViewById(R.id.textViewRating);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            listViewForeground = itemView.findViewById(R.id.listItemForeground);
            listViewBackground = itemView.findViewById(R.id.listItemBackground);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClickListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    /**
     * This function sets the stationList to the new filteredList.
     * @param filteredList - an ArrayList of Station objects.
     */
    public void filterList(ArrayList<Station> filteredList) {
        stationList = filteredList;
        notifyDataSetChanged();
    }
}
