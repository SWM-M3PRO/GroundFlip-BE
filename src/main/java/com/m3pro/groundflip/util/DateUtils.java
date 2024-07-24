package com.m3pro.groundflip.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;

public class DateUtils {
	public static boolean isDateInCurrentWeek(LocalDate date) {
		return getWeekOfDate(LocalDate.now()) == getWeekOfDate(date);
	}

	public static int getWeekOfDate(LocalDate date) {
		WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
		return date.get(weekFields.weekOfWeekBasedYear());
	}
}
