/**
 * Copyright 2017 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
    public int hashCode() {
        return 31 * Objects.hashCode(oldVal) + Objects.hashCode(newVal);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof Change<?> &&
                Objects.equals(oldVal, ((Change) obj).oldVal) &&
                Objects.equals(newVal, ((Change) obj).newVal);
    }
}
