package me.jtalk.android.geotasks.calendar;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;

import com.google.common.collect.Collections2;

import java.util.Arrays;
import java.util.Collections;

import me.jtalk.android.geotasks.util.Joiner;

import static me.jtalk.android.geotasks.util.Assert.*;
import static me.jtalk.android.geotasks.util.CalendarHelper.*;

public class CalendarSource {

	public void initialize(ContentResolver resolver) {
		Uri calendarUri = Calendars.CONTENT_URI;
		String selection = buildSelection(Calendars.ACCOUNT_NAME, Calendars.ACCOUNT_TYPE, Calendars.OWNER_ACCOUNT);
		resolver.query(...)
	}

}
