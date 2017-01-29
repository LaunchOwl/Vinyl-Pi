package io.vinylpi.app;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.vinylpi.app.DeviceListFragment.OnListFragmentInteractionListener;
//import io.vinylpi.app.dummy.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PiDevice} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class DeviceRecyclerViewAdapter extends RecyclerView.Adapter<DeviceRecyclerViewAdapter.ViewHolder> {

    private final List<PiDevice> mValues;
    private final OnListFragmentInteractionListener mListener;

    public DeviceRecyclerViewAdapter(List<PiDevice> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        int connections = mValues.get(position).getConnections();
        String deviceName = mValues.get(position).getDeviceName();
        holder.mConnectionsTextView.setText(String.valueOf(connections));
        holder.mDeviceNameTextView.setText(deviceName);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mConnectionsTextView;
        public final TextView mDeviceNameTextView;
        public PiDevice mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mConnectionsTextView = (TextView) view.findViewById(R.id.tv_device_count);
            mDeviceNameTextView = (TextView) view.findViewById(R.id.tv_device_name);
        }

        //@Override
        //public String toString() {
         //   return super.toString() + " '" + mContentView.getText() + "'";
        //}
    }
}
