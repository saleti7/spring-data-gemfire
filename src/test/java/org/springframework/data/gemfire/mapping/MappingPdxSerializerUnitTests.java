/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.mapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.data.gemfire.util.RuntimeExceptionFactory.newIllegalArgumentException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.geode.pdx.PdxReader;
import org.apache.geode.pdx.PdxSerializer;
import org.apache.geode.pdx.PdxWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.data.convert.EntityInstantiator;
import org.springframework.data.convert.EntityInstantiators;
import org.springframework.data.gemfire.repository.sample.Address;
import org.springframework.data.gemfire.repository.sample.Person;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.ParameterValueProvider;

/**
 * Unit tests for {@link MappingPdxSerializer}.
 *
 * @author Oliver Gierke
 * @author John Blum
 * @see org.junit.Rule
 * @see org.junit.Test
 * @see org.junit.rules.ExpectedException
 * @see org.junit.runner.RunWith
 * @see org.mockito.Mock
 * @see org.mockito.Mockito
 * @see org.mockito.runners.MockitoJUnitRunner
 * @see org.springframework.core.convert.ConversionService
 * @see org.springframework.data.gemfire.mapping.MappingPdxSerializer
 * @see org.apache.geode.pdx.PdxReader
 * @see org.apache.geode.pdx.PdxSerializer
 * @see org.apache.geode.pdx.PdxWriter
 */
@RunWith(MockitoJUnitRunner.class)
public class MappingPdxSerializerUnitTests {

	ConversionService conversionService;

	GemfireMappingContext mappingContext;

	MappingPdxSerializer pdxSerializer;

	@Mock
	EntityInstantiator mockInstantiator;

	@Mock
	PdxReader mockReader;

	@Mock
	PdxSerializer mockAddressSerializer;

	@Mock
	PdxWriter mockWriter;

	@Before
	public void setUp() {

		this.conversionService = new GenericConversionService();
		this.mappingContext = new GemfireMappingContext();
		this.pdxSerializer = new MappingPdxSerializer(this.mappingContext, this.conversionService);
		this.pdxSerializer.setCustomSerializers(Collections.singletonMap(Address.class, this.mockAddressSerializer));
	}

	@Test
	public void constructDefaultMappingPdxSerializer() {

		MappingPdxSerializer pdxSerializer = new MappingPdxSerializer();

		assertThat(pdxSerializer.getConversionService()).isInstanceOf(DefaultConversionService.class);
		assertThat(pdxSerializer.getCustomSerializers()).isEmpty();
		assertThat(pdxSerializer.getGemfireInstantiators()).isInstanceOf(EntityInstantiators.class);
		assertThat(pdxSerializer.getMappingContext()).isInstanceOf(GemfireMappingContext.class);
	}

	@Test
	public void constructMappingPdxSerializer() {

		ConversionService mockConversionService = mock(ConversionService.class);
		GemfireMappingContext mockMappingContext = mock(GemfireMappingContext.class);

		MappingPdxSerializer pdxSerializer = new MappingPdxSerializer(mockMappingContext, mockConversionService);

		assertThat(pdxSerializer.getConversionService()).isEqualTo(mockConversionService);
		assertThat(pdxSerializer.getCustomSerializers()).isEmpty();
		assertThat(pdxSerializer.getGemfireInstantiators()).isInstanceOf(EntityInstantiators.class);
		assertThat(pdxSerializer.getMappingContext()).isEqualTo(mockMappingContext);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructMappingPdxSerializerWithNullConversionService() {

		try {
			new MappingPdxSerializer(this.mappingContext, null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("ConversionService is required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructMappingPdxSerializerWithNullMappingContext() {

		try {
			new MappingPdxSerializer(null, this.conversionService);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("MappingContext is required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void createMappingPdxSerializer() {

		ConversionService mockConversionService = mock(ConversionService.class);
		GemfireMappingContext mockMappingContext = mock(GemfireMappingContext.class);

		MappingPdxSerializer pdxSerializer = MappingPdxSerializer.create(mockMappingContext, mockConversionService);

		assertThat(pdxSerializer).isNotNull();
		assertThat(pdxSerializer.getConversionService()).isEqualTo(mockConversionService);
		assertThat(pdxSerializer.getMappingContext()).isEqualTo(mockMappingContext);
	}

	@Test
	public void createWithNullConversionService() {

		GemfireMappingContext mockMappingContext = mock(GemfireMappingContext.class);

		MappingPdxSerializer pdxSerializer = MappingPdxSerializer.create(mockMappingContext, null);

		assertThat(pdxSerializer).isNotNull();
		assertThat(pdxSerializer.getConversionService()).isInstanceOf(ConversionService.class);
		assertThat(pdxSerializer.getMappingContext()).isEqualTo(mockMappingContext);
	}

	@Test
	public void createWithNullMappingContext() {

		ConversionService mockConversionService = mock(ConversionService.class);

		MappingPdxSerializer pdxSerializer = MappingPdxSerializer.create(null, mockConversionService);

		assertThat(pdxSerializer).isNotNull();
		assertThat(pdxSerializer.getConversionService()).isEqualTo(mockConversionService);
		assertThat(pdxSerializer.getMappingContext()).isInstanceOf(GemfireMappingContext.class);
	}

	@Test
	public void createWithNullConversionServiceAndNullMappingContext() {

		MappingPdxSerializer pdxSerializer = MappingPdxSerializer.create(null, null);

		assertThat(pdxSerializer).isNotNull();
		assertThat(pdxSerializer.getConversionService()).isInstanceOf(ConversionService.class);
		assertThat(pdxSerializer.getMappingContext()).isInstanceOf(GemfireMappingContext.class);
	}

	@Test
	public void setCustomSerializersWithMappingOfClassTypesToPdxSerializers() {

		Map<Class<?>, PdxSerializer> customSerializers =
			Collections.singletonMap(Person.class, mock(PdxSerializer.class));

		this.pdxSerializer.setCustomSerializers(customSerializers);

		assertThat(this.pdxSerializer.getCustomSerializers()).isEqualTo(customSerializers);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setCustomSerializersToNull() {

		try {
			this.pdxSerializer.setCustomSerializers(null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("Custom PdxSerializers are required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void getCustomSerializerForMappedType() {

		PdxSerializer mockPdxSerializer = mock(PdxSerializer.class);

		Map<Class<?>, PdxSerializer> customSerializers = Collections.singletonMap(Person.class, mockPdxSerializer);

		this.pdxSerializer.setCustomSerializers(customSerializers);

		assertThat(this.pdxSerializer.getCustomSerializer(Person.class)).isEqualTo(mockPdxSerializer);
	}

	@Test
	public void getCustomSerializerForNonMappedType() {

		PdxSerializer mockPdxSerializer = mock(PdxSerializer.class);

		Map<Class<?>, PdxSerializer> customSerializers = Collections.singletonMap(Person.class, mockPdxSerializer);

		this.pdxSerializer.setCustomSerializers(customSerializers);

		assertThat(this.pdxSerializer.getCustomSerializer(Address.class)).isNull();
	}

	@Test
	public void setGemfireInstantiatorsWithEntityInstantiators() {

		EntityInstantiators mockEntityInstantiators = mock(EntityInstantiators.class);

		this.pdxSerializer.setGemfireInstantiators(mockEntityInstantiators);

		assertThat(this.pdxSerializer.getGemfireInstantiators()).isSameAs(mockEntityInstantiators);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setGemfireInstantiatorsWithNullEntityInstantiators() {

		try {
			this.pdxSerializer.setGemfireInstantiators((EntityInstantiators) null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("EntityInstantiators are required");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void setGemfireInstantiatorsWithMappingOfClassTypesToEntityInstantiators() {

		Map<Class<?>, EntityInstantiator> entityInstantiators =
			Collections.singletonMap(Person.class, mock(EntityInstantiator.class));

		this.pdxSerializer.setGemfireInstantiators(entityInstantiators);

		assertThat(this.pdxSerializer.getGemfireInstantiators()).isInstanceOf(EntityInstantiators.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setGemfireInstantiatorsWithNullMap() {

		try {
			this.pdxSerializer.setGemfireInstantiators((Map<Class<?>, EntityInstantiator>) null);
		}
		catch (IllegalArgumentException expected) {

			assertThat(expected).hasMessage("CustomInstantiators must not be null!");
			assertThat(expected).hasNoCause();

			throw expected;
		}
	}

	@Test
	public void getInstantiatorForManagedPersistentEntityWithInstantiator() {

		EntityInstantiator mockEntityInstantiator = mock(EntityInstantiator.class);

		Map<Class<?>, EntityInstantiator> entityInstantiators =
			Collections.singletonMap(Person.class, mockEntityInstantiator);

		PersistentEntity mockEntity = mock(PersistentEntity.class);

		when(mockEntity.getType()).thenReturn(Person.class);

		this.pdxSerializer.setGemfireInstantiators(entityInstantiators);

		assertThat(this.pdxSerializer.getInstantiatorFor(mockEntity)).isEqualTo(mockEntityInstantiator);

		verify(mockEntity, atLeast(1)).getType();
		verifyZeroInteractions(mockEntityInstantiator);
	}

	@Test
	public void getInstantiatorForNonManagedPersistentEntityWithNoInstantiator() {

		EntityInstantiator mockEntityInstantiator = mock(EntityInstantiator.class);

		Map<Class<?>, EntityInstantiator> entityInstantiators =
			Collections.singletonMap(Person.class, mockEntityInstantiator);

		PersistentEntity mockEntity = mock(PersistentEntity.class);

		when(mockEntity.getType()).thenReturn(Address.class);

		this.pdxSerializer.setGemfireInstantiators(entityInstantiators);

		assertThat(this.pdxSerializer.getInstantiatorFor(mockEntity)).isNotEqualTo(mockEntityInstantiator);

		verify(mockEntity, atLeast(1)).getType();
		verifyZeroInteractions(mockEntityInstantiator);
	}

	@Test
	public void isReadableWithNonTransientPropertyReturnsTrue() {

		GemfirePersistentProperty mockPersistentProperty = mock(GemfirePersistentProperty.class);

		when(mockPersistentProperty.isTransient()).thenReturn(false);

		assertThat(this.pdxSerializer.isReadable(mockPersistentProperty)).isTrue();

		verify(mockPersistentProperty, times(1)).isTransient();
	}

	@Test
	public void isReadableWithTransientPropertyReturnsFalse() {

		GemfirePersistentProperty mockPersistentProperty = mock(GemfirePersistentProperty.class);

		when(mockPersistentProperty.isTransient()).thenReturn(true);

		assertThat(this.pdxSerializer.isReadable(mockPersistentProperty)).isFalse();

		verify(mockPersistentProperty, times(1)).isTransient();
	}

	@Test
	public void isWritableWithWritablePropertyReturnsTrue() {

		GemfirePersistentEntity<?> mockEntity = mock(GemfirePersistentEntity.class);

		GemfirePersistentProperty mockProperty = mock(GemfirePersistentProperty.class);

		when(mockEntity.isConstructorArgument(any(GemfirePersistentProperty.class))).thenReturn(false);
		when(mockProperty.isTransient()).thenReturn(false);
		when(mockProperty.isWritable()).thenReturn(true);

		assertThat(this.pdxSerializer.isWritable(mockEntity, mockProperty)).isTrue();

		verify(mockEntity, times(1)).isConstructorArgument(eq(mockProperty));
		verify(mockProperty, times(1)).isWritable();
		verify(mockProperty, times(1)).isTransient();
	}

	@Test
	public void isWritableWithConstructorArgumentPropertyReturnsFalse() {

		GemfirePersistentEntity<?> mockEntity = mock(GemfirePersistentEntity.class);

		GemfirePersistentProperty mockProperty = mock(GemfirePersistentProperty.class);

		when(mockEntity.isConstructorArgument(any(GemfirePersistentProperty.class))).thenReturn(true);

		assertThat(this.pdxSerializer.isWritable(mockEntity, mockProperty)).isFalse();

		verify(mockEntity, times(1)).isConstructorArgument(eq(mockProperty));
		verify(mockProperty, never()).isWritable();
		verify(mockProperty, never()).isTransient();
	}

	@Test
	public void isWritableWithNonWritablePropertyReturnsFalse() {

		GemfirePersistentEntity<?> mockEntity = mock(GemfirePersistentEntity.class);

		GemfirePersistentProperty mockProperty = mock(GemfirePersistentProperty.class);

		when(mockEntity.isConstructorArgument(any(GemfirePersistentProperty.class))).thenReturn(false);
		when(mockProperty.isWritable()).thenReturn(false);

		assertThat(this.pdxSerializer.isWritable(mockEntity, mockProperty)).isFalse();

		verify(mockEntity, times(1)).isConstructorArgument(eq(mockProperty));
		verify(mockProperty, times(1)).isWritable();
		verify(mockProperty, never()).isTransient();
	}

	@Test
	public void isWritableWithTransientPropertyReturnsFalse() {

		GemfirePersistentEntity<?> mockEntity = mock(GemfirePersistentEntity.class);

		GemfirePersistentProperty mockProperty = mock(GemfirePersistentProperty.class);

		when(mockEntity.isConstructorArgument(any(GemfirePersistentProperty.class))).thenReturn(false);
		when(mockProperty.isTransient()).thenReturn(true);
		when(mockProperty.isWritable()).thenReturn(true);

		assertThat(this.pdxSerializer.isWritable(mockEntity, mockProperty)).isFalse();

		verify(mockEntity, times(1)).isConstructorArgument(eq(mockProperty));
		verify(mockProperty, times(1)).isWritable();
		verify(mockProperty, times(1)).isTransient();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void fromDataDeserializesPdxAndMapsToApplicationDomainObject() {

		Address expectedAddress = new Address();

		expectedAddress.street = "100 Main St.";
		expectedAddress.city = "Portland";
		expectedAddress.zipCode = "12345";

		when(mockInstantiator.createInstance(any(GemfirePersistentEntity.class), any(ParameterValueProvider.class)))
			.thenReturn(new Person(null, null, null));
		when(mockReader.readField(eq("id"))).thenReturn(1L);
		when(mockReader.readField(eq("firstname"))).thenReturn("Jon");
		when(mockReader.readField(eq("lastname"))).thenReturn("Doe");
		when(mockAddressSerializer.fromData(eq(Address.class), eq(mockReader))).thenReturn(expectedAddress);

		this.pdxSerializer.setGemfireInstantiators(Collections.singletonMap(Person.class, mockInstantiator));

		Object obj = this.pdxSerializer.fromData(Person.class, mockReader);

		assertThat(obj).isInstanceOf(Person.class);

		Person jonDoe = (Person) obj;

		assertThat(jonDoe.getAddress()).isEqualTo(expectedAddress);
		assertThat(jonDoe.getId()).isEqualTo(1L);
		assertThat(jonDoe.getFirstname()).isEqualTo("Jon");
		assertThat(jonDoe.getLastname()).isEqualTo("Doe");

		verify(mockInstantiator, times(1))
			.createInstance(any(GemfirePersistentEntity.class), any(ParameterValueProvider.class));
		verify(mockReader, times(1)).readField(eq("id"));
		verify(mockReader, times(1)).readField(eq("firstname"));
		verify(mockReader, times(1)).readField(eq("lastname"));
		verify(mockAddressSerializer, times(1)).fromData(eq(Address.class), eq(mockReader));
	}

	@Test(expected = MappingException.class)
	@SuppressWarnings("unchecked")
	public void fromDataHandlesExceptionProperly() {

		when(mockInstantiator.createInstance(any(GemfirePersistentEntity.class), any(ParameterValueProvider.class)))
			.thenReturn(new Person(null, null, null));

		when(mockReader.readField(eq("id"))).thenThrow(newIllegalArgumentException("test"));

		try {
			this.pdxSerializer.setGemfireInstantiators(Collections.singletonMap(Person.class, mockInstantiator));
			this.pdxSerializer.fromData(Person.class, mockReader);
		}
		catch (MappingException expected) {

			assertThat(expected).hasMessage("While setting value [null] of property [id] for entity of type [%s] from PDX", Person.class);
			assertThat(expected).hasCauseInstanceOf(IllegalArgumentException.class);
			assertThat(expected.getCause()).hasMessage("test");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockInstantiator, times(1))
				.createInstance(any(GemfirePersistentEntity.class), any(ParameterValueProvider.class));

			verify(mockReader, times(1)).readField(eq("id"));
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void fromDataUsesRegisteredInstantiator() {

		Address address = new Address();

		address.street = "100 Main St.";
		address.city = "London";
		address.zipCode = "01234";

		Person person = new Person(1L, "Oliver", "Gierke");

		person.address = address;

		when(mockInstantiator.createInstance(any(GemfirePersistentEntity.class), any(ParameterValueProvider.class)))
			.thenReturn(person);

		this.pdxSerializer.setGemfireInstantiators(Collections.singletonMap(Person.class, mockInstantiator));
		this.pdxSerializer.fromData(Person.class, mockReader);

		GemfirePersistentEntity<?> persistentEntity =
			Optional.ofNullable(mappingContext.getPersistentEntity(Person.class)).orElse(null);

		verify(mockInstantiator, times(1))
			.createInstance(eq(persistentEntity), any(ParameterValueProvider.class));

		verify(mockAddressSerializer, times(1))
			.fromData(eq(Address.class), any(PdxReader.class));
	}

	@Test
	public void toDataSerializesApplicationDomainObjectToPdx() {

		Address address = new Address();

		address.street = "100 Main St.";
		address.city = "Portland";
		address.zipCode = "12345";

		Person jonDoe = new Person(1L, "Jon", "Doe");

		jonDoe.address = address;

		this.pdxSerializer.setCustomSerializers(Collections.singletonMap(Address.class, mockAddressSerializer));

		assertThat(this.pdxSerializer.toData(jonDoe, mockWriter)).isTrue();

		verify(mockAddressSerializer, times(1)).toData(eq(address), eq(mockWriter));
		verify(mockWriter, times(1)).writeField(eq("id"), eq(1L), eq(Long.class));
		verify(mockWriter, times(1)).writeField(eq("firstname"), eq("Jon"), eq(String.class));
		verify(mockWriter, times(1)).writeField(eq("lastname"), eq("Doe"), eq(String.class));
		verify(mockWriter, times(1)).markIdentityField(eq("id"));
	}

	@Test(expected = MappingException.class)
	public void toDataHandlesExceptionProperly() {

		Address address = new Address();

		address.street = "100 Main St.";
		address.city = "Portland";
		address.zipCode = "12345";

		Person jonDoe = new Person(1L, "Jon", "Doe");

		jonDoe.address = address;

		when(mockWriter.writeField(eq("address"), eq(address), eq(Address.class)))
			.thenThrow(newIllegalArgumentException("test"));

		try {
			this.pdxSerializer.setCustomSerializers(Collections.emptyMap());
			this.pdxSerializer.toData(jonDoe, mockWriter);
		}
		catch (MappingException expected) {

			assertThat(expected).hasMessage("While serializing entity [%1$s] property [address]"
					+ " value [100 Main St. Portland, 12345] of type [%2$s] to PDX",
				Person.class.getName(), Address.class.getName());
			assertThat(expected).hasCauseInstanceOf(IllegalArgumentException.class);
			assertThat(expected.getCause()).hasMessage("test");
			assertThat(expected.getCause()).hasNoCause();

			throw expected;
		}
		finally {
			verify(mockWriter, atMost(1)).writeField(eq("id"), eq(1L), eq(Long.class));
			verify(mockWriter, atMost(1)).writeField(eq("firstname"), eq("Jon"), eq(String.class));
			verify(mockWriter, atMost(1)).writeField(eq("lastname"), eq("Doe"), eq(String.class));
			verify(mockWriter, times(1)).writeField(eq("address"), eq(address), eq(Address.class));
			verify(mockWriter, never()).markIdentityField(anyString());
		}
	}
}
