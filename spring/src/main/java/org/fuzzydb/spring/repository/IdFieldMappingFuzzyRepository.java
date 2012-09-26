package org.fuzzydb.spring.repository;

import java.io.Serializable;
import java.util.Iterator;

import org.fuzzydb.attrs.AttributeDefinitionService;
import org.fuzzydb.attrs.converters.WhirlwindConversionService;
import org.fuzzydb.attrs.search.SearchSpecImpl;
import org.fuzzydb.attrs.userobjects.IdFieldMappedFuzzyItem;
import org.fuzzydb.attrs.userobjects.MappedFuzzyItem;
import org.fuzzydb.attrs.userobjects.MappedItem;
import org.fuzzydb.client.DataOperations;
import org.fuzzydb.client.Ref;
import org.fuzzydb.client.internal.ResultImpl;
import org.fuzzydb.core.query.Result;
import org.fuzzydb.core.query.ResultIterator;
import org.fuzzydb.core.query.ResultSet;
import org.fuzzydb.core.whirlwind.SearchSpec;
import org.fuzzydb.spring.convert.FuzzyEntityConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.model.MappingException;

/**
 * A repository implementation that performs a minimal conversion to get attributes
 * in and out of the database, and is based on having an {@link Id} annotated field
 * which is used as the primary key for these items.
 *
 * @author Neale Upstone
 *
 * @param <T> the type being stored
 */
public class IdFieldMappingFuzzyRepository<T, KEY extends Serializable> extends AbstractConvertingRepository<MappedItem, T, KEY> implements FuzzyRepository<T,KEY>, InitializingBean {

	private final WhirlwindConversionService converter;

	private final AttributeDefinitionService attrDefinitionService;

	private FuzzyEntityConverter<T, MappedItem> entityConverter;

	private final boolean useDefaultNamespace;

	private IdPersistenceHelper<KEY, MappedItem> idPersistenceHelper;

	private Class<? extends MappedItem> internalType;


	@Autowired
	public IdFieldMappingFuzzyRepository(Class<T> type, boolean useDefaultNamespace, DataOperations persister,
			WhirlwindConversionService conversionService, AttributeDefinitionService attributeDefinitionService) {
		super(type, persister);
		this.useDefaultNamespace = useDefaultNamespace;
		this.converter = conversionService;
		this.attrDefinitionService = attributeDefinitionService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		entityConverter = new FuzzyEntityConverter<T, MappedItem>(converter, attrDefinitionService, persister);

		// select correct idPersistenceHelper for the index type
		if (entityConverter.getMappingContext().getPersistentEntity(type).getIdProperty().getType().equals(String.class)) {
			idPersistenceHelper = (IdPersistenceHelper<KEY, MappedItem>) new RefAsStringIdPersistenceHelper<MappedItem>();
			internalType = MappedFuzzyItem.class;
		} else {
			idPersistenceHelper = null; // TODO: The one using an index
			internalType = IdFieldMappedFuzzyItem.class;
		}
	}

	@Override
	protected T fromInternal(MappedItem internal) {
		T result = entityConverter.read(type, internal);
		return result;
	}


	@Override
	protected MappedItem toInternal(T external) {
		MappedItem result;
		try {
			result = getInternalType().newInstance();
		} catch (InstantiationException e) {
			throw new MappingException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new MappingException(e.getMessage(), e);
		}

		entityConverter.write(external, result);

		return result;
	}


	@SuppressWarnings("unchecked")
	@Override
	protected Class<MappedItem> getInternalType() {
		return (Class<MappedItem>) internalType;
	}

	@Override
	protected Iterator<Result<T>> findMatchesInternal(MappedItem internal, String matchStyle, int maxResults) {
		SearchSpec spec = new SearchSpecImpl(getInternalType(), matchStyle);
		spec.setTargetNumResults(maxResults);
		spec.setAttributes(internal);
		ResultSet<Result<MappedItem>> resultsInternal = getPersister().query(getInternalType(), spec);
		final ResultIterator<Result<MappedItem>> resultIterator = resultsInternal.iterator();

		Iterator<Result<T>> iterator = new ConvertingIterator<Result<MappedItem>,Result<T>>(resultIterator) {
			@Override
			protected Result<T> convert(Result<MappedItem> internal) {

				MappedItem item = internal.getItem();
				T external = fromInternal(item);
				Result<T> result = new ResultImpl<T>(external, internal.getScore());
				return result;
			}
		};
		return iterator;
	}

	@Override
	protected MappedItem merge(MappedItem toWrite,
			Ref<MappedItem> existingRef) {

		MappedItem existing = getPersister().retrieve(existingRef);
		existing.mergeFrom(toWrite);
		return existing;
	}

	@Override
	protected void selectNamespace() {
		String namespace = useDefaultNamespace ? "" : type.getCanonicalName();
		getPersister().setNamespace(namespace);
	}

	@Override
	protected IdPersistenceHelper<KEY, MappedItem> getIdPersistenceHelper() {
		return idPersistenceHelper;
	}
}
