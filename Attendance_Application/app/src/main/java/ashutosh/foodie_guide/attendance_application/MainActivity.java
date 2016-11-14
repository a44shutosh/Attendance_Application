package ashutosh.foodie_guide.attendance_application;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //this method has to be public and the associated view is the view that called it
    public void moveToNext(View view){
        Intent intent=new Intent(this, ActivityCourseList.class);
        startActivity(intent);
    }
}
