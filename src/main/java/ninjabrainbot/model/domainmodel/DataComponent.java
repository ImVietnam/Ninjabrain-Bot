package ninjabrainbot.model.domainmodel;

import java.io.Serializable;
import java.util.function.Consumer;

import ninjabrainbot.event.ISubscribable;
import ninjabrainbot.event.ObservableField;
import ninjabrainbot.event.Subscription;
import ninjabrainbot.util.Assert;

/**
 * Represents a piece of data, write permissions of DataComponents are automatically handled by the DomainModel.
 * Any modifications to a DataComponent are automatically saved by the DomainModel, for the undo action to work.
 * The generic type T should be immutable to ensure that no modifications to the data go unnoticed by the domain model.
 * If null is passed as the IDomainModel to the constructor, the data in the DataComponent will not be saved
 * for the undo action, and the DataComponent will not be write locked. However, in most cases where saving of
 * the data for undo is not needed, an {@link ObservableField} is more suiting.
 */
public class DataComponent<T extends Serializable> implements IDataComponent<T> {

	private final IDomainModel domainModel;
	private final ObservableField<T> observableField;
	private final ISubscribable<T> externalEvent;
	private final T defaultValue;
	private final String uniqueId;

	public DataComponent(String uniqueId, IDomainModel domainModel) {
		this(uniqueId, domainModel, null);
	}

	public DataComponent(String uniqueId, IDomainModel domainModel, T defaultValue) {
		Assert.isNotNull(domainModel, "Domain model cannot be null");
		this.domainModel = domainModel;
		this.uniqueId = uniqueId;
		observableField = new ObservableField<>(defaultValue);
		externalEvent = domainModel.createExternalEventFor(observableField);
		this.defaultValue = defaultValue;
		domainModel.registerFundamentalComponent(this);
	}

	@Override
	public T get() {
		return observableField.get();
	}

	@Override
	public T getAsImmutable() {
		return get();
	}

	@Override
	public void set(T value) {
		if (domainModel != null)
			domainModel.checkWriteAccess();
		observableField.set(value);
	}

	@Override
	public void reset() {
		set(defaultValue);
		Assert.isTrue(isReset());
	}

	@Override
	public boolean contentEquals(T value) {
		return observableField.get() == value;
	}

	@Override
	public boolean isReset() {
		return get() == defaultValue;
	}

	@Override
	public Subscription subscribeInternal(Consumer<T> subscriber) {
		Assert.isTrue(domainModel.isInternalSubscriptionRegistrationAllowed(), "Attempted to subscribe to internal events after domain model initialization has completed. External subscribers should use IDataComponent.subscribe().");
		return observableField.subscribe(subscriber);
	}

	@Override
	public Subscription subscribe(Consumer<T> subscriber) {
		Assert.isTrue(domainModel.isExternalSubscriptionRegistrationAllowed(), "Attempted to subscribe to external events before domain model initialization has completed. Internal subscribers should use IDataComponent.subscribeInternal().");
		return externalEvent.subscribe(subscriber);
	}

	@Override
	public String uniqueId() {
		return uniqueId;
	}

	@Override
	public T getAsSerializable() {
		return get();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setFromDeserializedObject(Serializable deserialized) {
		set((T) deserialized);
	}

}
