package com.rcarausu.ptdma.voiceassistant.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.rcarausu.ptdma.voiceassistant.R;

import java.util.ArrayList;
import java.util.List;

public class TodoActivity extends AppCompatActivity {

    private static final String EMPTY_STRING = "";
    public static final String DATA_TODO_LIST = "DataTodoList";
    private ArrayAdapter arrayAdapter;
    private List<String> todoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        final ListView todoListView = findViewById(R.id.todoList);

        todoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removeTodo(position);
                return true;
            }
        });

        initializeListView(todoListView);

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTodo();
            }
        });

        Button clearButton = findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllToDos();
            }
        });

        Button clearTextButton = findViewById(R.id.clearTextButton);
        clearTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = findViewById(R.id.todoText);
                et.setText(EMPTY_STRING);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menu.removeItem(R.id.todo_item);
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_item:
                startActivity(new Intent(getBaseContext(), MainActivity.class));
                return true;
            case R.id.help_item:
                startActivity(new Intent(getBaseContext(), HelpActivity.class));
                return true;
            default:
                return false;
        }
    }

    private void initializeListView(ListView todoListView) {
        todoList = new ArrayList<>();

        SharedPreferences todoPreferences = getSharedPreferences(DATA_TODO_LIST, MODE_PRIVATE);

        if (todoPreferences.getAll().size() != 0) {
            todoList.addAll(todoPreferences.getAll().keySet());
        }

        arrayAdapter = new ArrayAdapter<>(
                this, R.layout.simple_list_item, todoList);
        todoListView.setAdapter(arrayAdapter);
    }

    private void addTodo() {
        EditText editText = findViewById(R.id.todoText);
        String text = editText.getText().toString();
        if (!text.equals(EMPTY_STRING)) {
            SharedPreferences preferences = getSharedPreferences(DATA_TODO_LIST, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(text, text);
            editor.apply();
            todoList.add(text);
            arrayAdapter.notifyDataSetChanged();
            editText.setText(EMPTY_STRING);
        }
    }

    private void removeTodo(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Are you sure?");
        dialogBuilder.setMessage("This will remove the selected to-do");
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (todoList.size() > 0) {
                    SharedPreferences preferences = getSharedPreferences(DATA_TODO_LIST, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(todoList.get(position));
                    editor.apply();
                    todoList.remove(position);
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", null);
        dialogBuilder.create().show();
    }

    private void clearAllToDos() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Are you sure?");
        dialogBuilder.setMessage("This will clear all the to-do's");
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences preferences = getSharedPreferences(DATA_TODO_LIST, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();
                todoList.clear();
                arrayAdapter.notifyDataSetChanged();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", null);
        dialogBuilder.create().show();
    }

}
