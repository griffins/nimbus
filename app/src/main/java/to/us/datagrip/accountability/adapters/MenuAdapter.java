package to.us.datagrip.accountability.adapters;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import to.us.datagrip.accountability.R;
import to.us.datagrip.accountability.models.MenuItem;


public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {

    private ArrayList<MenuItem> mDataset;

    public MenuAdapter() {
        mDataset = new ArrayList<>();
    }

    public MenuAdapter(ArrayList<MenuItem> dataset) {
        mDataset = dataset;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 0:
                convertView = inflater.inflate(R.layout.group_item_title, parent, false);
                break;
            case 1:
                convertView = inflater.inflate(R.layout.single_item, parent, false);
                break;
            case 2:
                convertView = inflater.inflate(R.layout.double_item, parent, false);
                break;
            case 5:
                convertView = inflater.inflate(R.layout.menu_item, parent, false);
                break;
            case 6:
                convertView = inflater.inflate(R.layout.check_item, parent, false);
                break;
            case 7:
                convertView = inflater.inflate(R.layout.desc_item, parent, false);
                break;
            case 3:
                convertView = inflater.inflate(R.layout.thin_divider, parent, false);
                break;
            default:
                convertView = inflater.inflate(R.layout.thick_divider, parent, false);
                break;
        }
        return new ViewHolder(convertView, viewType);
    }

    public void add(MenuItem account) {
        mDataset.add(account);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position_) {
        final int position = position_;
        switch (getItemViewType(position)) {
            case 0:
            case 7:
                holder.primaryText.setText(getItem(position).getPrimaryText());
                break;
            case 5:
                holder.primaryText.setText(getItem(position).getPrimaryText());
                holder.primaryImage.setImageResource(getItem(position).getIcon());
                break;
            case 6:
                holder.primaryText.setText(getItem(position).getPrimaryText());
                if (getItem(position).getValue() != null) {
                    holder.primaryRadio.setChecked((Boolean) getItem(position).getValue());
                } else {
                    holder.primaryRadio.setChecked(false);
                }
                holder.primaryRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        getItem(position).setValue(b);
                        getItem(position).getAction().run(getItem(position));
                    }
                });
                break;
            case 1:
            case 2:
                holder.primaryText.setText(getItem(position).getPrimaryText());
                holder.secondaryText.setText(getItem(position).getSecondaryText());
                break;
        }
        if (getItem(position).getAction() != null && getItemViewType(position) != 6) {
            holder.getView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getItem(position).getAction().run(getItem(position));
                }
            });
        }

        holder.getView().setEnabled(getItem(position).isEnabled());

    }

    private MenuItem getItem(int position) {
        return mDataset.get(position);
    }


    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView primaryText;
        TextView secondaryText;
        CheckBox primaryRadio;
        ImageView primaryImage;
        protected View view;

        ViewHolder(View view, int type) {
            super(view);
            this.view = view;
            primaryText = (TextView) view.findViewById(R.id.primaryText);
            if (type != MenuItem.TYPES.SECTION_CHECK) {
                secondaryText = (TextView) view.findViewById(R.id.secondaryText);
            } else {
                primaryRadio = (CheckBox) view.findViewById(R.id.primaryCheck);
            }
            if (type == MenuItem.TYPES.SECTION_MENU) {
                primaryImage = (ImageView) view.findViewById(R.id.primaryImage);
            }
        }

        public View getView() {
            return view;
        }
    }
}