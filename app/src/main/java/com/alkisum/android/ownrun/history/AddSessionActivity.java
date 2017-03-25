package com.alkisum.android.ownrun.history;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.data.Db;
import com.alkisum.android.ownrun.dialog.DistanceDialog;
import com.alkisum.android.ownrun.dialog.DurationDialog;
import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.model.SessionDao;
import com.alkisum.android.ownrun.utils.Format;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity to add a session manually to the database.
 *
 * @author Alkisum
 * @version 2.2
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
    ListView mListView;

    /**
     * Session to add to the database.
     */
    private Session mSession;

    /**
     * Adapter for the ListView containing the session's attributes to set.
     */
    private AddSessionListAdapter mListAdapter;

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
        Toolbar toolbar = ButterKnife.findById(this, R.id.add_session_toolbar);
        toolbar.setTitle(getString(R.string.add_session_title));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        onBackPressed();
                    }
                });

        Calendar now = Calendar.getInstance();
        mSession = new Session();
        mSession.setStart(now.getTimeInMillis());
        long duration = Format.getMillisFromTime(1, 0, 0);
        mSession.setEnd(now.getTimeInMillis() + duration);
        mSession.setDuration(duration);
        mSession.setDistance(0f);
        mListAdapter = new AddSessionListAdapter(this, mSession);
        mListView.setAdapter(mListAdapter);

        mListView.setOnItemClickListener(onItemClickListener);
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
                dao.insert(mSession);
                setResult(HistoryActivity.SESSION_ADDED);
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
        dateCalendar.setTimeInMillis(mSession.getStart());
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                dateCalendar.get(Calendar.YEAR),
                dateCalendar.get(Calendar.MONTH),
                dateCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dpd.setThemeDark(true);
        dpd.show(getFragmentManager(), TAG_DATE_PICKER_DIALOG);
    }

    /**
     * Show the TimePicker dialog.
     */
    private void showTimePickerDialog() {
        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTimeInMillis(mSession.getStart());
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                timeCalendar.get(Calendar.HOUR_OF_DAY),
                timeCalendar.get(Calendar.MINUTE),
                true
        );
        tpd.setThemeDark(true);
        tpd.show(getFragmentManager(), TAG_TIME_PICKER_DIALOG);
    }

    /**
     * Show the Duration dialog.
     */
    private void showDurationDialog() {
        DurationDialog durationDialog = DurationDialog.newInstance(
                Format.getHourFromMillis(mSession.getDuration()),
                Format.getMinuteFromMillis(mSession.getDuration()),
                Format.getSecondFromMillis(mSession.getDuration())
        );
        durationDialog.show(getSupportFragmentManager(),
                DurationDialog.FRAGMENT_TAG);
    }

    /**
     * Show the Distance dialog.
     */
    private void showDistanceDialog() {
        DistanceDialog distanceDialog = DistanceDialog.newInstance(
                mSession.getDistance()
        );
        distanceDialog.show(getSupportFragmentManager(),
                DistanceDialog.FRAGMENT_TAG);
    }

    @Override
    public final void onDateSet(final DatePickerDialog view, final int year,
                                final int monthOfYear, final int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        // Set calendar with old time
        calendar.setTimeInMillis(mSession.getStart());
        // Change calendar's date
        calendar.set(year, monthOfYear, dayOfMonth);
        long timeInMillis = calendar.getTimeInMillis();
        mSession.setStart(timeInMillis);
        mSession.setEnd(timeInMillis + mSession.getDuration());
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public final void onTimeSet(final TimePickerDialog view,
                                final int hourOfDay, final int minute,
                                final int second) {
        Calendar calendar = Calendar.getInstance();
        // Set calendar with old time
        calendar.setTimeInMillis(mSession.getStart());
        // Change calendar's time
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, second);
        long timeInMillis = calendar.getTimeInMillis();
        mSession.setStart(timeInMillis);
        mSession.setEnd(timeInMillis + mSession.getDuration());
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public final void onDurationSubmit(final int hour, final int minute,
                                       final int second) {
        long duration = Format.getMillisFromTime(hour, minute, second);
        mSession.setDuration(duration);
        mSession.setEnd(mSession.getStart() + duration);
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public final void onDistanceSubmit(final float distance) {
        mSession.setDistance(distance);
        mListAdapter.notifyDataSetChanged();
    }
}
