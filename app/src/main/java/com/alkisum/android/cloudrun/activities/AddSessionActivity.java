package com.alkisum.android.cloudrun.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.adapters.AddSessionListAdapter;
import com.alkisum.android.cloudrun.database.Db;
import com.alkisum.android.cloudrun.dialogs.DistanceDialog;
import com.alkisum.android.cloudrun.dialogs.DurationDialog;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.model.SessionDao;
import com.alkisum.android.cloudrun.utils.Format;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity to add a session manually to the database.
 *
 * @author Alkisum
 * @version 3.0
 * @since 2.0
 */
public class AddSessionActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
        DurationDialog.DurationDialogListener,
        DistanceDialog.DistanceDialogListener {

    /**
     * Tag for the DatePicker fragment.
     */
    private static final String TAG_DATE_PICKER_DIALOG = "date_picker_dialog";

    /**
     * Tag for the TimePicker fragment.
     */
    private static final String TAG_TIME_PICKER_DIALOG = "time_picker_dialog";

    /**
     * ListView containing the session attributes to set.
     */
    @BindView(R.id.add_session_list)
    ListView listView;

    /**
     * Session to add to the database.
     */
    private Session session;

    /**
     * Adapter for the ListView containing the session's attributes to set.
     */
    private AddSessionListAdapter listAdapter;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_session);
        ButterKnife.bind(this);

        setGui();
    }

    /**
     * Set GUI.
     */
    private void setGui() {
        Toolbar toolbar = findViewById(R.id.add_session_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Calendar now = Calendar.getInstance();
        session = new Session();
        session.setStart(now.getTimeInMillis());
        long duration = Format.getMillisFromTime(1, 0, 0);
        session.setEnd(now.getTimeInMillis() + duration);
        session.setDuration(duration);
        session.setDistance(0f);
        listAdapter = new AddSessionListAdapter(this, session);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(onItemClickListener);
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_session, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                SessionDao dao = Db.getInstance().getDaoSession()
                        .getSessionDao();
                dao.insert(session);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * ClickListener for the ListView items.
     */
    private final AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(final AdapterView<?> adapterView,
                                        final View view, final int position,
                                        final long id) {
                    switch (position) {
                        case AddSessionListAdapter.DATE:
                            showDatePickerDialog();
                            break;
                        case AddSessionListAdapter.TIME:
                            showTimePickerDialog();
                            break;
                        case AddSessionListAdapter.DURATION:
                            showDurationDialog();
                            break;
                        case AddSessionListAdapter.DISTANCE:
                            showDistanceDialog();
                            break;
                        default:
                            break;
                    }
                }
            };

    /**
     * Show the DatePicker dialog.
     */
    private void showDatePickerDialog() {
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTimeInMillis(session.getStart());
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                dateCalendar.get(Calendar.YEAR),
                dateCalendar.get(Calendar.MONTH),
                dateCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setThemeDark(true);
        dpd.show(getSupportFragmentManager(), TAG_DATE_PICKER_DIALOG);
    }

    /**
     * Show the TimePicker dialog.
     */
    private void showTimePickerDialog() {
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTimeInMillis(session.getStart());
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                timeCalendar.get(Calendar.HOUR_OF_DAY),
                timeCalendar.get(Calendar.MINUTE),
                true
        );
        tpd.setThemeDark(true);
        tpd.show(getSupportFragmentManager(), TAG_TIME_PICKER_DIALOG);
    }

    /**
     * Show the Duration dialog.
     */
    private void showDurationDialog() {
        DurationDialog durationDialog = DurationDialog.newInstance(
                Format.getHourFromMillis(session.getDuration()),
                Format.getMinuteFromMillis(session.getDuration()),
                Format.getSecondFromMillis(session.getDuration())
        );
        durationDialog.show(getSupportFragmentManager(),
                DurationDialog.FRAGMENT_TAG);
    }

    /**
     * Show the Distance dialog.
     */
    private void showDistanceDialog() {
        DistanceDialog distanceDialog = DistanceDialog.newInstance(
                session.getDistance()
        );
        distanceDialog.show(getSupportFragmentManager(),
                DistanceDialog.FRAGMENT_TAG);
    }

    @Override
    public final void onDateSet(final DatePickerDialog view, final int year,
                                final int monthOfYear, final int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        // Set calendar with old time
        calendar.setTimeInMillis(session.getStart());
        // Change calendar's date
        calendar.set(year, monthOfYear, dayOfMonth);
        long timeInMillis = calendar.getTimeInMillis();
        session.setStart(timeInMillis);
        session.setEnd(timeInMillis + session.getDuration());
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public final void onTimeSet(final TimePickerDialog view,
                                final int hourOfDay, final int minute,
                                final int second) {
        Calendar calendar = Calendar.getInstance();
        // Set calendar with old time
        calendar.setTimeInMillis(session.getStart());
        // Change calendar's time
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, second);
        long timeInMillis = calendar.getTimeInMillis();
        session.setStart(timeInMillis);
        session.setEnd(timeInMillis + session.getDuration());
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public final void onDurationSubmit(final int hour, final int minute,
                                       final int second) {
        long duration = Format.getMillisFromTime(hour, minute, second);
        session.setDuration(duration);
        session.setEnd(session.getStart() + duration);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public final void onDistanceSubmit(final float distance) {
        session.setDistance(distance);
        listAdapter.notifyDataSetChanged();
    }
}
