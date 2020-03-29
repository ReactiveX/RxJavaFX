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
package io.reactivex.rxjavafx.subscriptions;

import io.reactivex.rxjava3.exceptions.Exceptions;
import javafx.beans.binding.Binding;

import java.util.*;


/**
 * A group of of Bindings that are disposed together.
 * <p>
 * All methods of this class must be called on JavaFX Thread
 */
public final class CompositeBinding {

    private Set<Binding> bindings;
    private Set<CompositeBinding> compBindings;
    private boolean disposedInd;

    public CompositeBinding() {}

    public CompositeBinding(final Binding... bindings) {
        this.bindings = new HashSet<>(Arrays.asList(bindings));
    }

    public void invalidate() {
        if (bindings != null) {
            bindings.forEach(Binding::invalidate);
        }
        if (compBindings != null) {
            compBindings.forEach(CompositeBinding::invalidate);
        }
    }
    public boolean isDisposed() {
        return disposedInd;
    }

    /**
     * Adds a new {@link CompositeBinding} to this {@code CompositeBinding} if the
     * {@code CompositeBinding} is not yet disposed. If the {@code CompositeBinding} <em>is</em>
     * disposed, {@code add} will indicate this by explicitly disposing the new {@code CompositeBinding} as
     * well.
     *
     * @param b the {@link Binding} to add
     */
    public void add(final CompositeBinding b) {
        if (!disposedInd) {
            if (compBindings == null) {
                compBindings = new HashSet<>(4);
            }
            compBindings.add(b);
            return;
        }
        b.dispose();
    }

    /**
     * Adds a new {@link Binding} to this {@code CompositeBinding} if the
     * {@code CompositeBinding} is not yet disposedInd. If the {@code CompositeBinding} <em>is</em>
     * disposed, {@code add} will indicate this by explicitly disposing the new {@code Binding} as
     * well.
     *
     * @param b the {@link Binding} to add
     */
    public void add(final Binding b) {
        if (!disposedInd) {
            if (bindings == null) {
                bindings = new HashSet<>(4);
            }
            bindings.add(b);
            return;
        }
        b.dispose();
    }

    /**
     * Removes a {@link CompositeBinding} from this {@code CompositeBinding}, and disposes the
     * {@link CompositeBinding}.
     *
     * @param b the {@link CompositeBinding} to remove
     */
    public void remove(final CompositeBinding b) {
        if (!disposedInd) {
            boolean unsubscribe = false;
            if (compBindings == null) {
                return;
            }
            unsubscribe = compBindings.remove(b);
            if (unsubscribe) {
                // if we removed successfully we then need to call dispose on it
                b.dispose();
            }
        }
    }

    /**
     * Removes a {@link Binding} from this {@code CompositeBinding}, and disposes the
     * {@link Binding}.
     *
     * @param b the {@link Binding} to remove
     */
    public void remove(final Binding b) {
        if (!disposedInd) {
            boolean unsubscribe = false;
            if (bindings == null) {
                return;
            }
            unsubscribe = bindings.remove(b);
            if (unsubscribe) {
                // if we removed successfully we then need to call dispose on it
                b.dispose();
            }
        }
    }


    /**
     * Disposes any bindings that are currently part of this {@code CompositeBinding} and remove
     * them from the {@code CompositeBinding} so that the {@code CompositeBinding} is empty and
     * able to manage new bindings.
     */
    public void clear() {
        if (!disposedInd) {
            Collection<Binding> unsubscribe1 = null;
            Collection<CompositeBinding> unsubscribe2 = null;
            if (bindings == null && compBindings == null) {
                return;
            }
            if (bindings != null) {
                unsubscribe1 = bindings;
                bindings = null;
                unsubscribeFromAll(unsubscribe1);
            }
            if (compBindings != null) {
                unsubscribe2 = compBindings;
                compBindings = null;
                unsubscribeFromAllComposite(unsubscribe2);
            }
        }
    }


    /**
     * Disposes itself and all inner Bindings.
     * <p>After call of this method, new {@code Binding}s added to {@link CompositeBinding}
     * will be disposed immediately.
     */
    public void dispose() {
        if (!disposedInd) {
            Collection<Binding> unsubscribe1 = null;
            Collection<CompositeBinding> unsubscribe2 = null;
            disposedInd = true;
            unsubscribe1 = bindings;
            unsubscribe2 = compBindings;
            bindings = null;
            compBindings = null;
            // we will only get here once
            unsubscribeFromAll(unsubscribe1);
            unsubscribeFromAllComposite(unsubscribe2);
        }
    }

    private static void unsubscribeFromAllComposite(Collection<CompositeBinding> bindings) {
        if (bindings == null) {
            return;
        }
        List<Throwable> es = null;
        for (CompositeBinding b : bindings) {
            try {
                b.dispose();
            } catch (Throwable e) {
                if (es == null) {
                    es = new ArrayList<>();
                }
                es.add(e);
            }
        }
        if (es != null)
            es.forEach(Exceptions::throwIfFatal);
    }
    private static void unsubscribeFromAll(Collection<Binding> bindings) {
        if (bindings == null) {
            return;
        }
        List<Throwable> es = null;
        for (Binding b : bindings) {
            try {
                b.dispose();
            } catch (Throwable e) {
                if (es == null) {
                    es = new ArrayList<>();
                }
                es.add(e);
            }
        }
        if (es != null)
            es.forEach(Exceptions::throwIfFatal);
    }

    /**
     * Returns true if this composite is not disposed and contains Bindings.
     *
     * @return {@code true} if this composite is not disposed and contains Bindings.
     * @since 1.0.7
     */
    public boolean hasSubscriptions() {
        return !disposedInd && ((bindings != null && !bindings.isEmpty()) || (compBindings != null && !compBindings.isEmpty()));
    }
}

