package com.example.mwallpaper.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mwallpaper.Model.Categories_model;
import com.example.mwallpaper.R;
import com.example.mwallpaper.WallpaperListActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Categories_model> mList;

    public CategoryAdapter(Context mContext, ArrayList<Categories_model> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        View view = layoutInflater.inflate(R.layout.categories_list, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        TextView category_text = holder.category_text;
        ImageView category_image = holder.category_image;
        CardView categories_cardview = holder.categories_cardview;

        category_text.setText(mList.get(position).getCategory_text().toUpperCase());
        category_text.setAllCaps(true);
        Picasso.get().load(mList.get(position).getCategory_image()).into(category_image);

        categories_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, WallpaperListActivity.class);
                intent.putExtra("key", mList.get(position).getCategory_text());
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView category_image;
        TextView category_text;
        CardView categories_cardview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            category_image = itemView.findViewById(R.id.categories_image);
            category_text = itemView.findViewById(R.id.categories_name);
            categories_cardview = itemView.findViewById(R.id.categories_cardview);

        }
    }

}
