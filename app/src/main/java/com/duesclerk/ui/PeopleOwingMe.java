package com.duesclerk.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.duesclerk.R;
import com.duesclerk.ui.fragment_peopleowingme.FragmentPeopleOwingMe;

public class PeopleOwingMe extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_owing_me);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, FragmentPeopleOwingMe.newInstance())
                    .commitNow();
        }
    }
}