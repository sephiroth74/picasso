package it.sephiroth.android.library.picasso;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static it.sephiroth.android.library.picasso.TestUtils.makeBitmap;

final class Shadows {

  @Implements(MediaStore.Video.Thumbnails.class)
  public static class ShadowVideoThumbnails {

    @Implementation
    public static Bitmap getThumbnail(ContentResolver cr, long origId, int kind,
        BitmapFactory.Options options) {
      return TestUtils.makeBitmap();
    }
  }

  @Implements(MediaStore.Images.Thumbnails.class)
  public static class ShadowImageThumbnails {

    @Implementation
    public static Bitmap getThumbnail(ContentResolver cr, long origId, int kind,
        BitmapFactory.Options options) {
      return TestUtils.makeBitmap(20, 20);
    }
  }
}
