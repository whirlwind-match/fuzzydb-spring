package org.fuzzydb.spring.repository;

import org.fuzzydb.attrs.userobjects.IdFieldMappedFuzzyItem;
import org.fuzzydb.client.DataOperations;
import org.fuzzydb.client.Ref;
import org.fuzzydb.client.exceptions.UnknownObjectException;

public final class IndexedIdPersistenceHelper<ID extends Comparable<ID>> implements
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
			entity = persister.retrieve(internalType, idField, id);
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
		throw new UnsupportedOperationException(); // HERE!!!
		// TODO Auto-generated method stub
//		return ((RefImpl<I>) ref).asString();
	}
}