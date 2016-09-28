package com.alkisum.android.ownrun.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alkisum.android.ownrun.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for list of application information shown in the AboutActivity.
 *
 * @author Alkisum
 * @version 1.3
 * @since 1.2
 */
class AboutListAdapter extends ArrayAdapter<String[]> {

    /**
     * Context.
     */
    private final Context mContext;

    /**
     * Array containing the app information.
     */
    private final String[][] mInfo;

    /**
     * AboutListAdapter constructor.
     *
     * @param context Context
     * @param info    Array containing the app information
     */
    AboutListAdapter(final Context context, final String[][] info) {
        super(context, R.layout.list_item_about, info);
        mContext = context;
        mInfo = info;
    }

    @NonNull
    @Override
    public final View getView(final int position, final View convertView,
                              @NonNull final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = convertView;
        if (view == null || view.getTag() == null) {
            view = inflater.inflate(
                    R.layout.list_item_about, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            view.setTag(holder);
        }
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.title.setText(mInfo[position][0]);
        holder.summary.setText(mInfo[position][1]);
        if (AboutActivity.GITHUB_ADDRESS.equals(mInfo[position][1])) {
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(AboutActivity.GITHUB_ADDRESS));
                    mContext.startActivity(intent);
                }
            });
        }
        return view;
    }

    /**
     * ViewHolder for app information list adapter.
     */
    static class ViewHolder {

        /**
         * Item layout.
         */
        @BindView(R.id.about_list_layout)
        LinearLayout layout;

        /**
         * TextView for title.
         */
        @BindView(R.id.about_list_title)
        TextView title;

        /**
         * TextView for summary.
         */
        @BindView(R.id.about_list_summary)
        TextView summary;

        /**
         * ViewHolder constructor.
         *
         * @param view View to bind with ButterKnife
         */
        ViewHolder(final View view) {
            ButterKnife.bind(this, view);
        }
    }
}
