package to.us.datagrip.accountability.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.models.Tag;
import to.us.datagrip.accountability.utils.AutoSpanGridLayoutManager;
import to.us.datagrip.accountability.utils.AutoSpannable;
import to.us.datagrip.textdrawable.TextDrawable;
import to.us.datagrip.textdrawable.util.ColorGenerator;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> implements AutoSpanGridLayoutManager.AutoSpanAdapter {
    private ArrayList<Tag> mDataset;

    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;

    @Override
    public List<? extends AutoSpannable> getItems() {
        return mDataset;
    }

    public List<String> getTags() {
        ArrayList tags = new ArrayList(getItemCount());
        for (Tag tag : mDataset) {
            tags.add(tag.getText());
        }
        return tags;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView imageView;
        public TextView primaryText;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.date);
            primaryText = (TextView) view.findViewById(R.id.primaryText);
        }
    }

    public void add(Tag tag) {
        mDataset.add(tag);
        notifyDataSetChanged();
    }

    public TagsAdapter(ArrayList<Tag> myDataset) {
        mDataset = myDataset;
        init();
    }

    public TagsAdapter() {
        mDataset = new ArrayList<Tag>();
        init();
    }

    private void init() {
    }

    @Override
    public TagsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Tag current = mDataset.get(position);
//        holder.imageView.setImageDrawable(drawable);
        holder.primaryText.setText(current.getText());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}