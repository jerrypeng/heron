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
package com.twitter.heron.streamlet.impl;

import java.time.Duration;

import com.twitter.heron.streamlet.KeyValue;
import com.twitter.heron.streamlet.KeyedWindow;
import com.twitter.heron.streamlet.ReduceStreamlet;
import com.twitter.heron.streamlet.SerializableBiFunction;
import com.twitter.heron.streamlet.SerializableFunction;
import com.twitter.heron.streamlet.Streamlet;
import com.twitter.heron.streamlet.WindowConfig;
import com.twitter.heron.streamlet.impl.streamlets.GeneralReduceByKeyAndWindowStreamlet;

public class ReduceStreamletImpl<R, T> implements ReduceStreamlet<R, T> {

  private StreamletImpl<R> parent;
  private T identity;

  public ReduceStreamletImpl(StreamletImpl<R> parent, T identity){
    this.parent = parent;
    this.identity = identity;
  }

  @Override
  public <K> KeyedReduceStreamletImpl<K, R, T> keyBy(SerializableFunction<R, K> keyExtractorFn) {
    return new KeyedReduceStreamletImpl<>(keyExtractorFn, this);
  }

  @Override
  public <T1> ReduceStreamlet<R, T1> setIdentity(T1 identity) {
    return new ReduceStreamletImpl<>(this.parent, identity);
  }

  public class KeyedReduceStreamletImpl<K, R, T> implements KeyedReduceStreamlet<K, R, T> {

    private ReduceStreamletImpl<R, T> reduceStreamlet;
    private SerializableFunction<R, K> keyExtractorFn;

    public KeyedReduceStreamletImpl(SerializableFunction<R, K> keyExtractorFn,  ReduceStreamletImpl<R, T> reduceStreamlet) {
      this.keyExtractorFn = keyExtractorFn;
      this.reduceStreamlet = reduceStreamlet;
    }

    @Override
    public WindowReduceStreamletImpl<K, R, T> window(WindowConfig windowConfig) {
      return new WindowReduceStreamletImpl<>(windowConfig, this.reduceStreamlet, this);
    }
  }

  public class WindowReduceStreamletImpl<K, R, T> implements WindowReduceStreamlet<K, R, T> {
    private WindowConfig windowConfig;
    private ReduceStreamletImpl<R, T> reduceStreamlet;
    private KeyedReduceStreamletImpl<K, R, T> keyedReduceStreamlet;
    private SerializableFunction<R, Long> timestampExtractorFn;
    private Duration lagDuration;

    public WindowReduceStreamletImpl(WindowConfig windowConfig, ReduceStreamletImpl<R, T> reduceStreamlet, KeyedReduceStreamletImpl<K, R, T> keyedReduceStreamlet) {
      this.windowConfig = windowConfig;
      this.reduceStreamlet = reduceStreamlet;
      this.keyedReduceStreamlet = keyedReduceStreamlet;
    }

    @Override
    public WindowReduceStreamletImpl<K, R, T> withTimestampExtractor(SerializableFunction<R, Long> timestampExtractorFn) {
      this.timestampExtractorFn = timestampExtractorFn;
      return this;
    }

    @Override
    public WindowReduceStreamletImpl<K, R, T> withLag(Duration lagDuration) {
      this.lagDuration = lagDuration;
      return this;
    }

    @Override
    public Streamlet<KeyValue<KeyedWindow<K>, T>> apply(SerializableBiFunction<T, R, ? extends T> reduceFn) {

      return new GeneralReduceByKeyAndWindowStreamlet<>(this.reduceStreamlet.parent, this.keyedReduceStreamlet.keyExtractorFn, this.windowConfig,
 this.reduceStreamlet.identity, reduceFn);
    }
  }
}
