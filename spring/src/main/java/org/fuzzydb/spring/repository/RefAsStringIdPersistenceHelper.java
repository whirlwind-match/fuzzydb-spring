package org.fuzzydb.spring.repository;

import org.fuzzydb.client.DataOperations;
import org.fuzzydb.client.Ref;
import org.fuzzydb.client.exceptions.UnknownObjectException;
import org.fuzzydb.client.internal.RefImpl;

public final class RefAsStringIdPersistenceHelper<I> implements
		IdPersistenceHelper<String, I> {


	private final DataOperations persister;


	public RefAsStringIdPersistenceHelper(DataOperations persister) {
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
}