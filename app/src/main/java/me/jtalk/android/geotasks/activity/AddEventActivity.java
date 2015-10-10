package me.jtalk.android.geotasks.activity;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;

import me.jtalk.android.geotasks.R;

public class AddEventActivity extends Activity {
    public static final String EXTRA_TITLE = "event-name";
    public static final String EXTRA_DESCRIPTION = "event-description";

    private TextView nameText;
    private TextView descriptionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        nameText = (TextView) findViewById(R.id.eventNameText);
        descriptionText = (TextView) findViewById(R.id.eventDescriptionText);

        Button addCalendarButton = (Button) findViewById(R.id.eventAddButton);
        addCalendarButton.setOnClickListener(view -> {
            Intent returnIntent = new Intent(AddEventActivity.this, MainActivity.class);
            returnIntent.putExtra(EXTRA_TITLE, AddEventActivity.this.nameText.getText().toString());
            returnIntent.putExtra(EXTRA_DESCRIPTION, AddEventActivity.this.descriptionText.getText().toString());
            AddEventActivity.this.setResult(RESULT_OK, returnIntent);
            AddEventActivity.this.finish();
        });
    }
}
