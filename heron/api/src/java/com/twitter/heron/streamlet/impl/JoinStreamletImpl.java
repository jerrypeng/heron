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

import com.twitter.heron.streamlet.JoinStreamlet;
import com.twitter.heron.streamlet.JoinType;
import com.twitter.heron.streamlet.KeyValue;
import com.twitter.heron.streamlet.KeyedWindow;
import com.twitter.heron.streamlet.SerializableBiFunction;
import com.twitter.heron.streamlet.SerializableFunction;
import com.twitter.heron.streamlet.WindowConfig;


public class JoinStreamletImpl<V1, V2> implements JoinStreamlet<V1, V2> {


  private StreamletImpl<V1> left;
  private StreamletImpl<V2> right;
  private JoinType joinType;


  public JoinStreamletImpl(StreamletImpl<V1> left, StreamletImpl<V2> right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public JoinStreamletImpl<V1, V2> withJoinType(JoinType joinType) {
    this.joinType = joinType;
    return this;
  }

  @Override
  public <K> KeyedJoinStreamletImpl<K, V1, V2> keyBy(SerializableFunction<V1, K> left, SerializableFunction<V2, K> right) {
    return new KeyedJoinStreamletImpl<>(left, right, this);
  }

  public class KeyedJoinStreamletImpl<K, V1, V2> implements KeyedJoinStreamlet<K, V1, V2> {

    private SerializableFunction<V1, K> leftKeyExtractor;
    private SerializableFunction<V2, K> rightKeyExtractor;
    JoinStreamletImpl<V1, V2> joinStreamlet;

    public KeyedJoinStreamletImpl(SerializableFunction<V1, K> left, SerializableFunction<V2, K> right, JoinStreamletImpl<V1, V2> joinStreamlet) {
      this.leftKeyExtractor = left;
      this.rightKeyExtractor = right;
      this.joinStreamlet = joinStreamlet;
    }

    @Override
    public WindowJoinStreamlet<K, V1, V2> window(WindowConfig windowConfig) {
      return new WindowJoinStreamletImpl<>(windowConfig, this.joinStreamlet, this);
    }
  }

  public class WindowJoinStreamletImpl<K, V1, V2> implements WindowJoinStreamlet<K, V1, V2> {

    WindowConfig windowConfig;
    SerializableFunction<KeyValue<K, V1>, Long> leftTimstampExtractor;
    SerializableFunction<KeyValue<K, V2>, Long> rightTimstampExtractor;
    Duration lag;
    private JoinStreamletImpl<V1, V2> joinStreamlet;
    private KeyedJoinStreamletImpl<K, V1, V2> keyedJoinStreamlet;


    public WindowJoinStreamletImpl(WindowConfig windowConfig, JoinStreamletImpl<V1, V2> joinStreamlet, KeyedJoinStreamletImpl<K, V1, V2> keyedJoinStreamlet) {
      this.windowConfig = windowConfig;
      this.joinStreamlet = joinStreamlet;
      this.keyedJoinStreamlet = keyedJoinStreamlet;
    }

    @Override
    public WindowJoinStreamletImpl<K, V1, V2> withTimestampExtractor(SerializableFunction<KeyValue<K, V1>, Long> left, SerializableFunction<KeyValue<K, V2>, Long> right) {
      this.leftTimstampExtractor = left;
      this.rightTimstampExtractor = right;
      return this;
    }

    @Override
    public WindowJoinStreamletImpl<K, V1, V2> withLag(Duration lagDuration) {
      this.lag = lagDuration;
      return this;
    }

    @Override
    public <T> StreamletImpl<KeyValue<KeyedWindow<K>, T>> apply(SerializableBiFunction<V1, V2, ? extends T> joinFunction) {
      com.twitter.heron.streamlet.impl.streamlets.JoinStreamlet<K, V1, V2, T> joinStreamlet =
            com.twitter.heron.streamlet.impl.streamlets.JoinStreamlet.createJoinStreamlet(
                this.joinStreamlet.left, this.joinStreamlet.right, this.keyedJoinStreamlet.leftKeyExtractor,
                this.keyedJoinStreamlet.rightKeyExtractor, windowConfig, this.joinStreamlet.joinType, joinFunction);
      left.addChild(joinStreamlet);
      right.addChild(joinStreamlet);

      return joinStreamlet;
    }
  }
}
