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

import io.reactivex.rxjava3.core.Observable;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import io.reactivex.rxjavafx.subscriptions.JavaFxSubscriptions;
import java.util.concurrent.atomic.AtomicLong;

public final class TimerSource {
    private TimerSource() {
    }


    public static Observable<Long> interval(final Duration duration) {
        return Observable.create(sub -> {
            final AtomicLong value = new AtomicLong(0);
            Timeline timeline = new Timeline(new KeyFrame(duration, ae -> sub.onNext(value.getAndIncrement())));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();

            sub.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(timeline::stop));
        });
    }
}
