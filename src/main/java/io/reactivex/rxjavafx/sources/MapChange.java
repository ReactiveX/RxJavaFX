/**
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.rxjavafx.sources;

public final class MapChange<K,T> {
    private final K key;
    private final T value;
    private final Flag flag;

    MapChange(K key, T value, Flag flag) {
        this.key = key;
        this.value = value;
        this.flag = flag;
    }

    public K getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public Flag getFlag() {
        return flag;
    }
}
