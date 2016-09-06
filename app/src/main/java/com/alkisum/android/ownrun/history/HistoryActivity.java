package com.alkisum.android.ownrun.history;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.connect.ConnectDialog;
import com.alkisum.android.ownrun.connect.ConnectInfo;
import com.alkisum.android.ownrun.data.Db;
import com.alkisum.android.ownrun.data.Uploader;
import com.alkisum.android.ownrun.model.DataPoint;
import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.model.SessionDao;
import com.alkisum.android.ownrun.dialog.ConfirmDialog;
import com.alkisum.android.ownrun.dialog.ErrorDialog;
import com.alkisum.android.ownrun.utils.Pref;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Activity listing the history of sessions.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.0
 */
public class HistoryActivity extends AppCompatActivity implements
        ConnectDialog.ConnectDialogListener, Uploader.UploaderListener {

    /**
     * Argument for the session's ID that has just been stopped. This ID is
     * given only when the history activity is started automatically after
     * the session is stopped.
     */
    public static final String ARG_HIGHLIGHTED_SESSION_ID =
            "arg_highlighted_session_id";

    /**
     * Argument for the session's ID that should not be listed because it is
     * still running. When no session is running, this argument is null.
     */
    public static final String ARG_IGNORE_SESSION_ID = "arg_ignore_session_id";

    /**
     * ListView containing the sessions.
     */
    @BindView(R.id.history_list)
    ListView mListView;

    /**
     * TextView informing the user that no sessions are available.
     */
    @BindView(R.id.history_no_session)
    TextView mNoSessionTextView;

    /**
     * List adapter for the list of session.
     */
    private HistoryListAdapter mListAdapter;

    /**
     * Progress dialog to show the progress of uploading.
     */
    private ProgressDialog mProgressDialog;

    /**
     * Uploader instance created when the user presses on the Upload item from
     * the context menu, and initialized when the connect dialog is submit.
     */
    private Uploader mUploader;

    /**
     * Session attached to the item that has been pressed by the user.
     * This session instance is used to prepare the context menu item list.
     */
    private Session mActiveSession;

    /**
     * ID of the session that should be highlighted.
     */
    private Long mHighlightedSessionId;

    /**
     * ID of the session that should be ignore because it is still running.
     */
    private Long mIgnoreSessionId;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        mHighlightedSessionId = null;
        mIgnoreSessionId = null;

        if (extras != null) {
            mHighlightedSessionId = extras.getLong(ARG_HIGHLIGHTED_SESSION_ID);
            mIgnoreSessionId = extras.getLong(ARG_IGNORE_SESSION_ID);
        }

        setGui();
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        unregisterForContextMenu(mListView);
    }

    /**
     * Set the GUI.
     */
    private void setGui() {
        Toolbar toolbar = ButterKnife.findById(this, R.id.history_toolbar);
        toolbar.setTitle(R.string.history_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });

        mListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(
                            final AdapterView<?> adapterView, final View view,
                            final int i, final long l) {
                        // Save session attached to the item to prepare the menu
                        mActiveSession = (Session) mListAdapter.getItem(i);
                        return false;
                    }
                });

        List<Session> sessions = loadSessions();
        if (sessions.isEmpty()) {
            mListView.setVisibility(View.GONE);
            mNoSessionTextView.setVisibility(View.VISIBLE);
        }
        mListAdapter = new HistoryListAdapter(this, sessions,
                mHighlightedSessionId);
        mListView.setAdapter(mListAdapter);

        registerForContextMenu(mListView);
    }

    @Override
    public final void onCreateContextMenu(
            final ContextMenu menu, final View v,
            final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_history, menu);
        if (mActiveSession.getEnd() != null) {
            // The session has been stopped properly, it can be uploaded
            menu.findItem(R.id.action_upload).setVisible(true);
            menu.findItem(R.id.action_fix).setVisible(false);
        } else {
            // The session has not been stopped properly, it should be fixed
            menu.findItem(R.id.action_upload).setVisible(false);
            menu.findItem(R.id.action_fix).setVisible(true);
        }
    }

    @Override
    public final boolean onContextItemSelected(final MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Session session = (Session) mListAdapter.getItem(info.position);
        switch (item.getItemId()) {
            case R.id.action_upload:
                DialogFragment connectDialog = new ConnectDialog();
                connectDialog.show(getSupportFragmentManager(),
                        ConnectDialog.FRAGMENT_TAG);
                mUploader = new Uploader(this, session);
                return true;
            case R.id.action_delete:
                deleteSession(session);
                return true;
            case R.id.action_fix:
                fixSession(session);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Delete the given session from the database.
     *
     * @param session Session to delete
     */
    private void deleteSession(final Session session) {
        ConfirmDialog.build(this, getString(R.string.history_delete_title),
                getString(R.string.history_delete_msg),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface,
                                        final int i) {
                        Db.getInstance().getDaoSession().getSessionDao()
                                .delete(session);
                        refreshList();
                    }
                }).show();
    }

    /**
     * Fix the session. Use DataPoints of the session to get the end and to
     * calculate the distance.
     *
     * @param session Session to fix
     */
    private void fixSession(final Session session) {
        List<DataPoint> dataPoints = session.getDataPoints();

        // If the session has no DataPoint, it cannot be fixed
        if (dataPoints.isEmpty()) {
            ConfirmDialog.build(this, getString(R.string.history_fix_title),
                    getString(R.string.history_fix_msg),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                final DialogInterface dialogInterface,
                                final int i) {
                            Db.getInstance().getDaoSession().getSessionDao()
                                    .delete(session);
                            refreshList();
                        }
                    }).show();
            return;
        }

        // Get end from the last datapoint recorded during the session
        Long end = dataPoints.get(dataPoints.size() - 1).getTime();

        // Calculate distance between each datapoint's location
        float distance = 0;
        Location src = null;
        Location dst;

        int i = 0;
        while (i < dataPoints.size()) {

            DataPoint dataPoint = dataPoints.get(i);

            if (src == null) {
                if (isDataPointValid(dataPoint)) {
                    src = new Location("src");
                    src.setLatitude(dataPoint.getLatitude());
                    src.setLongitude(dataPoint.getLongitude());
                }
            } else {
                if (isDataPointValid(dataPoint)) {
                    dst = new Location("dst");
                    dst.setLatitude(dataPoint.getLatitude());
                    dst.setLongitude(dataPoint.getLongitude());

                    distance += src.distanceTo(dst);

                    src = dst;
                }
            }
            i++;
        }

        // Update database with the new session's information
        session.setEnd(end);
        session.setDistance(distance);
        session.update();

        refreshList();
    }

    /**
     * Check if the given DataPoint is valid.
     *
     * @param dataPoint DataPoint to check
     * @return True if the DataPoint is valid, false otherwise
     */
    private boolean isDataPointValid(final DataPoint dataPoint) {
        return dataPoint.getLatitude() != null
                && dataPoint.getLongitude() != null
                && dataPoint.getLatitude() != 0
                && dataPoint.getLongitude() != 0;
    }

    /**
     * Reload the list of sessions and notify the list adapter.
     */
    private void refreshList() {
        List<Session> sessions = loadSessions();
        if (sessions.isEmpty()) {
            mListView.setVisibility(View.GONE);
            mNoSessionTextView.setVisibility(View.VISIBLE);
        }
        mListAdapter.setSessions(sessions);
        mListAdapter.notifyDataSetChanged();
    }

    /**
     * Load the sessions from the database.
     *
     * @return List of sessions in the anti-chronological order
     */
    private List<Session> loadSessions() {
        SessionDao dao = Db.getInstance().getDaoSession()
                .getSessionDao();
        List<Session> sessions = dao.loadAll();
        if (mIgnoreSessionId != null) {
            sessions.remove(dao.load(mIgnoreSessionId));
        }
        Collections.reverse(sessions);
        return sessions;
    }

    @Override
    public final void onSubmit(final ConnectInfo connectInfo) {
        if (mUploader == null) {
            return;
        }
        mUploader.init(
                connectInfo.getAddress(),
                connectInfo.getPath(),
                connectInfo.getUsername(),
                connectInfo.getPassword()).start();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(getString(
                R.string.upload_progress_init_msg));
        mProgressDialog.show();

        saveConnectInfo(connectInfo);
    }

    /**
     * Save the connection information (server address, remote path and
     * username) into the SharedPreferences to pre-fill the connect dialog.
     *
     * @param connectInfo Connection information
     */
    private void saveConnectInfo(final ConnectInfo connectInfo) {
        SharedPreferences sharedPref = getSharedPreferences(
                Pref.NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Pref.ADDRESS, connectInfo.getAddress());
        editor.putString(Pref.PATH, connectInfo.getPath());
        editor.putString(Pref.USERNAME, connectInfo.getUsername());
        editor.apply();
    }

    @Override
    public final void onWritingFileFailed(final Exception e) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        ErrorDialog.build(this, getString(
                R.string.upload_writing_failure_title), e.getMessage(), null)
                .show();
    }

    @Override
    public final void onUploading(final int percentage) {
        if (mProgressDialog != null) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMessage(
                    getString(R.string.uploading_progress_msg_1) + percentage
                            + getString(R.string.uploading_progress_msg_2));
            mProgressDialog.setProgress(percentage);
        }
    }

    @Override
    public final void onUploadDone() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        Toast.makeText(this, getString(R.string.upload_success_toast),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public final void onUploadFailed(final String message) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        ErrorDialog.build(this, getString(R.string.upload_failure_title),
                message, null).show();
    }
}
