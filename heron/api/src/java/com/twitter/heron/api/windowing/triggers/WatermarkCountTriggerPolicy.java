// Copyright 2017 Twitter. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.heron.api.windowing.triggers;

import java.io.Serializable;
import java.util.List;

import com.twitter.heron.api.windowing.DefaultEvictionContext;
import com.twitter.heron.api.windowing.Event;

/**
 * A trigger policy that tracks event counts and sets the context for
 * eviction policy to evict based on latest watermark time.
 *
 * @param <T> the type of event tracked by this policy.
 */
public class WatermarkCountTriggerPolicy<T extends Serializable> extends
        AbstractBaseTriggerPolicy<T, Long> {
  private final int count;
  private volatile long lastProcessedTs;

  public WatermarkCountTriggerPolicy(int count) {
    super();
    this.count = count;
  }

  @Override
  public void track(Event<T> event) {
    if (started && event.isWatermark()) {
      handleWaterMarkEvent(event);
    }
  }

  @Override
  public void reset() {
    // NOOP
  }

  @Override
  public void shutdown() {
    // NOOP
  }

  /**
   * Triggers all the pending windows up to the waterMarkEvent timestamp
   * based on the sliding interval count.
   *
   * @param waterMarkEvent the watermark event
   */
  private void handleWaterMarkEvent(Event<T> waterMarkEvent) {
    long watermarkTs = waterMarkEvent.getTimestamp();
    List<Long> eventTs = windowManager.getSlidingCountTimestamps(lastProcessedTs, watermarkTs,
        count);
    for (long ts : eventTs) {
      evictionPolicy.setContext(new DefaultEvictionContext(ts, null, Long.valueOf(count)));
      handler.onTrigger();
      lastProcessedTs = ts;
    }
  }

  @Override
  public Long getState() {
    return lastProcessedTs;
  }

  @Override
  public void restoreState(Long state) {
    lastProcessedTs = state;
  }

  @Override
  public String toString() {
    return "WatermarkCountTriggerPolicy{" + "count=" + count + ", lastProcessedTs="
        + lastProcessedTs + ", started=" + started + '}';
  }
}
