package com.example.storymaker.adapters;

import android.app.Activity;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.example.storymaker.R;
import com.example.storymaker.Activity.PreviewActivity;
import com.example.storymaker.fragments.MyStoriesFrag;
import com.example.storymaker.interfaces.ItemClickListener;
import com.example.storymaker.interfaces.ItemLongClickListener;
import com.example.storymaker.utils.DensityUtil;
import java.io.File;
import java.util.ArrayList;

public class RvStoryAdapter extends RecyclerView.Adapter<RvStoryAdapter.ViewHolderCollagePattern> {

    private ArrayList<String> checkedImages;
    private final ArrayList<String> imgPaths;
    private boolean isFirstClick;
    private boolean isLongClick;
    private final Activity mContext;
    private long mLastClickTime = System.currentTimeMillis();
    private final MyStoriesFrag myStoriesFrag;
    private final int screenWidth;

    static class ViewHolderCollagePattern extends RecyclerView.ViewHolder implements OnClickListener, OnLongClickListener {
        ItemClickListener itemClickListener;
        ItemLongClickListener itemLongClickListener;
        ImageView ivThumb;
        RelativeLayout rlChecked;
        View vLine;

        public ViewHolderCollagePattern(View view) {
            super(view);
            this.ivThumb = (ImageView) view.findViewById(R.id.iv_thumb);
            this.vLine = view.findViewById(R.id.v_line);
            this.rlChecked = (RelativeLayout) view.findViewById(R.id.rl_checked);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener2) {
            this.itemClickListener = itemClickListener2;
        }

        public void setItemLongClickListener(ItemLongClickListener itemLongClickListener2) {
            this.itemLongClickListener = itemLongClickListener2;
        }

        public void onClick(View view) {
            this.itemClickListener.onItemClick(view, getLayoutPosition());
        }

        public boolean onLongClick(View view) {
            this.itemLongClickListener.onItemLongClick(view, getLayoutPosition());
            return true;
        }
    }

    public RvStoryAdapter(Activity activity, ArrayList<String> arrayList, int i, MyStoriesFrag myStoriesFrag2) {
        this.mContext = activity;
        this.imgPaths = arrayList;
        this.screenWidth = i;
        this.myStoriesFrag = myStoriesFrag2;
        this.checkedImages = new ArrayList<>();
        this.isFirstClick = true;
        this.isLongClick = false;
    }

    public ViewHolderCollagePattern onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolderCollagePattern(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_drafts, viewGroup, false));
    }

    public void onBindViewHolder(ViewHolderCollagePattern viewHolderCollagePattern, int i) {
        int dp2px = (this.screenWidth / 2) - DensityUtil.dp2px(this.mContext, 12.0f);
        double d = (double) dp2px;
        Double.isNaN(d);
        int i2 = (int) (d * 1.7777777777777777d);
        viewHolderCollagePattern.ivThumb.getLayoutParams().width = dp2px;
        viewHolderCollagePattern.ivThumb.getLayoutParams().height = i2;
        viewHolderCollagePattern.rlChecked.getLayoutParams().width = dp2px;
        viewHolderCollagePattern.rlChecked.getLayoutParams().height = i2;
        viewHolderCollagePattern.vLine.setVisibility(View.GONE);
        Glide.with(this.mContext).load((String) this.imgPaths.get(i)).into(viewHolderCollagePattern.ivThumb);
        if (this.checkedImages.contains(this.imgPaths.get(i))) {
            viewHolderCollagePattern.rlChecked.setVisibility(View.VISIBLE);
        } else {
            viewHolderCollagePattern.rlChecked.setVisibility(View.GONE);
        }
        viewHolderCollagePattern.setItemClickListener(new ItemClickListener() {
            public void onItemClick(View view, int i) {
                long currentTimeMillis = System.currentTimeMillis();
                if (RvStoryAdapter.this.isLongClick || RvStoryAdapter.this.isFirstClick || currentTimeMillis - RvStoryAdapter.this.mLastClickTime >= 3000) {
                    RvStoryAdapter.this.mLastClickTime = currentTimeMillis;
                    RvStoryAdapter.this.isFirstClick = false;
                    if (RvStoryAdapter.this.myStoriesFrag.getWgCheckedList() != 0) {
                        File file = new File((String) RvStoryAdapter.this.imgPaths.get(i));
                        Intent intent = new Intent(RvStoryAdapter.this.mContext, PreviewActivity.class);
                        intent.putExtra("savedImageFile", file);
                        intent.putExtra("FromCreation", true);
                        RvStoryAdapter.this.mContext.startActivity(intent);
                        RvStoryAdapter.this.mContext.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                    } else if (!RvStoryAdapter.this.checkedImages.contains(RvStoryAdapter.this.imgPaths.get(i))) {
                        RvStoryAdapter.this.checkedImages.add(RvStoryAdapter.this.imgPaths.get(i));
                    } else {
                        RvStoryAdapter.this.checkedImages.remove(RvStoryAdapter.this.imgPaths.get(i));
                    }
                    RvStoryAdapter.this.notifyDataSetChanged();
                }
            }
        });
        viewHolderCollagePattern.setItemLongClickListener(new ItemLongClickListener() {
            public void onItemLongClick(View view, int i) {
                if (RvStoryAdapter.this.checkedImages.size() == 0) {
                    RvStoryAdapter.this.checkedImages.add(RvStoryAdapter.this.imgPaths.get(i));
                    RvStoryAdapter.this.myStoriesFrag.setWgCheckedList(true);
                    RvStoryAdapter.this.notifyDataSetChanged();
                    RvStoryAdapter.this.isLongClick = true;
                }
            }
        });
    }

    public int getItemCount() {
        ArrayList<String> arrayList = this.imgPaths;
        if (arrayList != null) {
            return arrayList.size();
        }
        return 0;
    }

    public ArrayList<String> getCheckedImages() {
        this.isLongClick = false;
        return this.checkedImages;
    }

    public void setCheckedImages(ArrayList<String> arrayList) {
        this.checkedImages = arrayList;
        notifyDataSetChanged();
        this.isLongClick = false;
    }
}
