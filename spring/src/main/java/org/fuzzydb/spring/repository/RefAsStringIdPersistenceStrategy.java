package org.fuzzydb.spring.repository;

import org.fuzzydb.attrs.userobjects.MappedItem;
import org.fuzzydb.client.DataOperations;
import org.fuzzydb.client.Ref;
import org.fuzzydb.client.exceptions.UnknownObjectException;
import org.fuzzydb.client.internal.RefImpl;

public final class RefAsStringIdPersistenceStrategy<I extends MappedItem> implements
		PersistByIdPersistenceStrategy<String, I> {


	private final DataOperations persister;


	public RefAsStringIdPersistenceStrategy(DataOperations persister) {
		this.persister = persister;
	}

	@Override
	public boolean exists(String id) {
		return findEntityById(id) != null;
	}

	@Override
	public I findEntityById(String id) {
		Ref<I> ref = toInternalId(id);
		I entity;
		try {
			entity = persister.retrieve(ref);
		} catch (UnknownObjectException e) {
			return null;
		}
		return entity;
	}

	@Override
	public final Ref<I> toInternalId(String id) {
		// Externally we ref as Ref<T>  and we are using the real ref here
		return RefImpl.valueOf(id);
	}

	@Override
	public String toExternalId(Ref<I> ref) {
		return ((RefImpl<I>) ref).asString();
	}

	@Override
	public I merge(I entity, String id) {
		Ref<I> internalId = toInternalId(id);
		I existing = persister.retrieve(internalId);
		existing.mergeFrom(entity);
		return existing;
	}
}