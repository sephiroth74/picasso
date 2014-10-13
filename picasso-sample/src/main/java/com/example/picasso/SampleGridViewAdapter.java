package com.example.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Cache;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.sephiroth.android.library.disklrumulticache.DiskLruMultiCache;
import it.sephiroth.android.library.disklrumulticache.DiskLruMultiCacheReadException;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

final class SampleGridViewAdapter extends BaseAdapter {
  private final Context context;
  private final List<String> urls = new ArrayList<String>();
  private Picasso mPicasso;
  private DiskCacheWrapper mDiskCache;

  public SampleGridViewAdapter(Context context) {
    this.context = context;

    try {
      DiskLruMultiCache cache = new DiskLruMultiCache(context, "name", 1024*1024*50);
      mDiskCache = new DiskCacheWrapper(cache);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Ensure we get a different ordering of images on each run.
    urls.add("custom://color/ff00ff00");
    urls.add("custom://color/ff00ffff");
    urls.add("custom://color/ff0000ff");
    Collections.addAll(urls, Data.URLS);
    Collections.shuffle(urls);

    // Triple up the list.
    ArrayList<String> copy = new ArrayList<String>(urls);
    urls.addAll(copy);
    urls.addAll(copy);

    mPicasso = Picasso.with(context);

    mPicasso.addRequestHandler(new RequestHandler() {
      @Override
      public boolean canHandleRequest(Request data) {
        String scheme = data.uri.getScheme();
        if (!TextUtils.isEmpty(scheme)) {
          return scheme.equals("custom");
        }
        return false;
      }

      @Override
      public Result load(Request data) throws IOException {
        Log.d("data", "path: " + data.uri.getLastPathSegment());
        int color = (int) Long.parseLong(data.uri.getLastPathSegment(), 16);
        Log.d("data", "color: " + color);
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(color);
        return new Result(bitmap, Picasso.LoadedFrom.DISK);
      }
    });

    mPicasso.setLoggingEnabled(false);
    mPicasso.setIndicatorsEnabled(true);
    mPicasso.setUseBatch(false);
  }

  @Override public View getView(final int position, View convertView, ViewGroup parent) {
    SquaredImageView view = (SquaredImageView) convertView;
    if (view == null) {
      view = new SquaredImageView(context);
      view.setScaleType(CENTER_CROP);
    }


    // Get the image URL for the current position.
    final String url = getItem(position);
    //Picasso.with(context).cancelTag(url);

    // Trigger the download of the URL asynchronously into the image view.
    final SquaredImageView finalView = view;

    mPicasso
        .load(url) //
        .placeholder(R.drawable.placeholder) //
        .error(R.drawable.error) //
        .fit()
        .tag(url) //
        .fade(200)
            .withDiskCache(mDiskCache)
        .into(view, new Callback() {
          @Override
          public void onSuccess() {
            Log.d("picasso", "success (" + position + " : " + url + "): " + finalView.getDrawable().getIntrinsicWidth() + "x"
                    + finalView.getDrawable().getIntrinsicHeight());
          }

          @Override
          public void onError() {
            Log.e("picasso", "error: " + url);
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

  static class DiskCacheWrapper implements Cache {

    final DiskLruMultiCache cache;

    public DiskCacheWrapper(DiskLruMultiCache cache){
      this.cache = cache;
    }

    @Override
    public Bitmap get(String key) {
      if (cache.isClosed()) return null;
      try {
        DiskLruMultiCache.Entry<DiskLruMultiCache.Metadata, EntryBitmap> result = cache.get(key, DiskLruMultiCache.Metadata.class, EntryBitmap.class);
        if(null != result){
          return result.getValue().bitmap;
        }
      } catch (DiskLruMultiCacheReadException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    public void set(String key, Bitmap bitmap) {
      if (cache.isClosed()) return;

      boolean contains;

      try {
        contains = cache.containsKey(key);
      } catch (IllegalStateException e) {
        e.printStackTrace();
        return;
      }

      if (! contains && null != bitmap && !bitmap.isRecycled()) {
        EntryBitmap entryBitmap = new EntryBitmap(bitmap, Bitmap.CompressFormat.JPEG, 90);
        DiskLruMultiCache.Entry<DiskLruMultiCache.Metadata, EntryBitmap> entry = new DiskLruMultiCache.Entry<DiskLruMultiCache.Metadata, EntryBitmap>(null, entryBitmap);
        try {
          cache.put(key, entry);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      Log.v("cache", "diskCache size: " + size() + " of " + maxSize() + " (" + (((double) size() / maxSize()) * 100) + "%)");
    }

    @Override
    public int size() {
      if (! cache.isClosed()) {
        return (int) cache.size();
      }
      return - 1;
    }

    @Override
    public int maxSize() {
      if (! cache.isClosed()) {
        return (int) cache.getMaxSize();
      }
      return - 1;
    }

    @Override
    public void clear() {
      if (! cache.isClosed()) {
        try {
          cache.delete();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }


  public static class EntryBitmap extends DiskLruMultiCache.EntryObject {
    Bitmap bitmap;
    Bitmap.CompressFormat format;
    int quality;

    public EntryBitmap() {}

    EntryBitmap(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
      this.bitmap = bitmap;
      this.format = format;
      this.quality = quality;
    }

    @Override
    public void read(final InputStream inputStream) throws IOException {
      bitmap = BitmapFactory.decodeStream(inputStream);
    }

    @Override
    public void write(final OutputStream outputStream) throws IOException {
      if (null == bitmap || bitmap.isRecycled()) throw new IOException("bitmap is null or already recycled");
      bitmap.compress(format, quality, outputStream);
    }
  }
}
