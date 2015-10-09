package me.jtalk.android.geotasks.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import me.jtalk.android.geotasks.R;

public class AddCalendarActivity extends Activity {

    public static final String EXTRA_NAME = "calendar-name";

    TextView calendarNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calendar);

        calendarNameText = (TextView) findViewById(R.id.calendarNameText);

        Button addCalendarButton = (Button) findViewById(R.id.calendarAddButton);
        addCalendarButton.setOnClickListener(view -> {
            Intent returnIntent = new Intent(AddCalendarActivity.this, MainActivity.class);
            returnIntent.putExtra(EXTRA_NAME, AddCalendarActivity.this.calendarNameText.getText().toString());
            AddCalendarActivity.this.setResult(RESULT_OK, returnIntent);
            AddCalendarActivity.this.finish();
        });
    }
}
