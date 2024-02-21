package com.example.storymaker.adapters;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.storymaker.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import java.util.ArrayList;

public class Stickeradapter extends BaseAdapter {

    Context context;
    ArrayList<String> arrayList = new ArrayList();
    LayoutInflater inflater;

    class ViewHolder {

        ImageView imageview;
        ViewHolder() { }
    }

    public Stickeradapter(Context context, ArrayList<String> f) {
        this.context = context;
        arrayList = f;
        initImageLoader(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Stickeradapter(Context c) {
        context = c;
    }

    public int getCount() {
        return arrayList.size();
    }

    public String getItem(int position) {
        return (String) arrayList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View tmpView = convertView;

        if (tmpView == null) {
            ViewHolder holder = new ViewHolder();
            tmpView = inflater.inflate(R.layout.item_sticker, null);
            holder.imageview = (ImageView) tmpView.findViewById(R.id.grid_item);
            tmpView.setTag(holder);
        }
        ImageLoader.getInstance().displayImage("assets://crown/" + ((String) arrayList.get(position)), ((ViewHolder) tmpView.getTag()).imageview);
        return tmpView;
    }

    public static void initImageLoader(Context context) {

        DisplayImageOptions defaultOptions = new Builder().cacheInMemory(true).cacheOnDisc(true).showImageOnLoading(17301633).showImageForEmptyUri(17301543).showImageOnFail(17301624).considerExifParams(true).bitmapConfig(Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build();
        ImageLoader.getInstance().init(new ImageLoaderConfiguration.Builder(context).threadPriority(3).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).defaultDisplayImageOptions(null).build());
    }
}
