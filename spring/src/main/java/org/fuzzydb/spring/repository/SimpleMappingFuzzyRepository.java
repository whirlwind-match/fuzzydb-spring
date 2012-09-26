package org.fuzzydb.spring.repository;

import java.util.Iterator;

import org.fuzzydb.attrs.AttributeDefinitionService;
import org.fuzzydb.attrs.converters.WhirlwindConversionService;
import org.fuzzydb.attrs.search.SearchSpecImpl;
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

/**
 * A simple (PoC) Repository implementation that performs a minimal conversion to get attributes
 * in and out of the database
 *
 * Fuller support will come in time. This is a starting point to get a walking-skeleton
 * up and err... walking.
 *
 * @author Neale Upstone
 *
 * @param <T> the type being stored (Must contain a field: Map<String,Object> attributes for the fuzzy data)
 */
public class SimpleMappingFuzzyRepository<T> extends AbstractConvertingRepository<MappedItem, T, String> implements FuzzyRepository<T,String>, InitializingBean {

	private final WhirlwindConversionService converter;

	private final AttributeDefinitionService attrDefinitionService;

	private FuzzyEntityConverter<T, MappedItem> entityConverter;

	private final boolean useDefaultNamespace;

	private final IdPersistenceHelper<String, MappedItem> idPersistenceHelper = new RefAsStringIdPersistenceHelper<MappedItem>();

	@Autowired
	public SimpleMappingFuzzyRepository(Class<T> type, boolean useDefaultNamespace, DataOperations persister,
			WhirlwindConversionService conversionService, AttributeDefinitionService attributeDefinitionService) {
		super(type, persister);
		this.useDefaultNamespace = useDefaultNamespace;
		this.converter = conversionService;
		this.attrDefinitionService = attributeDefinitionService;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		entityConverter = new FuzzyEntityConverter<T,MappedItem>(converter, attrDefinitionService, persister);
	}

	@Override
	protected T fromInternal(MappedItem internal) {
		T result = entityConverter.read(type, internal);
		return result;
	}


	@Override
	protected MappedItem toInternal(T external) {
		MappedItem result = new MappedFuzzyItem();

		entityConverter.write(external, result);

		return result;
	}

	@Override
	protected Class<MappedFuzzyItem> getInternalType() {
		return MappedFuzzyItem.class;
	}

	@Override
	protected Iterator<Result<T>> findMatchesInternal(MappedItem internal, String matchStyle, int maxResults) {
		SearchSpec spec = new SearchSpecImpl(getInternalType(), matchStyle);
		spec.setTargetNumResults(maxResults);
		spec.setAttributes(internal);
		ResultSet<Result<MappedItem>> resultsInternal = getPersister().query(MappedItem.class, spec);
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
		getPersister().setNamespace(
				useDefaultNamespace ? "" : type.getCanonicalName()
				);
	}

	@Override
	protected IdPersistenceHelper<String, MappedItem> getIdPersistenceHelper() {
		return idPersistenceHelper;
	}

//	private BasicPersistentEntity<T, BasicPers> createEntity(Comparator<T> comparator) {
//		return new BasicPersistentEntity<Person, T>(ClassTypeInformation.from(Person.class), comparator);
//	}

}
