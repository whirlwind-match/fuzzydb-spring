package org.fuzzydb.spring.repository;

import org.fuzzydb.client.DataOperations;
import org.fuzzydb.client.Ref;


/**
 * Persistence helper that deals with operations based on an ID which isn't necessarily
 * native Ref, but instead may be based on a key added to a unique index which is used
 * to find the entity on which to operate.
 */
public interface IdPersistenceHelper<ID, INTERNAL_TYPE> {

	boolean exists(DataOperations persister, ID id);

	INTERNAL_TYPE findEntityById(DataOperations persister, ID id);

	Ref<INTERNAL_TYPE> toInternalId(ID id);

	ID toExternalId(Ref<INTERNAL_TYPE> ref);

}
