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

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static it.sephiroth.android.library.picasso.Picasso.LoadedFrom.MEMORY;
import static it.sephiroth.android.library.picasso.Picasso.RequestTransformer.IDENTITY;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ImageViewActionTest {

  @Test(expected = AssertionError.class)
  public void throwsErrorWithNullResult() throws Exception {
    ImageViewAction action =
        new ImageViewAction(mock(Picasso.class), TestUtils.mockImageViewTarget(), null, false, 200, 0, null,
            TestUtils.URI_KEY_1, null, null);
    action.complete(null, MEMORY);
  }

  @Test
  public void returnsIfTargetIsNullOnComplete() throws Exception {
    Picasso picasso = mock(Picasso.class);
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, false, 200, 0, null, TestUtils.URI_KEY_1, null, callback);
    request.target.clear();
    request.complete(TestUtils.BITMAP_1, MEMORY);
    verifyZeroInteractions(target);
    verifyZeroInteractions(callback);
  }

  @Test
  public void returnsIfTargetIsNullOnError() throws Exception {
    Picasso picasso = mock(Picasso.class);
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, false, 200, 0, null, TestUtils.URI_KEY_1, null, callback);
    request.target.clear();
    request.error();
    verifyZeroInteractions(target);
    verifyZeroInteractions(callback);
  }

  @Test
  public void invokesTargetAndCallbackSuccessIfTargetIsNotNull() throws Exception {
    Picasso picasso =
        new Picasso(Robolectric.application, mock(Dispatcher.class), Cache.NONE, null, IDENTITY,
            null, mock(Stats.class), false, false);
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, false, 200, 0, null, TestUtils.URI_KEY_1, null, callback);
    request.complete(TestUtils.BITMAP_1, MEMORY);
    verify(target).setImageDrawable(any(PicassoDrawable.class));
    verify(callback).onSuccess();
  }

  @Test
  public void invokesTargetAndCallbackErrorIfTargetIsNotNullWithErrorResourceId() throws Exception {
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    Picasso mock = mock(Picasso.class);
    ImageViewAction request =
        new ImageViewAction(mock, target, null, false, 200, TestUtils.RESOURCE_ID_1, null, null, null,
            callback);
    request.error();
    verify(target).setImageResource(TestUtils.RESOURCE_ID_1);
    verify(callback).onError();
  }

  @Test
  public void invokesErrorIfTargetIsNotNullWithErrorResourceId() throws Exception {
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    Picasso mock = mock(Picasso.class);
    ImageViewAction request =
        new ImageViewAction(mock, target, null, false, 200, TestUtils.RESOURCE_ID_1, null, null, null,
            callback);
    request.error();
    verify(target).setImageResource(TestUtils.RESOURCE_ID_1);
    verify(callback).onError();
  }

  @Test
  public void invokesErrorIfTargetIsNotNullWithErrorDrawable() throws Exception {
    Drawable errorDrawable = mock(Drawable.class);
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    Picasso mock = mock(Picasso.class);
    ImageViewAction request =
        new ImageViewAction(mock, target, null, false, 200, 0, errorDrawable, TestUtils.URI_KEY_1, null,
            callback);
    request.error();
    verify(target).setImageDrawable(errorDrawable);
    verify(callback).onError();
  }

  @Test
  public void clearsCallbackOnCancel() throws Exception {
    Picasso picasso = mock(Picasso.class);
    ImageView target = TestUtils.mockImageViewTarget();
    Callback callback = TestUtils.mockCallback();
    ImageViewAction request =
        new ImageViewAction(picasso, target, null, false, 200, 0, null, TestUtils.URI_KEY_1, null, callback);
    request.cancel();
    assertThat(request.callback).isNull();
  }
}
