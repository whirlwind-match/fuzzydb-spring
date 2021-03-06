package org.fuzzydb.spring.repository;

import java.io.Serializable;

import org.fuzzydb.client.Ref;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.util.Assert;


/**
 * A Repository implementation that performs no conversion.
 *
 * This is adequate for objects not requiring any fuzzy matching.  The absence of
 * any conversion therefore requires that the repository is for an embedded instance,
 * or that the Class being persisted is available on the server's classpath.
 *
 * @author Neale Upstone
 *
 * @param <T>
 */
public class RawFuzzyRepository<T> extends AbstractConvertingRepository<T, T, Ref<T>> {

	private final PersistByIdPersistenceStrategy<Ref<T>, T> idPersistenceHelper = new RawIdPersistenceStrategy<T>(persister);

	public RawFuzzyRepository(Class<T> type) {
		super(type);
		Assert.isAssignable(Serializable.class, type, "Items being persisted by Raw repositories must be Serializable. ");
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (idField == null || !idField.getType().isAssignableFrom(Ref.class)) {
			throw new MappingException(type.getCanonicalName() + " must have a @Key(unique=true) annotated field of type Ref");
		}
	}

	@Override
	protected T fromInternal(T internal) {
		return internal;
	}

	@Override
	protected T toInternal(T external) {
		return external;
	}

	@Override
	protected Class<T> getInternalType() {
		return type;
	}

	@Override
	protected void selectNamespace() {
		// deliberately empty for Raw
	}

	@Override
	protected PersistByIdPersistenceStrategy<Ref<T>, T> getPersistenceStrategy() {
		return idPersistenceHelper;
	}
}
