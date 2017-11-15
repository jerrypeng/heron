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
package com.twitter.heron.examples.streamlet;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import com.twitter.heron.examples.streamlet.utils.StreamletUtils;
import com.twitter.heron.streamlet.Builder;
import com.twitter.heron.streamlet.JoinType;
import com.twitter.heron.streamlet.KeyValue;
import com.twitter.heron.streamlet.KeyedWindow;
import com.twitter.heron.streamlet.ReduceStreamlet;
import com.twitter.heron.streamlet.SerializableBiFunction;
import com.twitter.heron.streamlet.SerializableFunction;
import com.twitter.heron.streamlet.Streamlet;
import com.twitter.heron.streamlet.WindowConfig;

public class TestTopology {
  private static final List<String> SENTENCES = Arrays.asList(
      "I have nothing to declare but my genius",
      "You can even",
      "Compassion is an action word with no boundaries",
      "To thine own self be true"
  );

  public static void main(String[] args) throws Exception {
    Builder processingGraphBuilder = Builder.createBuilder();

    Streamlet<String> a = processingGraphBuilder
        .newSource(() -> StreamletUtils.randomFromList(SENTENCES));
    Streamlet<String> b = processingGraphBuilder
        .newSource(() -> StreamletUtils.randomFromList(SENTENCES));


    Streamlet<KeyValue<KeyedWindow<String>, Integer>> c =
        a.join(b).withJoinType(JoinType.INNER)
          .keyBy(s -> s, s -> s)
          .window(WindowConfig.TumblingTimeWindow(Duration.ofMillis(1000))).withTimestampExtractor(s -> null, s -> null).withLag(Duration.ofMillis(6))
          .apply((SerializableBiFunction<String, String, Integer>) (s, s2) -> (s + s2).length());


    Streamlet<KeyValue<KeyedWindow<String>, Integer>> m =
        a.reduce()
          .setIdentity(5)
          .keyBy((SerializableFunction<String, String>) s -> s)
          .window(WindowConfig.TumblingTimeWindow(Duration.ofMillis(1000))).withTimestampExtractor(s -> null).withLag(Duration.ofMillis(6))
          .apply((SerializableBiFunction<Integer, String, Integer>) (integer, s) -> s.length() + integer);

  }
}
