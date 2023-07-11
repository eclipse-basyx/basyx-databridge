/*******************************************************************************
 * Copyright (C) 2023 the Eclipse BaSyx Authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/
package org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration.deserializer.DeserializationException;
import org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration.deserializer.OptionDeserializer;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Tests the behavior of {@link OptionDeserializer}
 * 
 * @author danish
 *
 */
public class TestOptionDeserializer {

	private static final String SINGLE_OPTION_STRING_TYPE = "option1=value1";
	private static final String MULTIPLE_OPTIONS = "option1=value1&option2=value2&option3=value3";
	private static final String INVALID_OPTION = "option1:value1&option2=value2";
	private static final Type OPTION_LIST_TYPE = new TypeToken<Object>() {}.getType();
	private static final Object SINGLE_OPTION_OBJECT_TYPE = new Gson().fromJson("[\r\n"
			+ "			{\r\n"
			+ "				\"name\": \"option4\",\r\n"
			+ "				\"value\": \"value4\"\r\n"
			+ "			}\r\n"
			+ "		]", OPTION_LIST_TYPE);

	@Test
	public void deserializeSingleStringTypeOption() {
		List<Option> expectedOptions = new ArrayList<>(Arrays.asList(new Option("option1", "value1")));

		List<Option> actualOptions = new OptionDeserializer().deserialize(SINGLE_OPTION_STRING_TYPE);

		assertEqualOptions(expectedOptions, actualOptions);
	}
	
	@Test
	public void deserializeSingleObjectTypeOption() {
		List<Option> expectedOptions = new ArrayList<>(Arrays.asList(new Option("option4", "value4")));
		
		List<Option> actualOptions = new OptionDeserializer().deserialize(SINGLE_OPTION_OBJECT_TYPE);
		
		assertEqualOptions(expectedOptions, actualOptions);
	}

	@Test
	public void deserializeMultipleOptions() {
		List<Option> expectedOptions = new ArrayList<>(Arrays.asList(new Option("option1", "value1"),
				new Option("option2", "value2"), new Option("option3", "value3")));

		List<Option> actualOptions = new OptionDeserializer().deserialize(MULTIPLE_OPTIONS);

		assertEqualOptions(expectedOptions, actualOptions);
	}

	@Test
	public void deserializeEmptyOption() {
		List<Option> expectedOptions = new ArrayList<>();

		List<Option> actualOptions = new OptionDeserializer().deserialize(StringUtils.EMPTY);

		assertEqualOptions(expectedOptions, actualOptions);
	}

	@Test(expected = DeserializationException.class)
	public void deserializeInvalidOption() {
		new OptionDeserializer().deserialize(INVALID_OPTION);
	}

	private void assertEqualOptions(List<Option> expectedOptions, List<Option> actualOptions) {
		assertEquals(expectedOptions.size(), actualOptions.size());

		assertTrue(expectedOptions.containsAll(actualOptions));
		assertTrue(actualOptions.containsAll(expectedOptions));
	}

}
