package com.example.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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

final class SampleGridViewAdapter extends BaseAdapter implements Picasso.Listener {
  private final Context context;
  private final List<String> urls = new ArrayList<String>();
  private final DisplayMetrics metrics;
  private final Picasso mPicasso;

  public SampleGridViewAdapter(Context context) {
    this.context = context;

	mPicasso = new Picasso.Builder(context).listener(this).build();

    // Ensure we get a different ordering of images on each run.
    Collections.addAll(urls, Data.URLS);
    Collections.addAll(urls, Data.URLS);
    Collections.addAll(urls, Data.URLS);
    Collections.addAll(urls, Data.URLS);
    Collections.addAll(urls, Data.URLS);
    Collections.addAll(urls, Data.URLS);
    Collections.shuffle(urls);

	mPicasso.setUseBatch(false);
	mPicasso.setLoggingEnabled(false);

    metrics = context.getResources().getDisplayMetrics();
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
      view.setScaleType(CENTER_CROP);
    } else {
      view = (SquaredImageView) convertView;
    }

    view.setImageBitmap(null);

    // Get the image URL for the current position.
    String url = getItem(position);

	mPicasso.cancelRequest(view);

    // Trigger the download of the URL asynchronously into the image view.
	mPicasso //
        .load(url) //
        .placeholder(R.drawable.placeholder)
        .error(R.drawable.error) //
        .config(Bitmap.Config.RGB_565)
        .skipMemoryCache()
        .resize(metrics.widthPixels/3, metrics.widthPixels/3, true)
        .centerCrop()
        .into(
	        view, new Callback() {
		        @Override
		        public void onSuccess() {
		        }

		        @Override
		        public void onError() {
		        }
	        }
        );

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

	@Override
	public void onImageLoadFailed(
		final Picasso picasso, final Uri uri, final Exception exception) {
		Log.w("Picasso", "onImageLoadFailed: " + uri + ", exception: ", exception);
	}
}
