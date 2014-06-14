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

    //Picasso.with(context).setIndicatorsEnabled(true);
    Picasso.with(context).setUseBatch(false);

    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    Log.e("picasso", "density: " + metrics.density);
    Log.e("picasso", "screen inches: " + getScreenInches(context));
  }

  public static double getScreenInches( Context context ) {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    double w1 = ( (double) metrics.widthPixels ) / metrics.densityDpi;
    double h1 = ( (double) metrics.heightPixels ) / metrics.densityDpi;
    return Math.sqrt( Math.pow( w1, 2 ) + Math.pow( h1, 2 ) );
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    final SquaredImageView view;
    if (convertView == null) {
      view = new SquaredImageView(context);
      view.setScaleType(CENTER_CROP);
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
//        .placeholder(R.drawable.placeholder) //
//        .error(R.drawable.error) //
        .config(Bitmap.Config.RGB_565)
        .resize(300, 300)
        .centerInside()
        .into(view, new Callback() {
          @Override
          public void onSuccess() {
            Log.d("picasso", "success: " + view.getDrawable().getIntrinsicWidth() + "x" + view.getDrawable().getIntrinsicHeight() );
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
