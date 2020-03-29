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

package io.reactivex.rxjavafx.transformers;

import io.reactivex.rxjava3.core.ObservableOperator;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.ResourceObserver;
import javafx.application.Platform;


public final class FxObservableTransformers {
    private FxObservableTransformers() {}

    private static <T> void runOnFx(T t, Consumer<T> consumer)  {
        Platform.runLater(() -> {
                    try {
                        consumer.accept(t);
                    } catch (Throwable e) {
                        throw Exceptions.propagate(e);
                    }
                }
        );
    }
    private static <T> void runOnFx(Action action)  {
        Platform.runLater(() -> {
                    try {
                        action.run();
                    } catch (Throwable e) {
                        throw Exceptions.propagate(e);
                    }
                }
        );
    }

    /**
     * Performs a given action for each item on the FX thread
     * @param onNext
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnNextFx(Consumer<T> onNext) {
        return obs -> obs.doOnNext(t -> runOnFx(t, onNext));
    }

    /**
     * Performs a given action on a Throwable on the FX thread in the event of an onError
     * @param onError
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnErrorFx(Consumer<Throwable> onError) {
        return obs -> obs.doOnError(e -> runOnFx(e,onError));
    }

    /**
     * Performs a given Action on the FX thread when onCompleted is called
     * @param onCompleted
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnCompleteFx(Action onCompleted) {
        return obs -> obs.doOnComplete(() -> runOnFx(onCompleted));
    }

    /**
     * Performs a given Action on the FX thread when subscribed to
     * @param subscribe
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnSubscribeFx(Consumer<Disposable> subscribe) {
        return obs -> obs.doOnSubscribe((d -> runOnFx(d,subscribe)));
    }

    /**
     * Performs the provided onTerminate action on the FX thread
     * @param onTerminate
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnTerminateFx(Action onTerminate) {
        return obs -> obs.doOnTerminate(() -> runOnFx(onTerminate));
    }

    /**
     * Performs the provided onTerminate action on the FX thread
     * @param onDipsose
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnDisposeFx(Action onDipsose) {
        return obs -> obs.doOnDispose(() -> runOnFx(onDipsose));
    }

    /**
     * Performs an action on onNext with the provided emission count
     * @param onNext
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnNextCount(Consumer<Integer> onNext) {
        return obs -> obs.lift(new OperatorEmissionCounter<>(new CountObserver(onNext,null,null)));
    }

    /**
     * Performs an action on onComplete with the provided emission count
     * @param onComplete
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnCompleteCount(Consumer<Integer> onComplete) {
        return obs -> obs.lift(new OperatorEmissionCounter<>(new CountObserver(null,onComplete,null)));
    }

    /**
     * Performs an action on onError with the provided emission count
     * @param onError
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnErrorCount(Consumer<Integer> onError) {
        return obs -> obs.lift(new OperatorEmissionCounter<>(new CountObserver(null,null,onError)));
    }

    /**
     * Performs an action on FX thread on onNext with the provided emission count
     * @param onNext
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnNextCountFx(Consumer<Integer> onNext) {
        return obs -> obs.compose(doOnNextCount(i -> runOnFx(i,onNext)));
    }

    /**
     * Performs an action on FX thread on onCompleted with the provided emission count
     * @param onComplete
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnCompleteCountFx(Consumer<Integer> onComplete) {
        return obs -> obs.compose(doOnCompleteCount(i -> runOnFx(i,onComplete)));
    }

    /**
     * Performs an action on FX thread on onError with the provided emission count
     * @param onError
     * @param <T>
     */
    public static <T> ObservableTransformer<T,T> doOnErrorCountFx(Consumer<Integer> onError) {
        return obs -> obs.compose(doOnErrorCount(i -> runOnFx(i,onError)));
    }


    private static class OperatorEmissionCounter<T> implements ObservableOperator<T,T> {

        private final CountObserver ctObserver;

        OperatorEmissionCounter(CountObserver ctObserver) {
            this.ctObserver = ctObserver;
        }

        @Override
        public Observer<? super T> apply(Observer<? super T> child) {

            return new ResourceObserver<T>() {
                private int count = 0;
                private boolean done = false;

                @Override
                public void onComplete() {
                    if (done)
                        return;

                    try {
                        if (ctObserver.doOnCompletedCountAction != null)
                            ctObserver.doOnCompletedCountAction.accept(count);
                    } catch (Throwable e) {
                        Exceptions.throwIfFatal(e);
                        onError(e);
                        return;
                    }
                    done = true;
                    child.onComplete();
                }

                @Override
                public void onError(Throwable e) {
                    if (done)
                        return;
                    try {
                        if (ctObserver.doOnErrorCountAction != null)
                            ctObserver.doOnErrorCountAction.accept(count);
                    } catch(Throwable e1) {
                        Exceptions.throwIfFatal(e1);
                        child.onError(e1);
                    }
                }

                @Override
                public void onNext(T t) {
                    if (done)
                        return;
                    try {
                        ++count;
                        if (ctObserver.doOnNextCountAction != null)
                            ctObserver.doOnNextCountAction.accept(count);
                    } catch(Throwable e) {
                        Exceptions.throwIfFatal(e);
                        onError(e);
                        return;
                    }
                    child.onNext(t);
                }
            };
        }
    }
    private static final class CountObserver {
        private final Consumer<Integer> doOnNextCountAction;
        private final Consumer<Integer> doOnCompletedCountAction;
        private final Consumer<Integer> doOnErrorCountAction;

        CountObserver(Consumer<Integer> doOnNextCountAction, Consumer<Integer> doOnCompletedCountAction, Consumer<Integer> doOnErrorCountAction) {
            this.doOnNextCountAction = doOnNextCountAction;
            this.doOnCompletedCountAction = doOnCompletedCountAction;
            this.doOnErrorCountAction = doOnErrorCountAction;
        }
    }
}
