package com.alkisum.android.ownrun.data;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.utils.Format;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.UploadRemoteFileOperation;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Class uploading JSON file containing a session's data to the ownCloud server.
 *
 * @author Alkisum
 * @version 1.1
 * @since 1.0
 */
public class Uploader implements OnRemoteOperationListener,
        OnDatatransferProgressListener, JsonFileWriter.JsonFileWriterListener {

    /**
     * Log tag.
     */
    private static final String TAG = "Uploader";

    /**
     * JSON file name prefix.
     */
    public static final String FILE_PREFIX = "ownRun_";

    /**
     * JSON file extension.
     */
    public static final String FILE_EXT = ".json";

    /**
     * List of sessions to upload.
     */
    private List<Session> mSessions;

    /**
     * Context.
     */
    private final Context mContext;

    /**
     * Listener for the upload task.
     */
    private UploaderListener mCallback;

    /**
     * Path on the server where to upload the file.
     */
    private String mRemotePath;

    /**
     * OwnCloud client.
     */
    private OwnCloudClient mClient;

    /**
     * Handler for the operation on the ownCloud server.
     */
    private Handler mHandler;

    /**
     * Queue of wrappers containing sessions with their JSON files waiting
     * for being uploaded.
     */
    private Queue<JsonFileWriter.Wrapper> mWrappers;

    /**
     * Uploader constructor.
     *
     * @param context Context
     * @param sessions List of sessions to upload
     */
    public Uploader(final Context context, final List<Session> sessions) {
        mContext = context;
        try {
            mCallback = (UploaderListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement UploaderListener");
        }
        mSessions = sessions;
        mHandler = new Handler();
    }

    /**
     * Initialize the uploader with all the connection information.
     *
     * @param address  Server address
     * @param path     Remote path
     * @param username Username
     * @param password Password
     * @return Current instance of the uploader
     */
    public final Uploader init(final String address, final String path,
                               final String username, final String password) {
        mRemotePath = path;

        Uri serverUri = Uri.parse(address);
        mClient = OwnCloudClientFactory.createOwnCloudClient(
                serverUri, mContext, true);
        mClient.setCredentials(
                OwnCloudCredentialsFactory.newBasicCredentials(
                        username, password
                )
        );

        return this;
    }

    /**
     * Start the process. Execute the JsonFileWriter task to write sessions data
     * into temporary JSON files.
     */
    public final void start() {
        new JsonFileWriter(mContext, this, mSessions).execute();
    }

    @Override
    public final void onJsonFileWritten(
            final List<JsonFileWriter.Wrapper> wrappers) {
        mWrappers = new LinkedList<>(wrappers);
        upload(mWrappers.poll());
    }

    @Override
    public final void onJsonFileFailed(final Exception exception) {
        mCallback.onWritingFileFailed(exception);
    }

    /**
     * Upload the file to the ownCloud server.
     *
     * @param wrapper Wrapper containing the session and its JSON file to upload
     */
    private void upload(final JsonFileWriter.Wrapper wrapper) {
        mCallback.onUploadStart(wrapper);

        String remotePath = buildRemotePath(wrapper);
        String mimeType = "text/plain";

        UploadRemoteFileOperation op = new UploadRemoteFileOperation(
                wrapper.getFile().getAbsolutePath(),
                remotePath,
                mimeType);
        op.addDatatransferProgressListener(this);
        op.execute(mClient, this, mHandler);
    }

    /**
     * Build a valid remote path from the path given by the user.
     *
     * @param wrapper Wrapper containing the session and its JSON file
     *                information
     * @return Valid remote path
     */
    private String buildRemotePath(final JsonFileWriter.Wrapper wrapper) {
        if (mRemotePath == null || mRemotePath.equals("")) {
            mRemotePath = FileUtils.PATH_SEPARATOR;
        }
        if (!mRemotePath.startsWith(FileUtils.PATH_SEPARATOR)) {
            mRemotePath = FileUtils.PATH_SEPARATOR + mRemotePath;
        }
        if (!mRemotePath.endsWith(FileUtils.PATH_SEPARATOR)) {
            mRemotePath = mRemotePath + FileUtils.PATH_SEPARATOR;
        }
        // Add the file name to the remote path
        return mRemotePath + FILE_PREFIX + Format.DATE_TIME_JSON.format(
                new Date(wrapper.getSession().getStart())) + FILE_EXT;
    }

    @Override
    public final void onTransferProgress(final long progressRate,
                                         final long totalTransferredSoFar,
                                         final long totalToTransfer,
                                         final String fileName) {
        final int percentage;
        if (totalToTransfer > 0) {
            percentage = (int) (totalTransferredSoFar * 100 / totalToTransfer);
        } else {
            percentage = 0;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Uploading... (" + percentage + "%)");
                mCallback.onUploading(percentage);
            }
        });
    }

    @Override
    public final void onRemoteOperationFinish(
            final RemoteOperation operation,
            final RemoteOperationResult result) {
        if (result.isSuccess()) {
            JsonFileWriter.Wrapper wrapper = mWrappers.poll();
            if (wrapper != null) {
                upload(wrapper);
            } else {
                mCallback.onAllUploadComplete();
            }
        } else {
            Log.e(TAG, result.getLogMessage(), result.getException());
            mCallback.onUploadFailed(result.getLogMessage());
        }
    }

    /**
     * Listener to get notification from the Uploader tasks.
     */
    public interface UploaderListener {

        /**
         * Called when the JSON file could not have been written.
         *
         * @param e Exception thrown during the task
         */
        void onWritingFileFailed(Exception e);

        /**
         * Called when an upload operation starts.
         * @param wrapper Wrapper containing the session and its JSON file
         *                that is being uploaded
         */
        void onUploadStart(final JsonFileWriter.Wrapper wrapper);

        /**
         * Called when the file is being uploaded.
         *
         * @param percentage Progress of the upload (percentage)
         */
        void onUploading(int percentage);

        /**
         * Called when all upload operations are completed.
         */
        void onAllUploadComplete();

        /**
         * Called when the upload failed.
         *
         * @param message Message describing the cause of the failure
         */
        void onUploadFailed(String message);
    }
}
