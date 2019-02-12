package com.alkisum.android.cloudrun.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alkisum.android.cloudlib.events.DownloadEvent;
import com.alkisum.android.cloudlib.events.JsonFileReaderEvent;
import com.alkisum.android.cloudlib.events.JsonFileWriterEvent;
import com.alkisum.android.cloudlib.events.UploadEvent;
import com.alkisum.android.cloudlib.net.ConnectDialog;
import com.alkisum.android.cloudlib.net.ConnectInfo;
import com.alkisum.android.cloudrun.R;
import com.alkisum.android.cloudrun.adapters.HistoryListAdapter;
import com.alkisum.android.cloudrun.dialogs.ErrorDialog;
import com.alkisum.android.cloudrun.events.DeletedEvent;
import com.alkisum.android.cloudrun.events.InsertedEvent;
import com.alkisum.android.cloudrun.events.RestoredEvent;
import com.alkisum.android.cloudrun.interfaces.Deletable;
import com.alkisum.android.cloudrun.interfaces.Restorable;
import com.alkisum.android.cloudrun.model.Session;
import com.alkisum.android.cloudrun.net.Downloader;
import com.alkisum.android.cloudrun.net.Uploader;
import com.alkisum.android.cloudrun.tasks.Deleter;
import com.alkisum.android.cloudrun.tasks.Restorer;
import com.alkisum.android.cloudrun.utils.Deletables;
import com.alkisum.android.cloudrun.utils.Sessions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity listing the history of sessions.
 *
 * @author Alkisum
 * @version 4.0
 * @since 1.0
 */
public class HistoryActivity extends AppCompatActivity implements
        ConnectDialog.ConnectDialogListener {
    /**
     * Subscriber id to use when receiving event.
     */
    private static final int SUBSCRIBER_ID = 627;

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
     * Operation id for download.
     */
    private static final int DOWNLOAD_OPERATION = 1;

    /**
     * Operation id for upload.
     */
    private static final int UPLOAD_OPERATION = 2;

    /**
     * Request code for SessionActivity result.
     */
    private static final int SESSION_REQUEST_CODE = 286;

    /**
     * Result returned by SessionActivity when the session was deleted from the
     * SessionActivity.
     */
    public static final int SESSION_DELETED = 354;

    /**
     * Toolbar.
     */
    @BindView(R.id.history_toolbar)
    Toolbar toolbar;

    /**
     * SwipeRefreshLayout for list view.
     */
    @BindView(R.id.history_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    /**
     * ListView containing the sessions.
     */
    @BindView(R.id.history_list)
    ListView listView;

    /**
     * TextView informing the user that no sessions are available.
     */
    @BindView(R.id.history_no_session)
    TextView noSessionTextView;

    /**
     * Progress bar to show the progress of operations.
     */
    @BindView(R.id.history_progressbar)
    ProgressBar progressBar;

    /**
     * Floating action button to add sessions.
     */
    @BindView(R.id.history_fab_add)
    FloatingActionButton fab;

    /**
     * List adapter for the list of session.
     */
    private HistoryListAdapter listAdapter;

    /**
     * ID of the session that should be highlighted.
     */
    private Long highlightedSessionId;

    /**
     * ID of the session that should be ignored because it is still running. The
     * ID is null if no session is running.
     */
    private Long ignoreSessionId;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        highlightedSessionId = null;
        ignoreSessionId = null;

        if (extras != null) {
            highlightedSessionId = extras.getLong(ARG_HIGHLIGHTED_SESSION_ID);
            ignoreSessionId = extras.getLong(ARG_IGNORE_SESSION_ID);
        }

        Sessions.fixSessions(ignoreSessionId);

        setGui();

        // Register onCreate to receive events even when SessionActivity is open
        EventBus.getDefault().register(this);
    }

    @Override
    protected final void onStart() {
        super.onStart();
        refreshList();
    }

    @Override
    public final void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected final void onActivityResult(final int requestCode,
                                          final int resultCode,
                                          final Intent data) {
        if (requestCode == SESSION_REQUEST_CODE) {
            if (resultCode == SESSION_DELETED) {
                String json = data.getStringExtra(
                        SessionActivity.ARG_SESSION_JSON);
                final Session session = new Gson().fromJson(
                        json, Session.class);
                Snackbar.make(fab, R.string.session_delete_snackbar,
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_undo,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(final View v) {
                                        restoreSessions(session);
                                    }
                                }).show();
            }
        }
    }

    /**
     * Set the GUI.
     */
    private void setGui() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isEditMode()) {
                    disableEditMode();
                } else {
                    finish();
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView,
                                    final View view, final int position,
                                    final long id) {
                if (listAdapter.isEditMode()) {
                    listAdapter.changeSessionSelectedState(position);
                    listAdapter.notifyDataSetInvalidated();
                } else {
                    Intent intent = new Intent(HistoryActivity.this,
                            SessionActivity.class);
                    intent.putExtra(SessionActivity.ARG_SESSION_ID, id);
                    startActivityForResult(intent, SESSION_REQUEST_CODE);
                }
            }
        });

        listView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(
                            final AdapterView<?> adapterView, final View view,
                            final int i, final long l) {
                        enableEditMode(i);
                        return true;
                    }
                });

        List<Session> sessions = Sessions.loadSessions(ignoreSessionId);
        if (sessions.isEmpty()) {
            listView.setVisibility(View.GONE);
            noSessionTextView.setVisibility(View.VISIBLE);
        }
        listAdapter = new HistoryListAdapter(this, sessions,
                highlightedSessionId);
        listView.setAdapter(listAdapter);

        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshList();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );
    }

    @Override
    public final boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_history, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public final boolean onPrepareOptionsMenu(final Menu menu) {
        boolean editMode = listAdapter.isEditMode();
        menu.findItem(R.id.action_download).setVisible(!editMode);
        menu.findItem(R.id.action_upload).setVisible(editMode);
        menu.findItem(R.id.action_delete).setVisible(editMode);
        menu.findItem(R.id.action_select_all).setVisible(editMode);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download:
                ConnectDialog connectDialogDownload =
                        ConnectDialog.newInstance(DOWNLOAD_OPERATION);
                connectDialogDownload.setCallback(this);
                connectDialogDownload.show(getSupportFragmentManager(),
                        ConnectDialog.FRAGMENT_TAG);
                return true;
            case R.id.action_upload:
                if (!Sessions.getSelectedSessions().isEmpty()) {
                    ConnectDialog connectDialogUpload =
                            ConnectDialog.newInstance(UPLOAD_OPERATION);
                    connectDialogUpload.setCallback(this);
                    connectDialogUpload.show(getSupportFragmentManager(),
                            ConnectDialog.FRAGMENT_TAG);
                }
                return true;
            case R.id.action_delete:
                if (!Sessions.getSelectedSessions().isEmpty()) {
                    deleteSessions();
                }
                return true;
            case R.id.action_select_all:
                List<Session> sessions = Sessions.loadSessions(
                        ignoreSessionId);
                for (Session session : sessions) {
                    session.setSelected(true);
                }
                listAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Check if the list is in edit mode.
     *
     * @return true if the list is in edit mode, false otherwise
     */
    private boolean isEditMode() {
        return listAdapter != null && listAdapter.isEditMode();
    }

    /**
     * Called when the Back button is pressed. If enabled, the edit mode must be
     * disable, otherwise the activity should be finished.
     */
    @SuppressLint("RestrictedApi")
    private void disableEditMode() {
        fab.setVisibility(View.VISIBLE);
        listAdapter.disableEditMode();
        listAdapter.notifyDataSetInvalidated();
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        invalidateOptionsMenu();
    }

    /**
     * Enable the edit mode.
     *
     * @param position Position of the item that has been pressed long
     */
    @SuppressLint("RestrictedApi")
    private void enableEditMode(final int position) {
        if (!listAdapter.isEditMode()) {
            fab.setVisibility(View.GONE);
            listAdapter.enableEditMode(position);
            listAdapter.notifyDataSetInvalidated();
            toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
            invalidateOptionsMenu();
        }
    }

    /**
     * Execute the task to delete the selected sessions.
     */
    private void deleteSessions() {
        Deletable[] sessions = Sessions.getSelectedSessions().toArray(
                new Deletable[0]);
        new Deleter(new Integer[]{SUBSCRIBER_ID},
                new Session()).execute(sessions);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Execute the task to restore the given sessions.
     *
     * @param sessions Sessions to restore
     */
    private void restoreSessions(final Restorable... sessions) {
        new Restorer(new Session()).execute(sessions);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Reload the list of sessions and notify the list adapter.
     */
    private void refreshList() {
        if (isEditMode()) {
            disableEditMode();
        }
        List<Session> sessions = Sessions.loadSessions(ignoreSessionId);
        if (sessions.isEmpty()) {
            listView.setVisibility(View.GONE);
            noSessionTextView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            noSessionTextView.setVisibility(View.GONE);
        }
        listAdapter.setSessions(sessions);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public final void onSubmit(final int operation,
                               final ConnectInfo connectInfo) {
        if (operation == DOWNLOAD_OPERATION) {
            new Downloader<>(
                    getApplicationContext(),
                    connectInfo,
                    new Intent(this, HistoryActivity.class),
                    SUBSCRIBER_ID,
                    Session.class,
                    Sessions.Json.FILE_REGEX);
        } else if (operation == UPLOAD_OPERATION) {
            try {
                new Uploader(
                        getApplicationContext(),
                        connectInfo,
                        new Intent(this, HistoryActivity.class),
                        Sessions.getSelectedSessions(),
                        SUBSCRIBER_ID);
            } catch (JSONException e) {
                ErrorDialog.show(this,
                        getString(R.string.upload_failure_title),
                        e.getMessage(), null);
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Triggered on download event.
     *
     * @param event Download event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDownloadEvent(final DownloadEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case DownloadEvent.DOWNLOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case DownloadEvent.OK:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case DownloadEvent.NO_FILE:
                Snackbar.make(fab, R.string.session_download_no_file_snackbar,
                        Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                break;
            case DownloadEvent.ERROR:
                ErrorDialog.show(this,
                        getString(R.string.download_failure_title),
                        event.getMessage(), null);
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on JSON file reader event.
     *
     * @param event JSON file reader event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onJsonFileReaderEvent(final JsonFileReaderEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case JsonFileReaderEvent.OK:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case JsonFileReaderEvent.ERROR:
                ErrorDialog.show(this,
                        getString(R.string.download_reading_failure_title),
                        event.getException().getMessage(), null);
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on inserted event.
     *
     * @param event inserted event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onInsertedEvent(final InsertedEvent event) {
        if (!(event.getInsertable() instanceof Session)) {
            return;
        }
        switch (event.getResult()) {
            case InsertedEvent.OK:
                refreshList();
                Snackbar.make(fab, R.string.session_download_success_snackbar,
                        Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                break;
            case InsertedEvent.ERROR:
                ErrorDialog.show(this,
                        getString(R.string.download_insert_failure_title),
                        event.getException().getMessage(), null);
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on JSON file writer event.
     *
     * @param event JSON file writer event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onJsonFileWriterEvent(final JsonFileWriterEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case JsonFileWriterEvent.OK:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case JsonFileWriterEvent.ERROR:
                ErrorDialog.show(this,
                        getString(R.string.upload_writing_failure_title),
                        event.getException().getMessage(), null);
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on upload event.
     *
     * @param event Upload event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onUploadEvent(final UploadEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        switch (event.getResult()) {
            case UploadEvent.UPLOADING:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case UploadEvent.OK:
                Snackbar.make(fab, R.string.history_upload_success_snackbar,
                        Snackbar.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                break;
            case UploadEvent.ERROR:
                ErrorDialog.show(this, getString(
                        R.string.upload_failure_title), event.getMessage(),
                        null);
                progressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on deleted event.
     *
     * @param event Deleted event
     */
    @SuppressWarnings("unchecked")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDeletedEvent(final DeletedEvent event) {
        if (!(event.getDeletable() instanceof Session)
                || !event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        progressBar.setVisibility(View.GONE);
        refreshList();

        // create snackbar
        Snackbar snackbar = Snackbar.make(fab, R.string.history_delete_snackbar,
                Snackbar.LENGTH_LONG);

        // if the deleted entities are restorable, show UNDO action
        if (Restorable.class.isAssignableFrom(
                event.getDeletable().getClass())) {
            // convert deletable entities to restorable entities
            final Restorable[] sessions = Deletables.toRestorables(
                    event.getDeletedEntities());
            snackbar.setAction(R.string.action_undo,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            // restore sessions
                            restoreSessions(sessions);
                        }
                    });
        }
        snackbar.show();
    }

    /**
     * Triggered on restored event.
     *
     * @param event Restored event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onRestoredEvent(final RestoredEvent event) {
        if (!(event.getRestorable() instanceof Session)) {
            return;
        }
        progressBar.setVisibility(View.GONE);
        refreshList();
    }

    @Override
    public final void onBackPressed() {
        if (isEditMode()) {
            disableEditMode();
        } else {
            finish();
        }
    }

    /**
     * Called when the Add session button is clicked.
     */
    @OnClick(R.id.history_fab_add)
    public final void onAddSessionClicked() {
        Intent intent = new Intent(this, AddSessionActivity.class);
        startActivity(intent);
    }
}
