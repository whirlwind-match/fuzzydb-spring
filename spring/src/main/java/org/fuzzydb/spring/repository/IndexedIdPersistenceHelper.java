package org.fuzzydb.spring.repository;

import org.fuzzydb.attrs.userobjects.IdFieldMappedFuzzyItem;
import org.fuzzydb.client.DataOperations;
import org.fuzzydb.client.Ref;
import org.fuzzydb.client.exceptions.UnknownObjectException;

public final class IndexedIdPersistenceHelper<ID> implements
		IdPersistenceHelper<ID, IdFieldMappedFuzzyItem> {

	private final String idField = "id";
	private final Class<IdFieldMappedFuzzyItem> internalType = IdFieldMappedFuzzyItem.class;
	private final DataOperations persister;

	public IndexedIdPersistenceHelper(DataOperations persister) {
		this.persister = persister;
	}

	@Override
	public boolean exists(ID id) {
		return findEntityById(id) != null;
	}

	@Override
	public IdFieldMappedFuzzyItem findEntityById(ID id) {
		IdFieldMappedFuzzyItem entity;
		try {
			entity = persister.retrieve(internalType, idField, (Comparable<?>)id);
		} catch (UnknownObjectException e) {
			return null;
		}
		return entity;
	}

	@Override
	public final Ref<IdFieldMappedFuzzyItem> toInternalId(ID id) {
		IdFieldMappedFuzzyItem item = findEntityById(id);
		return persister.getRef(item);
	}

	@Override
	public ID toExternalId(Ref<IdFieldMappedFuzzyItem> ref) {
		return null;  // Return null and let entity converter do it
	}

	@Override
	public IdFieldMappedFuzzyItem merge(IdFieldMappedFuzzyItem entity, ID id) {
		IdFieldMappedFuzzyItem existing = findEntityById(id);
		if (existing == null) {
			return entity; // it's a new entity
		}
		existing.mergeFrom(entity);
		return existing;
	}

}