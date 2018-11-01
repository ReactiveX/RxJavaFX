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

public final class Change<T> {
    private final T oldVal;
    private final T newVal;

    public Change(T oldVal, T newVal) {
        this.oldVal = oldVal;
        this.newVal = newVal;
    }
    public T getOldVal() {
        return oldVal;
    }
    public T getNewVal() {
        return newVal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Change<?> change = (Change<?>) o;
        return Objects.equals(oldVal, change.oldVal) &&
                Objects.equals(newVal, change.newVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldVal, newVal);
    }

    @Override
    public String toString() {
        return "Change{" +
                "oldVal=" + oldVal +
                ", newVal=" + newVal +
                '}';
    }
}
