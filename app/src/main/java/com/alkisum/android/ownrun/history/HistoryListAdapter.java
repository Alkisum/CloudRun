package com.alkisum.android.ownrun.history;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
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
 * @version 1.1
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
     * Flag set to true if the ListView is in edit mode, false otherwise.
     */
    private boolean mEditMode;

    /**
     * Color for checkboxes.
     */
    private final ColorStateList mCheckBoxColor;

    /**
     * Color for highlighted checkboxes.
     */
    private final ColorStateList mHighlightedCheckBoxColor;

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
        resetSessionsSelectedStates();
        mHighlightedSessionId = highlightedSessionId;

        // Checkbox colors
        mCheckBoxColor = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_enabled}},
                new int[]{ContextCompat.getColor(mContext, R.color.accent)});
        mHighlightedCheckBoxColor = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_enabled}},
                new int[]{ContextCompat.getColor(mContext,
                        android.R.color.white)});
    }

    /**
     * Set new list of sessions.
     *
     * @param sessions List of sessions to set
     */
    public final void setSessions(final List<Session> sessions) {
        mSessions = sessions;
    }

    /**
     * @return True if the ListView is in edit mode, false otherwise
     */
    public final boolean isEditMode() {
        return mEditMode;
    }

    /**
     * Disable the edit mode and reset the sessions' selected states.
     */
    public final void disableEditMode() {
        mEditMode = false;
        resetSessionsSelectedStates();
    }

    /**
     * Switch ListView into edit mode and select the checkbox attached to the
     * item that has been pressed long.
     *
     * @param position Position of the item that has been pressed long
     */
    public final void enableEditMode(final int position) {
        mEditMode = true;
        mSessions.get(position).setSelected(true);
    }

    /**
     * Reverse the session selected state.
     *
     * @param position Position of the item that has been pressed
     */
    public final void changeSessionSelectedState(final int position) {
        Session session = mSessions.get(position);
        session.setSelected(!session.getSelected());
    }

    /**
     * Reset the sessions' selected states. Mandatory to avoid the sessions to
     * be selected from actions performed during last HistoryActivity instance.
     */
    private void resetSessionsSelectedStates() {
        for (Session session : mSessions) {
            session.setSelected(false);
        }
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

        // Set values
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
            // End of session unavailable, cannot calculate values
            holder.distance.setText(R.string.not_available);
            holder.duration.setText(R.string.not_available);
            holder.speed.setText(R.string.not_available);
            holder.pace.setText(R.string.not_available);
        }

        // Set colors
        if (session.getId().equals(mHighlightedSessionId)) {
            // Highlighted item
            holder.layout.setBackgroundColor(ContextCompat.getColor(
                    mContext, R.color.primary));
            holder.dateTime.setTextColor(ContextCompat.getColor(
                    mContext, android.R.color.white));
            holder.checkBox.setSupportButtonTintList(mHighlightedCheckBoxColor);
        } else {
            // Default item
            holder.layout.setBackgroundColor(ContextCompat.getColor(
                    mContext, android.R.color.transparent));
            holder.dateTime.setTextColor(ContextCompat.getColor(
                    mContext, R.color.accent));
            holder.checkBox.setSupportButtonTintList(mCheckBoxColor);
        }

        // Handle checkboxes according to the mode
        holder.checkBox.setChecked(session.getSelected());
        if (mEditMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    session.setSelected(holder.checkBox.isChecked());
                }
            });
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        return view;
    }

    /**
     * ViewHolder for history list adapter.
     */
    static class ViewHolder {

        /**
         * Item layout.
         */
        @BindView(R.id.history_list_layout)
        RelativeLayout layout;

        /**
         * Checkbox to select the item in edit mode.
         */
        @BindView(R.id.history_list_checkbox)
        AppCompatCheckBox checkBox;

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
         * ViewHolder constructor.
         *
         * @param view View to bind with ButterKnife
         */
        ViewHolder(final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
