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

import java.util.Objects;

/**
 * Holds an ADDED, REMOVED, or UPDATED flag with the associated value
 * @param <T>
 */
public final class ListChange<T> {
    private final T value;
    private final Flag flag;
    private final int index;

    private ListChange(T value, Flag flag, int index) {
        this.value = value;
        this.flag = flag;
        this.index = index;
    }
    public static <T> ListChange<T> of(T value, Flag flag, int index) {
        return new ListChange<>(value, flag, index);
    }
    public T getValue() {
        return value;
    }
    public Flag getFlag() {
        return flag;
    }
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return flag + " " + value + " " + index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListChange<?> that = (ListChange<?>) o;
        return index == that.index &&
                value.equals(that.value) &&
                flag == that.flag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, flag, index);
    }
}
