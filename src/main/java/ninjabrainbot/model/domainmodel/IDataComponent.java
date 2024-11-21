package ninjabrainbot.model.domainmodel;

import java.io.Serializable;

/**
 * Represents a piece of data, write permissions of DataComponents are automatically handled by the DomainModel.
 * Any modifications to a DataComponent are automatically saved by the DomainModel, for the undo action to work.
 * The generic type T should be immutable to ensure that no modifications to the data go unnoticed by the domain model.
 */
public interface IDataComponent<T extends Serializable> extends IFundamentalComponent<T, T> {

}