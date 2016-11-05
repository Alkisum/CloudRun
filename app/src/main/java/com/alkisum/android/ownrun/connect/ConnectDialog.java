package com.alkisum.android.ownrun.connect;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.alkisum.android.ownrun.R;
import com.alkisum.android.ownrun.utils.Pref;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Dialog to connect to ownCloud server to upload the session.
 *
 * @author Alkisum
 * @version 2.0
 * @since 1.0
 */
public class ConnectDialog extends DialogFragment {

    /**
     * Fragment tag for FragmentManager.
     */
    public static final String FRAGMENT_TAG = "connect_dialog";

    /**
     * Argument for operation type.
     */
    private static final String ARG_OPERATION = "arg_operation";

    /**
     * EditText for server address.
     */
    @BindView(R.id.address)
    EditText mAddressEditText;

    /**
     * EditText for remote path.
     */
    @BindView(R.id.path)
    EditText mPathEditText;

    /**
     * EditText for username.
     */
    @BindView(R.id.username)
    EditText mUsernameEditText;

    /**
     * EditText for password.
     */
    @BindView(R.id.password)
    EditText mPasswordEditText;

    /**
     * Listener for the dialog.
     */
    private ConnectDialogListener mCallback;

    /**
     * SharedPreferences to store the server address, the remote path and the
     * username.
     */
    private SharedPreferences mSharedPref;

    /**
     * Create new instance of ConnectDialog.
     *
     * @param operation Operation type
     * @return Instance of ConnectDialog
     */
    public static ConnectDialog newInstance(final int operation) {
        ConnectDialog connectDialog = new ConnectDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_OPERATION, operation);
        connectDialog.setArguments(args);
        return connectDialog;
    }

    @Override
    public final void onAttach(final Context context) {
        super.onAttach(context);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            mCallback = (ConnectDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement ConnectDialogListener");
        }
    }

    @NonNull
    @Override
    public final Dialog onCreateDialog(final Bundle savedInstanceState) {
        final int operation = getArguments().getInt(ARG_OPERATION);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getActivity(), R.layout.dialog_connect, null);
        ButterKnife.bind(this, view);

        mAddressEditText.setText(mSharedPref.getString(Pref.ADDRESS, ""));
        mPathEditText.setText(mSharedPref.getString(Pref.PATH, ""));
        mUsernameEditText.setText(mSharedPref.getString(Pref.USERNAME, ""));

        builder.setView(view)
                .setTitle(R.string.connect_title)
                .setPositiveButton(R.string.action_connect,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                mCallback.onSubmit(operation, new ConnectInfo(
                                        mAddressEditText.getText().toString(),
                                        mPathEditText.getText().toString(),
                                        mUsernameEditText.getText().toString(),
                                        mPasswordEditText.getText().toString()
                                ));

                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                ConnectDialog.this.getDialog().cancel();
                            }
                        });
        return builder.create();
    }

    /**
     * Listener for the dialog.
     */
    public interface ConnectDialogListener {

        /**
         * Called when the user submit the dialog.
         *
         * @param operation   Operation type
         * @param connectInfo Connection information entered in the dialog
         */
        void onSubmit(int operation, ConnectInfo connectInfo);
    }
}
