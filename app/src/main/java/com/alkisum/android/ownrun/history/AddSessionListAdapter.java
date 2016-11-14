package com.alkisum.android.ownrun.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.utils.Format;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * List adapter for AddSessionActivity.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */
class AddSessionListAdapter extends BaseAdapter {

    /**
     * Date position in ListView.
     */
    static final int DATE = 0;

    /**
     * Time position in ListView.
     */
    static final int TIME = 1;

    /**
     * Duration position in ListView.
     */
    static final int DURATION = 2;

    /**
     * Distance position in ListView.
     */
    static final int DISTANCE = 3;

    /**
     * Context.
     */
    private final Context mContext;

    /**
     * Session instance to add to database.
     */
    private final Session mSession;

    /**
     * List of session's attributes to set.
     */
    private final List<Integer> mAttributes;

    /**
     * AddSessionListAdapter constructor.
     *
     * @param context Context
     * @param session Session instance to add to database
     */
    AddSessionListAdapter(final Context context, final Session session) {
        mContext = context;
        mSession = session;
        mAttributes = new ArrayList<>();
        mAttributes.add(DATE);
        mAttributes.add(TIME);
        mAttributes.add(DURATION);
        mAttributes.add(DISTANCE);
    }

    @Override
    public int getCount() {
        return mAttributes.size();
    }

    @Override
    public Integer getItem(final int i) {
        return mAttributes.get(i);
    }

    @Override
    public long getItemId(final int i) {
        return i;
    }

    @Override
    public View getView(final int position, final View convertView,
                        final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
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
                holder.value.setText(Format.DATE_ADD_SESSION.format(
                        mSession.getStart()));
                break;
            case TIME:
                holder.icon.setImageResource(
                        R.drawable.ic_access_time_accent_24dp);
                holder.label.setText(R.string.add_session_time);
                holder.value.setText(Format.TIME_ADD_SESSION.format(
                        mSession.getStart()));
                break;
            case DURATION:
                holder.icon.setImageResource(
                        R.drawable.ic_timer_accent_24dp);
                holder.label.setText(R.string.add_session_duration);
                holder.value.setText(Format.formatDuration(
                        mSession.getDuration()));
                break;
            case DISTANCE:
                holder.icon.setImageResource(
                        R.drawable.ic_directions_run_accent_24dp);
                holder.label.setText(R.string.add_session_distance);
                holder.value.setText(String.format(Locale.getDefault(), "%s %s",
                        Format.formatDistance(mSession.getDistance()), "km"));
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
