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
package org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration.deserializer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.digitaltwin.basyx.databridge.plc4x.configuration.Option;

/**
 * Custom deserializer for {@link Option}
 * 
 * @author danish
 *
 */
public class OptionDeserializer {

	private static final String OPTION_DELIMITER = "&";
	private static final String OPTION_VALUE_SEPARATOR = "=";

	/**
	 * Deserializes the provided serialized option string into list of {@link Option}
	 * 
	 * @param serializedOption
	 * @return list of Option
	 * 
	 * @throws DeserializationException
	 */
	public List<Option> deserialize(String serializedOption) throws DeserializationException {
		List<Option> optionsList = new ArrayList<>();

		if (serializedOption == null || serializedOption.isEmpty())
			return optionsList;

		String[] options = serializedOption.split(OPTION_DELIMITER);

		for (String option : options) {
			optionsList.add(createOption(option));
		}

		return optionsList;
	}

	private Option createOption(String option) {
		String[] optionPart = option.split(OPTION_VALUE_SEPARATOR);

		if (optionPart.length != 2)
			throw new DeserializationException("Unable to deserialize option. Invalid option string provided '" + option
					+ "'. Make sure that option and its value is separated by '=' and multiple options are separated by '&'");

		return new Option(optionPart[0], optionPart[1]);
	}

}
