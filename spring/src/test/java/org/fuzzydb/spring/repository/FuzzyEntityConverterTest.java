package org.fuzzydb.spring.repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.fuzzydb.attrs.AttributeDefinitionService;
import org.fuzzydb.attrs.bool.BooleanValue;
import org.fuzzydb.attrs.converters.WhirlwindConversionService;
import org.fuzzydb.attrs.enums.EnumDefinition;
import org.fuzzydb.attrs.internal.AttrDefinitionMgr;
import org.fuzzydb.attrs.simple.FloatRangePreference;
import org.fuzzydb.attrs.simple.FloatValue;
import org.fuzzydb.attrs.userobjects.MappedFuzzyItem;
import org.fuzzydb.client.DataOperations;
import org.fuzzydb.client.Ref;
import org.fuzzydb.client.internal.RefImpl;
import org.fuzzydb.core.whirlwind.internal.IAttribute;
import org.fuzzydb.core.whirlwind.internal.IAttributeMap;
import org.fuzzydb.spring.repository.FuzzyEntityConverterTest.FuzzyItem.LiftType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.data.annotation.Id;


@RunWith(MockitoJUnitRunner.class)
public class FuzzyEntityConverterTest  {


	private IdFieldMappingFuzzyRepository<FuzzyItem, String> repo;

	@Mock
	private DataOperations persister;

	private final AttributeDefinitionService attrDefinitionService = new AttrDefinitionMgr();

	// prime the attribute mappings so we know what ids to look for
	private final int isMaleId = attrDefinitionService.getAttrId("isMale", Boolean.class);
	private final int ageId = attrDefinitionService.getAttrId("age", Float.class);
	private final int ageRangeId = attrDefinitionService.getAttrId("ageRange", float[].class);
	private final int liftTypeId = attrDefinitionService.getAttrId("liftType", Enum.class);


	@Captor
	private ArgumentCaptor<MappedFuzzyItem> wwItemCaptor;


	@Before
	public void injectMocksManually() throws Exception {
		EnumDefinition definition = attrDefinitionService.getEnumDefinition("liftType");
		attrDefinitionService.associateAttrToEnumDef(liftTypeId, definition);
		definition.getEnumValue(LiftType.OfferingLift.name(), liftTypeId);
		definition.getEnumValue(LiftType.WantLift.name(), liftTypeId);

		WhirlwindConversionService converter = new WhirlwindConversionService();
		new DirectFieldAccessor(converter).setPropertyValue("attrDefinitionService", attrDefinitionService);
		converter.afterPropertiesSet();

		repo = new IdFieldMappingFuzzyRepository<>(FuzzyItem.class, false, persister, converter, attrDefinitionService);
		repo.afterPropertiesSet();
	}

	@Test
	public void shouldConvertToWWItemOnSave() {
		// mocks
		when(persister.save((FuzzyItem)anyObject())).thenReturn(new RefImpl<>(1, 2, 3));


		// the action
		FuzzyItem external = new FuzzyItem();
//		external.ref = "1_2_3";

		external.populateTestData();
		FuzzyItem result = repo.save(external);

		// verify attributes converted
		verify(persister, times(1)).save(wwItemCaptor.capture());
		MappedFuzzyItem storedInDatabase = wwItemCaptor.getValue();
		IAttributeMap<IAttribute> attrs = storedInDatabase.getAttributeMap();
		assertThat(attrs.findAttr(isMaleId),equalTo(new BooleanValue(isMaleId,false)));
		FloatValue attr = (FloatValue)attrs.findAttr(ageId);

		assertThat(attr, equalTo(new FloatValue(ageId,1.1f)));

		FloatRangePreference floatPref = (FloatRangePreference) attrs.findAttr(ageRangeId);
		assertThat(floatPref, equalTo(new FloatRangePreference(ageRangeId, 25f, 30f, 38f)));

		// Verify id got set in result
		assertThat(result.ref, equalTo("1_2_3"));
	}


	@SuppressWarnings("unchecked")
	@Test
	public void shouldConvertToFieldsOnRetrieve() {

		// mock
		MappedFuzzyItem internal = getWWItem();
		when(persister.retrieve((Ref<MappedFuzzyItem>) anyObject())).thenReturn(internal);
		when(persister.getRef((FuzzyItem)anyObject())).thenReturn(new RefImpl<>(1, 2, 3));


		// the action
		FuzzyItem result = repo.findOne("1_1_1");

		// verify
		verify(persister, times(1)).retrieve(ArgumentCaptor.forClass(Ref.class).capture());
		assertEquals(Boolean.TRUE, result.isMale);
		assertEquals(2.2f, result.age, 0f);
		assertArrayEquals(new float[]{1.2f, 2.3f, 3.4f}, result.ageRange, 0f);
	}


	private MappedFuzzyItem getWWItem() {
		MappedFuzzyItem item = new MappedFuzzyItem();
		item.getAttributeMap().putAttr(new BooleanValue(isMaleId, true));
		item.getAttributeMap().putAttr(new FloatValue(ageId, 2.2f));
		item.getAttributeMap().putAttr(new FloatRangePreference(ageRangeId, 1.2f, 2.3f, 3.4f));
		return item;
	}


	public static class FuzzyItem implements Serializable {

		public enum LiftType {
			OfferingLift, WantLift
		}

		private static final long serialVersionUID = 1L;

		@Id
		String ref;

		Boolean isMale;

		LiftType liftType;

		Float age;

		float[] ageRange;

		void populateTestData() {
			isMale = Boolean.FALSE;
			age = 1.1f;
			ageRange = new float[]{25f, 30f, 38f};
			liftType = LiftType.OfferingLift;
		}
	}
}
