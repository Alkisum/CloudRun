package com.alkisum.android.cloudrun.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.utils.Format;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for History ListView.
 *
 * @author Alkisum
 * @version 4.0
 * @since 1.0
 */
public class HistoryListAdapter extends BaseAdapter {

    /**
     * Context.
     */
    private final Context context;

    /**
     * List of sessions.
     */
    private final List<Session> sessions;

    /**
     * ID of the session to be highlighted, used after a session has been
     * stopped. The ID is null if the user started the HistoryActivity manually.
     */
    private final Long highlightedSessionId;

    /**
     * Flag set to true if the ListView is in edit mode, false otherwise.
     */
    private boolean editMode;

    /**
     * Color for checkboxes.
     */
    private final ColorStateList checkBoxColor;

    /**
     * Color for highlighted checkboxes.
     */
    private final ColorStateList highlightedCheckBoxColor;

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
        this.context = context;
        this.sessions = sessions;
        resetSessionsSelectedStates();
        this.highlightedSessionId = highlightedSessionId;

        // Checkbox colors
        checkBoxColor = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_enabled}},
                new int[]{ContextCompat.getColor(this.context,
                        R.color.accent)});
        highlightedCheckBoxColor = new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_enabled}},
                new int[]{ContextCompat.getColor(this.context,
                        android.R.color.white)});
    }

    /**
     * Set new list of sessions.
     *
     * @param sessions List of sessions to set
     */
    public final void setSessions(final List<Session> sessions) {
        this.sessions.clear();
        this.sessions.addAll(sessions);
    }

    /**
     * @return True if the ListView is in edit mode, false otherwise
     */
    public final boolean isEditMode() {
        return editMode;
    }

    /**
     * Disable the edit mode and reset the sessions' selected states.
     */
    public final void disableEditMode() {
        editMode = false;
        resetSessionsSelectedStates();
    }

    /**
     * Switch ListView into edit mode and select the checkbox attached to the
     * item that has been pressed long.
     *
     * @param position Position of the item that has been pressed long
     */
    public final void enableEditMode(final int position) {
        editMode = true;
        sessions.get(position).setSelected(true);
    }

    /**
     * Reverse the session selected state.
     *
     * @param position Position of the item that has been pressed
     */
    public final void changeSessionSelectedState(final int position) {
        Session session = sessions.get(position);
        session.setSelected(!session.getSelected());
    }

    /**
     * Reset the sessions' selected states. Mandatory to avoid the sessions to
     * be selected from actions performed during last HistoryActivity instance.
     */
    private void resetSessionsSelectedStates() {
        for (Session session : sessions) {
            session.setSelected(false);
        }
    }

    @Override
    public final int getCount() {
        return sessions.size();
    }

    @Override
    public final Object getItem(final int i) {
        return sessions.get(i);
    }

    @Override
    public final long getItemId(final int i) {
        return sessions.get(i).getId();
    }

    @Override
    public final View getView(final int i, final View v,
                              final ViewGroup viewGroup) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View view = v;
        if (view == null || view.getTag() == null) {
            view = inflater.inflate(R.layout.list_item_history, viewGroup,
                    false);
            final ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
        }
        final Session session = (Session) getItem(i);
        final ViewHolder holder = (ViewHolder) view.getTag();

        // Set values
        holder.dateTime.setText(Format.getDateTimeHistory().format(
                new Date(session.getStart())));
        if (session.getEnd() != null) {
            long duration = session.getDuration();
            holder.distance.setText(String.format("%s %s",
                    Format.formatDistance(session.getDistance()),
                    context.getString(R.string.unit_distance)));
            holder.duration.setText(Format.formatDuration(duration));
            holder.speed.setText(String.format("%s %s",
                    Format.formatSpeedAvg(duration, session.getDistance()),
                    context.getString(R.string.unit_speed)));
            holder.pace.setText(String.format("%s %s",
                    Format.formatPaceAvg(duration, session.getDistance()),
                    context.getString(R.string.unit_pace)));
        } else {
            // End of session unavailable, cannot calculate values
            holder.distance.setText(R.string.not_available);
            holder.duration.setText(R.string.not_available);
            holder.speed.setText(R.string.not_available);
            holder.pace.setText(R.string.not_available);
        }

        // Set colors
        if (session.getId().equals(highlightedSessionId)) {
            // Highlighted item
            holder.layout.setBackgroundColor(ContextCompat.getColor(
                    context, R.color.primary));
            holder.dateTime.setTextColor(ContextCompat.getColor(
                    context, android.R.color.white));
            CompoundButtonCompat.setButtonTintList(holder.checkBox,
                    highlightedCheckBoxColor);
        } else {
            // Default item
            holder.layout.setBackgroundColor(ContextCompat.getColor(
                    context, android.R.color.transparent));
            holder.dateTime.setTextColor(ContextCompat.getColor(
                    context, R.color.accent));
            CompoundButtonCompat.setButtonTintList(holder.checkBox,
                    checkBoxColor);
        }

        // Handle checkboxes according to the mode
        holder.checkBox.setChecked(session.getSelected());
        if (editMode) {
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
