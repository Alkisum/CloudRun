package com.alkisum.android.ownrun.history;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
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
import com.alkisum.android.ownrun.data.JsonFileWriter;
import com.alkisum.android.ownrun.data.Uploader;
import com.alkisum.android.ownrun.dialog.ConfirmDialog;
import com.alkisum.android.ownrun.dialog.ErrorDialog;
import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.utils.Format;
import com.alkisum.android.ownrun.utils.Pref;

import java.util.Date;
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
     * Toolbar.
     */
    @BindView(R.id.history_toolbar)
    Toolbar mToolbar;

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
     * ID of the session that should be highlighted.
     */
    private Long mHighlightedSessionId;

    /**
     * ID of the session that should be ignored because it is still running. The
     * ID is null if no session is running.
     */
    private Long mIgnoreSessionId;

    /**
     * SharedPreferences for ownCloud information.
     */
    private SharedPreferences mSharedPref;

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

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        Sessions.fixSessions(mIgnoreSessionId);

        setGui();
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
                disableEditMode();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView,
                                    final View view, final int i,
                                    final long l) {
                if (mListAdapter.isEditMode()) {
                    mListAdapter.changeSessionSelectedState(i);
                    mListAdapter.notifyDataSetInvalidated();
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
        menu.findItem(R.id.action_upload).setVisible(editMode);
        menu.findItem(R.id.action_delete).setVisible(editMode);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                List<Session> selectedSessions = Sessions.getSelectedSessions();
                if (!selectedSessions.isEmpty()) {
                    DialogFragment connectDialog = new ConnectDialog();
                    connectDialog.show(getSupportFragmentManager(),
                            ConnectDialog.FRAGMENT_TAG);
                    mUploader = new Uploader(this, selectedSessions);
                }
                return true;
            case R.id.action_delete:
                if (!Sessions.getSelectedSessions().isEmpty()) {
                    showDeleteConfirmation();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when the Back button is pressed. If enabled, the edit mode must be
     * disable, otherwise the activity should be finished.
     */
    private void disableEditMode() {
        if (mListAdapter != null && mListAdapter.isEditMode()) {
            mListAdapter.disableEditMode();
            mListAdapter.notifyDataSetInvalidated();
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            invalidateOptionsMenu();
        } else {
            finish();
        }
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
                        Sessions.deleteSelectedSessions();
                        refreshList();
                    }
                }).show();
    }

    /**
     * Reload the list of sessions and notify the list adapter.
     */
    private void refreshList() {
        List<Session> sessions = Sessions.loadSessions(mIgnoreSessionId);
        if (sessions.isEmpty()) {
            mListView.setVisibility(View.GONE);
            mNoSessionTextView.setVisibility(View.VISIBLE);
        }
        mListAdapter.setSessions(sessions);
        mListAdapter.notifyDataSetChanged();
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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = new ProgressDialog(HistoryActivity.this);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(
                        ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMessage(getString(
                        R.string.upload_progress_init_msg));
                mProgressDialog.show();
            }
        });

        if (mSharedPref.getBoolean(Pref.SAVE_OWNCLOUD_INFO, false)) {
            saveConnectInfo(connectInfo);
        }
    }

    /**
     * Save the connection information (server address, remote path and
     * username) into the SharedPreferences to pre-fill the connect dialog.
     *
     * @param connectInfo Connection information
     */
    private void saveConnectInfo(final ConnectInfo connectInfo) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(Pref.ADDRESS, connectInfo.getAddress());
        editor.putString(Pref.PATH, connectInfo.getPath());
        editor.putString(Pref.USERNAME, connectInfo.getUsername());
        editor.apply();
    }

    @Override
    public final void onWritingFileFailed(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                ErrorDialog.build(HistoryActivity.this,
                        getString(R.string.upload_writing_failure_title),
                        e.getMessage(), null).show();
            }
        });
    }

    @Override
    public final void onUploadStart(final JsonFileWriter.Wrapper wrapper) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.setMessage("Uploading "
                            + Uploader.FILE_PREFIX
                            + Format.DATE_TIME_JSON.format(new Date(
                            wrapper.getSession().getStart()))
                            + Uploader.FILE_EXT
                            + " ...");
                    mProgressDialog.setIndeterminate(false);
                }
            }
        });
    }

    @Override
    public final void onUploading(final int percentage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.setProgress(percentage);
                }
            }
        });
    }

    @Override
    public final void onAllUploadComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                Toast.makeText(HistoryActivity.this, getString(R.string.
                        upload_success_toast), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public final void onUploadFailed(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }
                ErrorDialog.build(HistoryActivity.this, getString(
                        R.string.upload_failure_title), message, null).show();
            }
        });
    }

    @Override
    public final void onBackPressed() {
        disableEditMode();
    }
}
