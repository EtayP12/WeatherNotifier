package com.example.etayp.weathernotifier;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.example.etayp.weathernotifier.LocationsFragment.OnListFragmentInteractionListener;
import com.example.etayp.weathernotifier.dummy.RecyclerItems.RecyclerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link RecyclerItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class LocationsRecyclerViewAdapter extends RecyclerView.Adapter<LocationsRecyclerViewAdapter.ViewHolder> {

    private final List<RecyclerItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final ArrayList<ViewHolder> viewHolders = new ArrayList<>();

    public LocationsRecyclerViewAdapter(List<RecyclerItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        Animation animation = new AlphaAnimation(0,1);
        animation.setDuration(1000);
        view.startAnimation(animation);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolders.add(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).content);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final Button mButton;
        public RecyclerItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            mButton = (Button) view.findViewById(R.id.recycler_button);
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAt(getAdapterPosition());
                    if (null != mListener) mListener.onListFragmentInteraction(mItem);
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        private void removeAt(int position) {
            mValues.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mValues.size());
            for (int i = mValues.size(); i > position; i--) {
                mValues.get(i-1).id = String.valueOf(i);
                viewHolders.get(i-1).mIdView.setText(String.valueOf(i));
            }
        }

    }
}
