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

	/**
	 * Should do anything needed to merge an existing back in with
	 * existingRef from the current transaction.
	 *
	 * @returns entity or copy of entity that is ready to be natively persisted.
	 */
	public I merge(I entity, Ref<I> internalId) {
		I existing = persister.retrieve(internalId);
		existing.mergeFrom(entity);
		return existing;
	}

	@Override
	public String saveOrUpdate(I entity, String id) {
		if (id != null) { // already supplied, so either insert with ID or is merge
			Ref<I> internalId = toInternalId(id);
			I merged = merge(entity, internalId);
			try {
				persister.update(merged);
				return id; // on update id hasn't changed
			} catch (UnknownObjectException e) {
				deleteIfPossible(internalId);
			}
		}
		Ref<I> ref = persister.save(entity);
		return toExternalId(ref);
	}

	private void deleteIfPossible(Ref<I> existingRef) {
//		log.warn("save() - update of detached entity detected, with no merge support so doing delete/create instead on {}", existingRef);
		try {
			persister.delete(existingRef);
		} catch (UnknownObjectException e) {
//			log.debug("Nothing deleted.");
		}
	}

}