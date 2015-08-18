package org.cryse.widget.persistentsearch.sample;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collection;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder> {
    private List<SearchResult> mItemList;

    public SearchResultAdapter(List<SearchResult> mItemList) {
        this.mItemList = mItemList;
    }

    @Override
    public SearchResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_search_result, parent, false);
        return new SearchResultViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SearchResultViewHolder holder, int position) {
        SearchResult result = mItemList.get(position);
        holder.mTitleTextView.setText(result.getTitle());
        holder.mDescriptionTextView.setText(result.getDescription());
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    public static class SearchResultViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitleTextView;
        private TextView mDescriptionTextView;
        private ImageView mIconImageView;
        public SearchResultViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = (TextView) itemView.findViewById(R.id.textview_title);
            mDescriptionTextView = (TextView) itemView.findViewById(R.id.textview_description);
            mIconImageView = (ImageView) itemView.findViewById(R.id.imageview_icon);

        }
    }



    public void addAll(Collection<SearchResult> items) {
        int currentItemCount = mItemList.size();
        mItemList.addAll(items);
        notifyItemRangeInserted(currentItemCount, items.size());
    }

    public void addAll(int position, Collection<SearchResult> items) {
        int currentItemCount = mItemList.size();
        if(position > currentItemCount)
            throw new IndexOutOfBoundsException();
        else
            mItemList.addAll(position, items);
        notifyItemRangeInserted(position, items.size());
    }

    public void replaceWith(Collection<SearchResult> items) {
        replaceWith(items, false);
    }

    public void clear() {
        int itemCount = mItemList.size();
        mItemList.clear();
        notifyItemRangeRemoved(0, itemCount);
    }


    public void replaceWith(Collection<SearchResult> items, boolean cleanToReplace) {
        if(cleanToReplace) {
            clear();
            addAll(items);
        } else {
            int oldCount = mItemList.size();
            int newCount = items.size();
            int delCount = oldCount - newCount;
            mItemList.clear();
            mItemList.addAll(items);
            if(delCount > 0) {
                notifyItemRangeChanged(0, newCount);
                notifyItemRangeRemoved(newCount, delCount);
            } else if(delCount < 0) {
                notifyItemRangeChanged(0, oldCount);
                notifyItemRangeInserted(oldCount, - delCount);
            } else {
                notifyItemRangeChanged(0, newCount);
            }
        }
    }
}
