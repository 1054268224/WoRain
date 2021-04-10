package com.example.activitytest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class FistActivity extends AppCompatActivity {

    private List<Fruit> fruitList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);
        initFruits();
        FruitAdapter fruitAdapter = new FruitAdapter(FistActivity.this,R.layout.listview_item,fruitList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(fruitAdapter);
       /* Button button1 = (Button)findViewById(R.id.button_1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FistActivity.this,SecondActivity.class);
                startActivity(intent);
            }
        });*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.add_item:
                Toast.makeText(this,"You Click Add",Toast.LENGTH_SHORT).show();
                break;
            case R.id.remove_item:
                Toast.makeText(this,"You Click Revove",Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

    private void initFruits(){
       for (int i = 0; i<5; i++){
           Fruit Apple = new Fruit("apple",R.drawable.ic_launcher_background);
           fruitList.add(Apple);
           Fruit pear = new Fruit("pear",R.drawable.ic_launcher_background);
           fruitList.add(pear);
           Fruit pineapple = new Fruit("pineapple",R.drawable.ic_launcher_background);
           fruitList.add(pineapple);
           Fruit banana = new Fruit("banana",R.drawable.ic_launcher_background);
           fruitList.add(banana);

       }
    }
}
