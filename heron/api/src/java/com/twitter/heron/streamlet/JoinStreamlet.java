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
package com.twitter.heron.streamlet;

import java.time.Duration;

public interface JoinStreamlet<V1, V2> {

  JoinStreamlet<V1, V2> withJoinType(JoinType joinType);

  <K> KeyedJoinStreamlet<K, V1, V2> keyBy(SerializableFunction<V1, K> left , SerializableFunction<V2, K> right);

  interface KeyedJoinStreamlet<K, V, V1> {
    WindowJoinStreamlet<K, V, V1> window(WindowConfig windowConfig);
  }

  interface WindowJoinStreamlet<K, V, V1> {

    WindowJoinStreamlet<K, V, V1>  withTimestampExtractor(
        SerializableFunction<KeyValue<K, V>, Long> left,
        SerializableFunction<KeyValue<K, V1>, Long> right);

    WindowJoinStreamlet<K, V, V1>  withLag(Duration lagDuration);

    <T> Streamlet<KeyValue<KeyedWindow<K>, T>> apply(SerializableBiFunction<V, V1, ? extends T> joinFunction);

  }
}
