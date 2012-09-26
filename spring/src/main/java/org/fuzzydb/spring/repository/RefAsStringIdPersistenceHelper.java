package org.fuzzydb.spring.repository;

import org.fuzzydb.client.DataOperations;
import org.fuzzydb.client.Ref;
import org.fuzzydb.client.exceptions.UnknownObjectException;
import org.fuzzydb.client.internal.RefImpl;

public final class RefAsStringIdPersistenceHelper<I> implements
		IdPersistenceHelper<String, I> {

	@Override
	public boolean exists(DataOperations persister, String id) {
		return findEntityById(persister, id) != null;
	}

	@Override
	public I findEntityById(DataOperations persister, String id) {
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