package com.example.usingpreferencesnew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText mEditName;
    private EditText mEditAge;
    private CheckBox mCheckMan;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*프레퍼런스에 데이터를 저장한다.*/
        SharedPreferences prefs=getSharedPreferences("person_info",0);
        SharedPreferences.Editor editor=prefs.edit();

        String name=mEditName.getText().toString();
        int age=Integer.parseInt(mEditAge.getText().toString());
        boolean man=mCheckMan.isChecked();

        editor.putString("name",name);
        editor.putInt("age",age);
        editor.putBoolean("man",man);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditName=(EditText)findViewById(R.id.editName);
        mEditAge=(EditText)findViewById(R.id.editAge);
        mCheckMan=(CheckBox)findViewById(R.id.checkMan);

        /*프레퍼런스에서 데이터를 읽어온다.*/
        if(savedInstanceState==null){
            SharedPreferences prefs=getSharedPreferences("person_info",0);

            String name=prefs.getString("name","");
            int age=prefs.getInt("age",0);
            boolean man=prefs.getBoolean("man",false);

            mEditName.setText(name);
            mEditAge.setText(age+"");
            mCheckMan.setChecked(man);
        }

    }
}