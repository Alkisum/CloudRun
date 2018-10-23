package com.alkisum.android.cloudrun.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.utils.Format;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * List adapter for AddSessionActivity.
 *
 * @author Alkisum
 * @version 4.0
 * @since 2.0
 */
public class AddSessionListAdapter extends BaseAdapter {

    /**
     * Date position in ListView.
     */
    public static final int DATE = 0;

    /**
     * Time position in ListView.
     */
    public static final int TIME = 1;

    /**
     * Duration position in ListView.
     */
    public static final int DURATION = 2;

    /**
     * Distance position in ListView.
     */
    public static final int DISTANCE = 3;

    /**
     * Context.
     */
    private final Context context;

    /**
     * Session instance to add to database.
     */
    private final Session session;

    /**
     * List of session's attributes to set.
     */
    private final List<Integer> attributes;

    /**
     * AddSessionListAdapter constructor.
     *
     * @param context Context
     * @param session Session instance to add to database
     */
    public AddSessionListAdapter(final Context context, final Session session) {
        this.context = context;
        this.session = session;
        attributes = new ArrayList<>();
        attributes.add(DATE);
        attributes.add(TIME);
        attributes.add(DURATION);
        attributes.add(DISTANCE);
    }

    @Override
    public final int getCount() {
        return attributes.size();
    }

    @Override
    public final Integer getItem(final int i) {
        return attributes.get(i);
    }

    @Override
    public final long getItemId(final int i) {
        return i;
    }

    @Override
    public final View getView(final int position, final View convertView,
                        final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = convertView;
        if (view == null || view.getTag() == null) {
            view = inflater.inflate(R.layout.list_item_add_session, parent,
                    false);
            final ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
        }
        final ViewHolder holder = (ViewHolder) view.getTag();

        switch (getItem(position)) {
            case DATE:
                holder.icon.setImageResource(
                        R.drawable.ic_date_range_accent_24dp);
                holder.label.setText(R.string.add_session_date);
                holder.value.setText(Format.getDateAddSession().format(
                        session.getStart()));
                break;
            case TIME:
                holder.icon.setImageResource(
                        R.drawable.ic_access_time_accent_24dp);
                holder.label.setText(R.string.add_session_time);
                holder.value.setText(Format.getTimeAddSession().format(
                        session.getStart()));
                break;
            case DURATION:
                holder.icon.setImageResource(
                        R.drawable.ic_timer_accent_24dp);
                holder.label.setText(R.string.add_session_duration);
                holder.value.setText(Format.formatDuration(
                        session.getDuration()));
                break;
            case DISTANCE:
                holder.icon.setImageResource(
                        R.drawable.ic_directions_run_accent_24dp);
                holder.label.setText(R.string.add_session_distance);
                holder.value.setText(String.format(Locale.getDefault(), "%s %s",
                        Format.formatDistance(session.getDistance()), "km"));
                break;
            default:
                break;
        }

        return view;
    }

    /**
     * ViewHolder for the ListAdapter.
     */
    static class ViewHolder {

        /**
         * Attribute's icon.
         */
        @BindView(R.id.list_item_add_session_icon)
        ImageView icon;

        /**
         * Attribute's label.
         */
        @BindView(R.id.list_item_add_session_label)
        TextView label;

        /**
         * Attribute's value.
         */
        @BindView(R.id.list_item_add_session_value)
        TextView value;

        /**
         * ViewHolder constructor.
         *
         * @param view View
         */
        ViewHolder(final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
