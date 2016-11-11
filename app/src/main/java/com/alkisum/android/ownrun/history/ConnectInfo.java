package com.alkisum.android.ownrun.history;

/**
 * Class containing connection information for ownCloud server.
 *
 * @author Alkisum
 * @version 1.3
 * @since 1.0
 */
public class ConnectInfo {

    /**
     * Server address.
     */
    private final String mAddress;

    /**
     * Remote path.
     */
    private final String mPath;

    /**
     * Username.
     */
    private final String mUsername;

    /**
     * Password.
     */
    private final String mPassword;

    /**
     * ConnectInfo constructor.
     *
     * @param address  Server address
     * @param path     Remote path
     * @param username Username
     * @param password Password
     */
    public ConnectInfo(final String address, final String path,
                final String username, final String password) {
        mAddress = address;
        mPath = path;
        mUsername = username;
        mPassword = password;
    }

    /**
     * @return Server address
     */
    public final String getAddress() {
        return mAddress;
    }

    /**
     * @return Remote path
     */
    public final String getPath() {
        return mPath;
    }

    /**
     * @return Username
     */
    public final String getUsername() {
        return mUsername;
    }

    /**
     * @return Password
     */
    final String getPassword() {
        return mPassword;
    }
}
