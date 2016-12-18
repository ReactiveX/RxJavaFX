package rx.transformers;

import javafx.application.Platform;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Action1;

public final class JavaFxTransformers {
    private JavaFxTransformers() {}

    /**
     * Performs a given action for each item on the FX thread
     * @param onNext
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnNextFx(Action1<T> onNext) {
        return obs -> obs.doOnNext(t -> Platform.runLater(() -> onNext.call(t)));
    }

    /**
     * Performs a given action on a Throwable on the FX thread in the event of an onError
     * @param onError
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnErrorFx(Action1<Throwable> onError) {
        return obs -> obs.doOnError(e -> Platform.runLater(() -> onError.call(e)));
    }

    /**
     * Performs a given Action0 on the FX thread when onCompleted is called
     * @param onCompleted
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnCompletedFx(Action0 onCompleted) {
        return obs -> obs.doOnCompleted(() -> Platform.runLater(onCompleted::call));
    }

    /**
     * Performs a given Action0 on the FX thread when subscribed to
     * @param subscribe
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnSubscribeFx(Action0 subscribe) {
        return obs -> obs.doOnSubscribe((() -> Platform.runLater(subscribe::call)));
    }

    /**
     * Performs the provided onTerminate action on the FX thread
     * @param onTerminate
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnTerminateFx(Action0 onTerminate) {
        return obs -> obs.doOnTerminate(() -> Platform.runLater(onTerminate::call));
    }

    /**
     * Performs the provided onTerminate action on the FX thread
     * @param onUnsubscribe
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnUnsubscribeFx(Action0 onUnsubscribe) {
        return obs -> obs.doOnUnsubscribe(() -> Platform.runLater(onUnsubscribe::call));
    }

    /**
     * Performs an action on onNext with the provided emission count
     * @param onNext
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnNextCount(Action1<Integer> onNext) {
        return obs -> obs.lift(new OperatorEmissionCounter<>(new CountObserver(onNext,null,null)));
    }

    /**
     * Performs an action on onCompleted with the provided emission count
     * @param onCompleted
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnCompletedCount(Action1<Integer> onCompleted) {
        return obs -> obs.lift(new OperatorEmissionCounter<>(new CountObserver(null,onCompleted,null)));
    }

    /**
     * Performs an action on onError with the provided emission count
     * @param onError
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnErrorCount(Action1<Integer> onError) {
        return obs -> obs.lift(new OperatorEmissionCounter<>(new CountObserver(null,null,onError)));
    }

    /**
     * Performs an action on FX thread on onNext with the provided emission count
     * @param onNext
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnNextCountFx(Action1<Integer> onNext) {
        return obs -> obs.compose(doOnNextCount(i -> Platform.runLater(() -> onNext.call(i))));
    }

    /**
     * Performs an action on FX thread on onCompleted with the provided emission count
     * @param onCompleted
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnCompletedCountFx(Action1<Integer> onCompleted) {
        return obs -> obs.compose(doOnCompletedCount(i -> Platform.runLater(() -> onCompleted.call(i))));
    }

    /**
     * Performs an action on FX thread on onError with the provided emission count
     * @param onError
     * @param <T>
     */
    public static <T> Observable.Transformer<T,T> doOnErrorCountFx(Action1<Integer> onError) {
        return obs -> obs.compose(doOnErrorCount(i -> Platform.runLater(() -> onError.call(i))));
    }


    private static class OperatorEmissionCounter<T> implements Observable.Operator<T,T> {

        private final CountObserver ctObserver;

        OperatorEmissionCounter(CountObserver ctObserver) {
            this.ctObserver = ctObserver;
        }

        @Override
        public Subscriber<? super T> call(Subscriber<? super T> child) {

            return new Subscriber<T>() {
                private int count = 0;
                private boolean done = false;

                @Override
                public void onCompleted() {
                    if (done)
                        return;

                    try {
                        if (ctObserver.doOnCompletedCountAction != null)
                            ctObserver.doOnCompletedCountAction.call(count);
                    } catch (Exception e) {
                        Exceptions.throwIfFatal(e);
                        onError(e);
                        return;
                    }
                    done = true;
                    child.onCompleted();
                }

                @Override
                public void onError(Throwable e) {
                    if (done)
                        return;
                    try {
                        if (ctObserver.doOnErrorCountAction != null)
                            ctObserver.doOnErrorCountAction.call(count);
                    } catch(Exception e1) {
                        Exceptions.throwIfFatal(e1);
                        child.onError(e1);
                    }
                }

                @Override
                public void onNext(T t) {
                    if (done)
                        return;
                    try {
                        if (ctObserver.doOnNextCountAction != null)
                            ctObserver.doOnNextCountAction.call(++count);
                    } catch(Exception e) {
                        Exceptions.throwIfFatal(e);
                        onError(e);
                        return;
                    }
                    child.onNext(t);
                }

                @Override
                public void setProducer(Producer p) {
                    child.setProducer(p);
                }
            };
        }
    }
    private static final class CountObserver {
        private final Action1<Integer> doOnNextCountAction;
        private final Action1<Integer> doOnCompletedCountAction;
        private final Action1<Integer> doOnErrorCountAction;

        CountObserver(Action1<Integer> doOnNextCountAction, Action1<Integer> doOnCompletedCountAction, Action1<Integer> doOnErrorCountAction) {
            this.doOnNextCountAction = doOnNextCountAction;
            this.doOnCompletedCountAction = doOnCompletedCountAction;
            this.doOnErrorCountAction = doOnErrorCountAction;
        }
    }
}
