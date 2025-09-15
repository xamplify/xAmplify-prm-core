package com.xtremand.util.dto;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NumberFormatterString {

	private NumberFormatterString() {

	}

	public static String formatValueInTrillionsOrBillions(Double inputValue) {
		if (inputValue != null && inputValue > 0) {
			double value = inputValue.doubleValue();
			int power;
			String suffix = " KMBT";
			String formattedNumber = "";

			NumberFormat formatter = new DecimalFormat("#,###.#");
			power = (int) StrictMath.log10(value);
			double b = ((power / 3) * 3);
			value = value / (Math.pow(10, b));
			formattedNumber = formatter.format(value);
			int characterPosition = power / 3;
			int updatedCharacterPosition = characterPosition > 4 ? 4 : characterPosition;
			formattedNumber = formattedNumber + suffix.charAt(updatedCharacterPosition);
			return formattedNumber.length() > 4 ? formattedNumber.replaceAll("\\.[0-9]+", "") : formattedNumber;
		} else {
			return "0";
		}

	}

}
