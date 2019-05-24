module rxjavafx {
	requires io.reactivex.rxjava2;
	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires org.reactivestreams;

	exports io.reactivex.rxjavafx.observables;
	exports io.reactivex.rxjavafx.observers;
	exports io.reactivex.rxjavafx.schedulers;
	exports io.reactivex.rxjavafx.sources;
	exports io.reactivex.rxjavafx.subscriptions;
	exports io.reactivex.rxjavafx.transformers;
}
