package com.example.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.sephiroth.android.library.picasso.Callback;
import it.sephiroth.android.library.picasso.Picasso;

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import static android.widget.ImageView.ScaleType.CENTER_INSIDE;

final class SampleGridViewAdapter extends BaseAdapter {
  private final Context context;
  private final List<String> urls = new ArrayList<String>();

  public SampleGridViewAdapter(Context context) {
    this.context = context;

    // Ensure we get a different ordering of images on each run.
    Collections.addAll(urls, Data.URLS);
    Collections.shuffle(urls);


    // Triple up the list.
    ArrayList<String> copy = new ArrayList<String>(urls);
    urls.addAll(copy);
    urls.addAll(copy);
    urls.add(3, "http://lh3.googleusercontent.com/-AIS_50f8nC4/UlBc_sxqG4I/AAAAAAACJ08/79AXSoDfYnY/w1208-h1812-no/DSC_0535.jpg");

    //Picasso.with(context).setIndicatorsEnabled(true);
    Picasso.with(context).setUseBatch(false);
    Picasso.with(context).setLoggingEnabled(true);

    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    Log.e("picasso", "density: " + metrics.density);
    Log.e("picasso", "screen inches: " + getScreenInches(context));
    Log.e("picasso", "screen pixels: " + metrics.widthPixels + "x" + metrics.heightPixels);
  }

  public static double getScreenInches( Context context ) {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    double w1 = ( (double) metrics.widthPixels ) / metrics.densityDpi;
    double h1 = ( (double) metrics.heightPixels ) / metrics.densityDpi;
    return Math.sqrt( Math.pow( w1, 2 ) + Math.pow( h1, 2 ) );
  }

  @Override public View getView(final int position, View convertView, ViewGroup parent) {
    final SquaredImageView view;
    if (convertView == null) {
      view = new SquaredImageView(context);
      view.setScaleType(CENTER_INSIDE);
    } else {
      view = (SquaredImageView) convertView;
    }

    view.setImageBitmap(null);

    // Get the image URL for the current position.
    String url = getItem(position);

    Picasso.with(context).cancelRequest(view);


    // Trigger the download of the URL asynchronously into the image view.
    Picasso.with(context) //
        .load(url) //
//        .placeholder(R.drawable.placeholder)
        .error(R.drawable.error) //
        .config(Bitmap.Config.RGB_565)
        .skipMemoryCache()
        .resize(300,300,true)
        .centerInside()
        .into(view, new Callback() {
          @Override
          public void onSuccess() {
            Log.d("picasso",
                "bitmap(" + position + "): " + view.getDrawable().getIntrinsicWidth() + "x" + view.getDrawable().getIntrinsicHeight() +
                    ", image: " + view.getMeasuredWidth() + "x" + view.getMeasuredHeight()
            );
          }

          @Override
          public void onError() {
//            Log.e("adapter", "onError");
          }
        });

    return view;
  }

  @Override public int getCount() {
    return urls.size();
  }

  @Override public String getItem(int position) {
    return urls.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }
}
