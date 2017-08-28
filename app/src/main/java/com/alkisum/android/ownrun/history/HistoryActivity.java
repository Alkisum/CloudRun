package com.alkisum.android.ownrun.history;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alkisum.android.cloudops.events.DownloadEvent;
import com.alkisum.android.cloudops.events.JsonFileReaderEvent;
import com.alkisum.android.cloudops.events.JsonFileWriterEvent;
import com.alkisum.android.cloudops.events.UploadEvent;
import com.alkisum.android.cloudops.net.ConnectDialog;
import com.alkisum.android.cloudops.net.ConnectInfo;
import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.data.Deleter;
import com.alkisum.android.ownrun.data.Sessions;
import com.alkisum.android.ownrun.dialog.ConfirmDialog;
import com.alkisum.android.ownrun.dialog.ErrorDialog;
import com.alkisum.android.ownrun.event.DeleteEvent;
import com.alkisum.android.ownrun.event.InsertEvent;
import com.alkisum.android.ownrun.net.Downloader;
import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.net.Uploader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Activity listing the history of sessions.
 *
 * @author Alkisum
 * @version 3.0
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
    public static final int SESSION_DELETED = 627;

    /**
     * Result returned by AddSessionActivity when the session has been inserted
     * into the database.
     */
    public static final int SESSION_ADDED = 861;

    /**
     * Toolbar.
     */
    @BindView(R.id.history_toolbar)
    Toolbar mToolbar;

    /**
     * SwipeRefreshLayout for list view.
     */
    @BindView(R.id.history_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

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
     * Progress bar to show the progress of operations.
     */
    @BindView(R.id.history_progressbar)
    ProgressBar mProgressBar;

    /**
     * List adapter for the list of session.
     */
    private HistoryListAdapter mListAdapter;

    /**
     * ID of the session that should be highlighted.
     */
    private Long mHighlightedSessionId;

    /**
     * ID of the session that should be ignored because it is still running. The
     * ID is null if no session is running.
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

        Sessions.fixSessions(mIgnoreSessionId);

        setGui();

        // Register onCreate to receive events even when SessionActivity is open
        EventBus.getDefault().register(this);
    }

    @Override
    protected final void onActivityResult(final int requestCode,
                                          final int resultCode,
                                          final Intent data) {
        if (requestCode == SESSION_REQUEST_CODE) {
            if (resultCode == SESSION_DELETED) {
                refreshList();
            } else if (resultCode == SESSION_ADDED) {
                refreshList();
            }
        }
    }

    @Override
    public final void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Set the GUI.
     */
    private void setGui() {
        mToolbar.setTitle(R.string.history_title);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isEditMode()) {
                    disableEditMode();
                } else {
                    finish();
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView,
                                    final View view, final int position,
                                    final long id) {
                if (mListAdapter.isEditMode()) {
                    mListAdapter.changeSessionSelectedState(position);
                    mListAdapter.notifyDataSetInvalidated();
                } else {
                    Intent intent = new Intent(HistoryActivity.this,
                            SessionActivity.class);
                    intent.putExtra(SessionActivity.ARG_SESSION_ID, id);
                    startActivityForResult(intent, SESSION_REQUEST_CODE);
                }
            }
        });

        mListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(
                            final AdapterView<?> adapterView, final View view,
                            final int i, final long l) {
                        enableEditMode(i);
                        return true;
                    }
                });

        List<Session> sessions = Sessions.loadSessions(mIgnoreSessionId);
        if (sessions.isEmpty()) {
            mListView.setVisibility(View.GONE);
            mNoSessionTextView.setVisibility(View.VISIBLE);
        }
        mListAdapter = new HistoryListAdapter(this, sessions,
                mHighlightedSessionId);
        mListView.setAdapter(mListAdapter);

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshList();
                        mSwipeRefreshLayout.setRefreshing(false);
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
        boolean editMode = mListAdapter.isEditMode();
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
                    showDeleteConfirmation();
                }
                return true;
            case R.id.action_select_all:
                List<Session> sessions = Sessions.loadSessions(
                        mIgnoreSessionId);
                for (Session session : sessions) {
                    session.setSelected(true);
                }
                mListAdapter.notifyDataSetChanged();
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
        return mListAdapter != null && mListAdapter.isEditMode();
    }

    /**
     * Called when the Back button is pressed. If enabled, the edit mode must be
     * disable, otherwise the activity should be finished.
     */
    private void disableEditMode() {
        mListAdapter.disableEditMode();
        mListAdapter.notifyDataSetInvalidated();
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        invalidateOptionsMenu();
    }

    /**
     * Enable the edit mode.
     *
     * @param position Position of the item that has been pressed long
     */
    private void enableEditMode(final int position) {
        if (!mListAdapter.isEditMode()) {
            mListAdapter.enableEditMode(position);
            mListAdapter.notifyDataSetInvalidated();
            mToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
            invalidateOptionsMenu();
        }
    }

    /**
     * Show dialog to confirm the deletion of the selected sessions.
     */
    private void showDeleteConfirmation() {
        ConfirmDialog.build(this,
                getString(R.string.history_delete_title),
                getString(R.string.history_delete_msg),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface,
                                        final int i) {
                        deleteSelectedSessions();
                    }
                }).show();
    }

    /**
     * Execute the task to delete the selected sessions.
     */
    private void deleteSelectedSessions() {
        new Deleter(new Integer[]{SUBSCRIBER_ID}).execute();
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Reload the list of sessions and notify the list adapter.
     */
    private void refreshList() {
        if (isEditMode()) {
            disableEditMode();
        }
        List<Session> sessions = Sessions.loadSessions(mIgnoreSessionId);
        if (sessions.isEmpty()) {
            mListView.setVisibility(View.GONE);
            mNoSessionTextView.setVisibility(View.VISIBLE);
        } else {
            mListView.setVisibility(View.VISIBLE);
            mNoSessionTextView.setVisibility(View.GONE);
        }
        mListAdapter.setSessions(sessions);
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public final void onSubmit(final int operation,
                               final ConnectInfo connectInfo) {
        if (operation == DOWNLOAD_OPERATION) {
            new Downloader(getApplicationContext(), connectInfo,
                    new Intent(this, HistoryActivity.class), SUBSCRIBER_ID);
        } else if (operation == UPLOAD_OPERATION) {
            try {
                new Uploader(getApplicationContext(), connectInfo,
                        new Intent(this, HistoryActivity.class),
                        Sessions.getSelectedSessions(), SUBSCRIBER_ID);
            } catch (JSONException e) {
                ErrorDialog.build(this,
                        getString(R.string.upload_failure_title),
                        e.getMessage(), null).show();
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setIndeterminate(true);
                mProgressBar.setVisibility(View.VISIBLE);
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
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case DownloadEvent.OK:
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case DownloadEvent.NO_FILE:
                Toast.makeText(this, getString(R.string.download_no_file_toast),
                        Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.GONE);
                break;
            case DownloadEvent.ERROR:
                ErrorDialog.build(this,
                        getString(R.string.download_failure_title),
                        event.getMessage(), null).show();
                mProgressBar.setVisibility(View.GONE);
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
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case JsonFileReaderEvent.ERROR:
                ErrorDialog.build(this,
                        getString(R.string.download_reading_failure_title),
                        event.getException().getMessage(), null).show();
                mProgressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on inserter event.
     *
     * @param event Inserter event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onInsertEvent(final InsertEvent event) {
        switch (event.getResult()) {
            case InsertEvent.OK:
                refreshList();
                Toast.makeText(this, getString(R.string.
                        download_success_toast), Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.GONE);
                break;
            case InsertEvent.ERROR:
                ErrorDialog.build(this,
                        getString(R.string.download_insert_failure_title),
                        event.getException().getMessage(), null).show();
                mProgressBar.setVisibility(View.GONE);
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
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case JsonFileWriterEvent.ERROR:
                ErrorDialog.build(this,
                        getString(R.string.upload_writing_failure_title),
                        event.getException().getMessage(), null).show();
                mProgressBar.setVisibility(View.GONE);
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
                mProgressBar.setVisibility(View.VISIBLE);
                break;
            case UploadEvent.OK:
                Toast.makeText(this,
                        getString(R.string.history_upload_success_toast),
                        Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.GONE);
                break;
            case UploadEvent.ERROR:
                ErrorDialog.build(this, getString(
                        R.string.upload_failure_title), event.getMessage(),
                        null).show();
                mProgressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * Triggered on delete event.
     *
     * @param event Delete event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onDeleteEvent(final DeleteEvent event) {
        if (!event.isSubscriberAllowed(SUBSCRIBER_ID)) {
            return;
        }
        mProgressBar.setVisibility(View.GONE);
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
        startActivityForResult(intent, SESSION_REQUEST_CODE);
    }
}
