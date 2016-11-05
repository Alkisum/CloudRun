package com.alkisum.android.ownrun.data;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.alkisum.android.ownrun.model.Session;
import com.alkisum.android.ownrun.utils.Format;
import com.alkisum.android.ownrun.utils.Json;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Class downloading JSON file containing a session's data from the ownCloud
 * server.
 *
 * @author Alkisum
 * @version 2.0
 * @since 2.0
 */

public class Downloader implements OnRemoteOperationListener,
        OnDatatransferProgressListener, JsonFileReader.JsonFileReaderListener {

    /**
     * Log tag.
     */
    private static final String TAG = "Downloader";

    /**
     * Context.
     */
    private final Context mContext;

    /**
     * Listener for the download task.
     */
    private DownloaderListener mCallback;

    /**
     * Remote path to use to download the files.
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
     * Queue of remote files to download.
     */
    private Queue<RemoteFile> mRemoteFiles;

    /**
     * List of downloaded files to be parsed.
     */
    private List<File> mLocalFiles;

    /**
     * Downloader constructor.
     *
     * @param context Context
     */
    public Downloader(final Context context) {
        mContext = context;
        try {
            mCallback = (DownloaderListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement DownloaderListener");
        }
        mHandler = new Handler();
    }

    /**
     * Initialize the downloader with all the connection information.
     *
     * @param address  Server address
     * @param path     Remote path
     * @param username Username
     * @param password Password
     * @return Current instance of the downloader
     */
    public final Downloader init(final String address, final String path,
                                 final String username, final String password) {
        mRemotePath = buildRemotePath(path);

        Uri serverUri = Uri.parse(address);
        mClient = OwnCloudClientFactory.createOwnCloudClient(
                serverUri, mContext, true);
        mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(
                username, password));

        return this;
    }

    /**
     * Start the process. Get the remote files to download.
     */
    public final void start() {
        getRemoteFiles();
    }

    /**
     * List all remote files contained in the remote path directory.
     */
    private void getRemoteFiles() {
        ReadRemoteFolderOperation refreshOperation =
                new ReadRemoteFolderOperation(mRemotePath);
        refreshOperation.execute(mClient, this, mHandler);
    }

    /**
     * Download the given remote file.
     *
     * @param file Remote file
     */
    private void download(final RemoteFile file) {
        Log.d(TAG, "File to download: " + file.getRemotePath());
        mCallback.onDownloadStart(file);

        File localFile = new File(mContext.getCacheDir(), file.getRemotePath());
        mLocalFiles.add(localFile);

        Log.d(TAG, "Local file: " + localFile.getAbsolutePath());

        DownloadRemoteFileOperation downloadOperation =
                new DownloadRemoteFileOperation(file.getRemotePath(),
                        mContext.getCacheDir().getAbsolutePath());
        downloadOperation.addDatatransferProgressListener(this);
        downloadOperation.execute(mClient, this, mHandler);
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
                Log.i(TAG, "Downloading... (" + percentage + "%)");
                mCallback.onDownloading(percentage);
            }
        });
    }

    @Override
    public final void onRemoteOperationFinish(
            final RemoteOperation operation,
            final RemoteOperationResult result) {
        if (result.isSuccess()) {
            if (operation instanceof ReadRemoteFolderOperation) {
                onReadRemoteFolderFinish(result);
            } else if (operation instanceof DownloadRemoteFileOperation) {
                onDownloadRemoteFileFinish();
            }
        } else {
            Log.e(TAG, result.getLogMessage(), result.getException());
            mCallback.onDownloadFailed(result.getLogMessage());
        }
    }

    /**
     * Called when the read remote folder operation is finished.
     *
     * @param result Operation result
     */
    private void onReadRemoteFolderFinish(final RemoteOperationResult result) {
        mRemoteFiles = new LinkedList<>();
        mLocalFiles = new ArrayList<>();
        for (Object obj : result.getData()) {
            RemoteFile remoteFile = (RemoteFile) obj;
            if (isFileNameValid(remoteFile)
                    && !isSessionAlreadyInDb(remoteFile)) {
                mRemoteFiles.add(remoteFile);
            }
        }
        RemoteFile remoteFile = mRemoteFiles.poll();
        if (remoteFile != null) {
            download(remoteFile);
        } else {
            mCallback.onNoFileToDownload();
        }
    }

    /**
     * Called when the download remote file operation is finished.
     */
    private void onDownloadRemoteFileFinish() {
        RemoteFile remoteFile = mRemoteFiles.poll();
        if (remoteFile != null) {
            download(remoteFile);
        } else {
            if (mLocalFiles.isEmpty()) {
                mCallback.onNoFileToDownload();
            } else {
                mCallback.onAllDownloadComplete();
                new JsonFileReader(this, mLocalFiles).execute();
            }
        }
    }

    /**
     * Check if the file name is valid.
     *
     * @param file File to check
     * @return true if the file name is valid, false otherwise
     */
    private boolean isFileNameValid(final RemoteFile file) {
        String fileName = getRemoteFileName(file);
        return fileName.matches(Json.FILE_REGEX);
    }

    /**
     * Check if the session is already in the database.
     *
     * @param file File to check
     * @return true if the session is already in the database, false otherwise
     */
    private boolean isSessionAlreadyInDb(final RemoteFile file) {
        String fileName = getRemoteFileName(file);
        List<Session> sessions = Db.getInstance().getDaoSession()
                .getSessionDao().loadAll();
        for (Session session : sessions) {
            if (fileName.equals(Json.FILE_PREFIX + Format.DATE_TIME_JSON.format(
                    new Date(session.getStart())) + Json.FILE_EXT)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the file name from the remote file object.
     *
     * @param file Remote file
     * @return File name
     */
    private String getRemoteFileName(final RemoteFile file) {
        String[] splitPath = file.getRemotePath().split("/");
        return splitPath[splitPath.length - 1];
    }

    /**
     * Build a valid remote path from the path given by the user.
     *
     * @param path Path submit by user
     * @return Valid remote path
     */
    private static String buildRemotePath(final String path) {
        String remotePath = path;
        if (remotePath == null || remotePath.equals("")) {
            remotePath = FileUtils.PATH_SEPARATOR;
        }
        if (!remotePath.startsWith(FileUtils.PATH_SEPARATOR)) {
            remotePath = FileUtils.PATH_SEPARATOR + remotePath;
        }
        if (!remotePath.endsWith(FileUtils.PATH_SEPARATOR)) {
            remotePath = remotePath + FileUtils.PATH_SEPARATOR;
        }
        return remotePath;
    }

    @Override
    public final void onJsonFileRead() {
        mCallback.onSessionsInserted();
    }

    @Override
    public final void onReadJsonFileFailed(final Exception exception) {
        mCallback.onReadingFileFailed(exception);
    }

    /**
     * Listener to get notification from the Downloader tasks.
     */
    public interface DownloaderListener {

        /**
         * Called when a download operation starts.
         *
         * @param file Remote file being downloaded
         */
        void onDownloadStart(final RemoteFile file);

        /**
         * Called when there is no file to download.
         */
        void onNoFileToDownload();

        /**
         * Called when the file is being downloaded.
         *
         * @param percentage Progress of the download (percentage)
         */
        void onDownloading(int percentage);

        /**
         * Called when all download operations are completed.
         */
        void onAllDownloadComplete();

        /**
         * Called when the download failed.
         *
         * @param message Message describing the cause of the failure
         */
        void onDownloadFailed(String message);

        /**
         * Called when all the sessions and dataPoints are inserted into the
         * database.
         */
        void onSessionsInserted();

        /**
         * Called when the JSON file could not have been read.
         *
         * @param e Exception thrown during the task
         */
        void onReadingFileFailed(Exception e);
    }
}
