//  Copyright 2017 Twitter. All rights reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
package org.apache.storm.metric.internal;

import java.util.Map;

import org.apache.storm.metric.api.IMetric;

public class LatencyStatAndMetric implements IMetric {

  private final com.twitter.heron.api.metric.LatencyStatAndMetric delegate;

  public LatencyStatAndMetric(int numBuckets) {
    delegate = new com.twitter.heron.api.metric.LatencyStatAndMetric(numBuckets);
  }

  @Override
  public Object getValueAndReset() {
    return delegate.getValueAndReset();
  }

  public void record(long latency) {
    delegate.record(latency);
  }

  public Map<String, Double> getTimeLatAvg() {
    return delegate.getTimeLatAvg();
  }

  public void close() {
    delegate.close();
  }
}
