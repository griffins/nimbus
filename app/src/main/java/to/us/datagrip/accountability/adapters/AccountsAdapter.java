package to.us.datagrip.accountability.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.security.SecureRandom;
import java.util.ArrayList;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.models.DayAccount;
import to.us.datagrip.accountability.views.SelectListener;
import to.us.datagrip.textdrawable.TextDrawable;
import to.us.datagrip.textdrawable.util.ColorGenerator;

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.ViewHolder> {
    private static final String TAG = "Accounts";
    private ArrayList<DayAccount> mDataset;
    private boolean selectMode = false;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;
    private String todaysRandom;
    protected SelectListener SelectListener;

    public void clear() {
        mDataset.clear();
        notifyDataSetChanged();
    }

    public boolean isSelectMode() {
        return selectMode;
    }

    public SelectListener getSelectListener() {
        return SelectListener;
    }

    public void setSelectListener(SelectListener selectListener) {
        this.SelectListener = selectListener;
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
        for (DayAccount account : mDataset) {
            account.setSelected(false);
        }
        notifyDataSetChanged();
        if (SelectListener != null) {
            SelectListener.selected(selectMode);
        }
    }

    public void collapseItem(int position) {

    }

    public void expandItem(int position) {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView imageView;
        public TextView primaryText;
        public TextView smallText;
        public TextView smallText2;
        protected View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            imageView = (ImageView) view.findViewById(R.id.date);
            primaryText = (TextView) view.findViewById(R.id.primaryText);
            smallText = (TextView) view.findViewById(R.id.secondaryText);
            smallText2 = (TextView) view.findViewById(R.id.secondaryText2);
        }

        public View getView() {
            return view;
        }
    }

    public void add(DayAccount account) {
        mDataset.add(account);
        notifyDataSetChanged();
    }

    public AccountsAdapter(ArrayList<DayAccount> myDataset) {
        mDataset = myDataset;
        init();
    }

    public AccountsAdapter() {
        mDataset = new ArrayList<DayAccount>();
        init();
    }

    private void init() {
        mDrawableBuilder = TextDrawable.builder().round();
        byte[] bytes = new byte[19];
        new SecureRandom().nextBytes(bytes);
        todaysRandom = new String(bytes);
    }

    @Override
    public AccountsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final DayAccount current = mDataset.get(position);
        String img;
        if (!current.isSelected()) {
            img = String.valueOf(current.getWeekDay().charAt(0));
        } else {
            img = "\u2713";
        }
        Drawable drawable = mDrawableBuilder.build(img, mColorGenerator.getColor(img + todaysRandom));
        holder.imageView.setImageDrawable(drawable);
        holder.primaryText.setText(current.getDate());
        holder.smallText.setText(holder.getView().getContext().getString(R.string.income) + current.getTotalIncome());
        holder.smallText2.setText(holder.getView().getContext().getString(R.string.expenditure) + current.getTotalExpense());
        holder.getView().setLongClickable(true);
        holder.getView().setSelected(current.isSelected());

        holder.getView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!isSelectMode()) {
                    setSelectMode(true);
                    current.setSelected(true);
                    notifyDataSetChanged();
                    Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null) {
                        v.vibrate(50);
                    }
                    return true;
                }
                return false;
            }
        });

        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectMode) {
                    current.setSelected(!current.isSelected());
                    notifyDataSetChanged();
                    return;
                }
                if (current.getAction() != null) {
                    current.getAction().run(true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
