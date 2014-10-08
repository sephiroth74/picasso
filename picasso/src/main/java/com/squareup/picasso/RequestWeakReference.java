package com.squareup.picasso;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
* Created by alessandro on 10/8/14.
*/
class RequestWeakReference<T> extends WeakReference<T> {
  final Action action;

  public RequestWeakReference(Action action, T referent, ReferenceQueue<? super T> q) {
    super(referent, q);
    this.action = action;
  }
}
