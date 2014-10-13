/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.sephiroth.android.library.picasso;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Cache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.squareup.picasso.Picasso.LoadedFrom.MEMORY;
import static com.squareup.picasso.Picasso.RequestTransformer.IDENTITY;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TargetActionTest {

  @Test(expected = AssertionError.class)
  public void throwsErrorWithNullResult() throws Exception {
    TargetAction request =
        new TargetAction(mock(Picasso.class), TestUtils.mockTarget(), null, false, 0, null, TestUtils.URI_KEY_1, null,
                200);
    request.complete(null, MEMORY);
  }

  @Test
  public void invokesSuccessIfTargetIsNotNull() throws Exception {
    Target target = TestUtils.mockTarget();
    TargetAction request =
        new TargetAction(mock(Picasso.class), target, null, false, 0, null, TestUtils.URI_KEY_1, null, 200);
    request.complete(TestUtils.BITMAP_3, MEMORY);
    verify(target).onBitmapLoaded(TestUtils.BITMAP_3, MEMORY);
  }

  @Test
  public void invokesOnBitmapFailedIfTargetIsNotNullWithErrorDrawable() throws Exception {
    Drawable errorDrawable = mock(Drawable.class);
    Target target = TestUtils.mockTarget();
    TargetAction request =
        new TargetAction(mock(Picasso.class), target, null, false, 0, errorDrawable, TestUtils.URI_KEY_1,
                null, 200);
    request.error();
    verify(target).onBitmapFailed(errorDrawable);
  }

  @Test
  public void invokesOnBitmapFailedIfTargetIsNotNullWithErrorResourceId() throws Exception {
    Drawable errorDrawable = mock(Drawable.class);
    Target target = TestUtils.mockTarget();
    Context context = mock(Context.class);
    Picasso picasso =
        new Picasso(context, mock(Dispatcher.class), Cache.NONE, null, IDENTITY, null,
            mock(Stats.class), false, false);
    Resources res = mock(Resources.class);
    TargetAction request =
        new TargetAction(picasso, target, null, false, TestUtils.RESOURCE_ID_1, null, TestUtils.URI_KEY_1, null, 200);

    when(context.getResources()).thenReturn(res);
    when(res.getDrawable(TestUtils.RESOURCE_ID_1)).thenReturn(errorDrawable);
    request.error();
    verify(target).onBitmapFailed(errorDrawable);
  }

  @Test public void recyclingInSuccessThrowsException() {
    Target bad = new Target() {
      @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        bitmap.recycle();
      }

      @Override public void onBitmapFailed(Drawable errorDrawable) {
        throw new AssertionError();
      }

      @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
        throw new AssertionError();
      }
    };
    Picasso picasso = mock(Picasso.class);

    TargetAction tr = new TargetAction(picasso, bad, null, false, 0, null, TestUtils.URI_KEY_1, null, 200);
    try {
      tr.complete(TestUtils.BITMAP_1, MEMORY);
      fail();
    } catch (IllegalStateException expected) {
    }
  }
}
