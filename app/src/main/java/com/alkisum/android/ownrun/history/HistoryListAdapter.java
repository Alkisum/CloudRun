package com.alkisum.android.ownrun.history;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.utils.Format;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for History ListView.
 *
 * @author Alkisum
 * @version 1.0
 * @since 1.0
 */
public class HistoryListAdapter extends BaseAdapter {

    /**
     * Context.
     */
    private final Context mContext;

    /**
     * List of sessions.
     */
    private List<Session> mSessions;

    /**
     * ID of the session to be highlighted, used after a session has been
     * stopped. The ID is null if the user started the HistoryActivity manually.
     */
    private final Long mHighlightedSessionId;

    /**
     * HistoryListAdapter constructor.
     *
     * @param context              Context
     * @param sessions             List of sessions
     * @param highlightedSessionId ID of the session to be highlighted, used
     *                             after a session has been stopped.
     *                             The ID is null if the user started
     *                             the HistoryActivity manually
     */
    public HistoryListAdapter(final Context context,
                              final List<Session> sessions,
                              final Long highlightedSessionId) {
        mContext = context;
        mSessions = sessions;
        mHighlightedSessionId = highlightedSessionId;
    }

    /**
     * Set new list of sessions.
     *
     * @param sessions List of sessions to set
     */
    public final void setSessions(final List<Session> sessions) {
        mSessions = sessions;
    }

    @Override
    public final int getCount() {
        return mSessions.size();
    }

    @Override
    public final Object getItem(final int i) {
        return mSessions.get(i);
    }

    @Override
    public final long getItemId(final int i) {
        return mSessions.get(i).getId();
    }

    @Override
    public final View getView(final int i, final View v,
                              final ViewGroup viewGroup) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = v;
        if (view == null || view.getTag() == null) {
            view = inflater.inflate(R.layout.history_list_item, viewGroup,
                    false);
            final ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
        }
        final Session session = (Session) getItem(i);
        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.dateTime.setText(Format.DATE_TIME_HISTORY.format(
                new Date(session.getStart())));
        if (session.getEnd() != null) {
            long duration = session.getEnd() - session.getStart();
            holder.distance.setText(String.format(Locale.getDefault(),
                    Format.DISTANCE + " km", session.getDistance() / 1000f));
            holder.duration.setText(Format.formatDuration(duration));
            holder.speed.setText(String.format("%s km/h",
                    Format.formatSpeedAvg(duration, session.getDistance())));
            holder.pace.setText(String.format("%s min/km",
                    Format.formatPaceAvg(duration, session.getDistance())));
        } else {
            holder.distance.setText(R.string.not_available);
            holder.duration.setText(R.string.not_available);
            holder.speed.setText(R.string.not_available);
            holder.pace.setText(R.string.not_available);
        }

        if (session.getId().equals(mHighlightedSessionId)) {
            // Highlighted item
            holder.layout.setBackgroundColor(ContextCompat.getColor(
                    mContext, R.color.primary));
            holder.dateTime.setTextColor(ContextCompat.getColor(
                    mContext, android.R.color.white));
        } else {
            // Default item
            holder.layout.setBackgroundColor(ContextCompat.getColor(
                    mContext, android.R.color.transparent));
            holder.dateTime.setTextColor(ContextCompat.getColor(
                    mContext, R.color.accent));
        }

        return view;
    }

    /**
     * ViewHolder for device information list adapter.
     */
    static class ViewHolder {

        /**
         * Item layout.
         */
        @BindView(R.id.history_list_layout)
        RelativeLayout layout;

        /**
         * TextView for date and time.
         */
        @BindView(R.id.history_list_date_time)
        TextView dateTime;

        /**
         * TextView for distance.
         */
        @BindView(R.id.history_list_distance)
        TextView distance;

        /**
         * TextView for duration.
         */
        @BindView(R.id.history_list_duration)
        TextView duration;

        /**
         * TextView for speed.
         */
        @BindView(R.id.history_list_speed)
        TextView speed;

        /**
         * TextView for pace.
         */
        @BindView(R.id.history_list_pace)
        TextView pace;

        /**
         * DeviceViewHolder constructor.
         *
         * @param view View to bind with ButterKnife
         */
        ViewHolder(final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
