package io.reactivex.rxjavafx.observers;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.Arrays;
import java.util.Objects;

public abstract class ObservableListenerHelper<T> implements ObservableValue<T> {
	private boolean sentinel;
	private int     invalidationSize = 0;
	private int     size             = 0;
	private Object  listener         = null;
	private T       value            = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(InvalidationListener listener) {
		Objects.requireNonNull(listener);
		if (size == 0) {
			sentinel = false;
			invalidationSize = 1;
			this.listener = listener;
		} else if (size == 1) {
			sentinel = false;
			if (invalidationSize == 1) {
				invalidationSize = 2;
				this.listener = new Object[]{this.listener, listener};
			} else {
				invalidationSize = 1;
				this.listener = new Object[]{listener, this.listener};
			}
		} else {
			Object[] l = (Object[]) this.listener;

			if (l.length <= size + 1 || sentinel) {
				sentinel = false;
				l = Arrays.copyOf(l, l.length * 3 / 2 + 1);
				this.listener = l;
			}
			if (size > invalidationSize) {
				System.arraycopy(l, invalidationSize, l, invalidationSize + 1, size - invalidationSize);
			}
			l[invalidationSize] = listener;
			invalidationSize++;
		}
		size++;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(InvalidationListener listener) {
		Objects.requireNonNull(listener);
		if (0 < invalidationSize) {
			if (size == 1) {
				if (invalidationSize == 1 && this.listener.equals(listener)) {
					sentinel = false;
					this.listener = null;
					invalidationSize--;
					size--;
				}
			} else if (size == 2) {
				Object[] l = (Object[]) this.listener;
				if (listener.equals(l[0])) {
					sentinel = false;
					invalidationSize--;
					size--;
					this.listener = l[1];
				} else if (invalidationSize == 2 && listener.equals(l[1])) {
					sentinel = false;
					invalidationSize--;
					size--;
					this.listener = l[0];
				}
			} else {
				Object[] l = (Object[]) this.listener;

				for (int i = 0; i < invalidationSize; i++) {
					if (listener.equals(l[i])) {
						if (sentinel) {
							sentinel = false;
							l = Arrays.copyOf(l, l.length);
							this.listener = l;
						}
						if (i + 1 < size) {
							System.arraycopy(l, i + 1, l, i, size - i - 1);
						} else {
							l[i] = null;
						}
						invalidationSize--;
						size--;
						break;
					}
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addListener(ChangeListener<? super T> listener) {
		Objects.requireNonNull(listener);
		if (size == 0) {
			sentinel = false;
			this.listener = listener;
			this.value = getValue();
		} else if (size == 1) {
			sentinel = false;
			this.listener = new Object[]{this.listener, listener};
		} else {
			Object[] l = (Object[]) this.listener;
			if (l.length <= size + 1) { // test for sentinel not required as we put the new listener behind this.size, thus it won't be fired
				sentinel = false;
				l = Arrays.copyOf(l, l.length * 3 / 2 + 1);
				this.listener = l;
			}
			l[size] = listener;
		}
		if (invalidationSize == size) {
			this.value = getValue();
		}
		size++;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeListener(ChangeListener<? super T> listener) {
		Objects.requireNonNull(listener);
		if (invalidationSize < size) {
			if (size == 1) {
				sentinel = false;
				size--;
				this.listener = null;
				this.value = null;
			} else if (size == 2) {
				Object[] l = (Object[]) this.listener;
				if (listener.equals(l[1])) {
					sentinel = false;
					size--;
					this.listener = l[0];
					if (invalidationSize == 1) {
						this.value = null;
					}
				} else if (invalidationSize == 0 && listener.equals(l[1])) {
					sentinel = false;
					size--;
					this.listener = l[1];
				}
			} else {
				Object[] l = (Object[]) this.listener;

				for (int i = invalidationSize; i < size; i++) {
					if (listener.equals(l[i])) {
						if (sentinel) {
							sentinel = false;
							l = Arrays.copyOf(l, l.length);
							this.listener = l;
						}
						if (i + 1 < size) {
							System.arraycopy(l, i + 1, l, i, size - i - 1);
						} else {
							l[i] = null;
						}
						size--;
						if (size == invalidationSize) {
							this.value = null;
						}
						break;
					}
				}
			}
		}
	}

	protected void fireChange() {
		Object listener = this.listener;
		int invalidationSize = this.invalidationSize;
		int size = this.size;
		try {
			sentinel = true;

			if (size == 1) {
				if (invalidationSize == 1) {
					try {
						((InvalidationListener) listener).invalidated(this);
					} catch (Exception e) {
						Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
					}
				} else {
					T oldValue = this.value;
					this.value = getValue();
					try {
						((ChangeListener<? super T>) listener).changed(this, oldValue, this.value);
					} catch (Exception e) {
						Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
					}
				}
			} else if (size > 0) {
				Object[] l = (Object[]) listener;

				for (int i = 0; i < invalidationSize; i++) {
					try {
						((InvalidationListener) l[i]).invalidated(this);
					} catch (Exception e) {
						Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
					}
				}

				if (invalidationSize < size) {
					T oldValue = this.value;
					this.value = getValue();
					for (int i = invalidationSize; i < size; i++) {
						try {
							((ChangeListener<? super T>) l[i]).changed(this, oldValue, this.value);
						} catch (Exception e) {
							Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
						}
					}
				}
			}
		} finally {
			sentinel = false;
		}
	}
}
