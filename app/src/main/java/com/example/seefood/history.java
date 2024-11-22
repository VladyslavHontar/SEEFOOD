package com.example.seefood;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class history extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<File> imageFiles;
    private List<File> filteredImageFiles;
    private ViewFlipper viewFlipper;
    private GestureDetector gestureDetector;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        imageFiles = loadImages();
        filteredImageFiles = new ArrayList<>(imageFiles);
        imageAdapter = new ImageAdapter(this, filteredImageFiles);
        recyclerView.setAdapter(imageAdapter);

        viewFlipper = findViewById(R.id.view_flipper);
        gestureDetector = new GestureDetector(this, new GestureListener());

        viewFlipper.setInAnimation(this, R.anim.slide_in_right);
        viewFlipper.setOutAnimation(this, R.anim.slide_out_left);

        Button clearHistoryButton = findViewById(R.id.clear_history_button);
        clearHistoryButton.setOnClickListener(v -> clearHistory());

        Button filterByDateButton = findViewById(R.id.filter_by_date_button);
        filterByDateButton.setOnClickListener(v -> showDatePickerDialog());

        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);
        // Set current date as the selected date
        Calendar calendar = Calendar.getInstance();
        long currentDateInMillis = calendar.getTimeInMillis();
        saveSelectedDate(currentDateInMillis);
        filterImagesByDate(currentDateInMillis);
    }

    private List<File> loadImages() {
        List<File> images = new ArrayList<>();
        File directory = getExternalFilesDir(null);
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".jpg")) {
                        images.add(file);
                    }
                }
                images.sort((file1, file2) -> Long.compare(file2.lastModified(), file1.lastModified()));
            }
        }
        return images;
    }

    private void clearHistory() {
        File directory = getExternalFilesDir(null);
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".jpg")) {
                        file.delete();
                    }
                }
            }
        }
        imageFiles.clear();
        filteredImageFiles.clear();
        imageAdapter.notifyDataSetChanged();
        Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        long savedDateInMillis = sharedPreferences.getLong("selectedDate", -1);
        if (savedDateInMillis != -1) {
            calendar.setTimeInMillis(savedDateInMillis);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            long selectedDateInMillis = calendar.getTimeInMillis();
            filterImagesByDate(selectedDateInMillis);
            saveSelectedDate(selectedDateInMillis);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void filterImagesByDate(long selectedDateInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String selectedDate = sdf.format(selectedDateInMillis);

        filteredImageFiles.clear();
        for (File file : imageFiles) {
            String fileDate = sdf.format(file.lastModified());
            if (fileDate.equals(selectedDate)) {
                filteredImageFiles.add(file);
            }
        }
        imageAdapter.notifyDataSetChanged();
    }

    private void saveSelectedDate(long selectedDateInMillis) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("selectedDate", selectedDateInMillis);
        editor.apply();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        viewFlipper.setInAnimation(history.this, R.anim.slide_in_left);
                        viewFlipper.setOutAnimation(history.this, R.anim.slide_out_right);
                        viewFlipper.showPrevious();
                    } else {
                        viewFlipper.setInAnimation(history.this, R.anim.slide_in_right);
                        viewFlipper.setOutAnimation(history.this, R.anim.slide_out_left);
                        viewFlipper.showNext();
                    }
                    return true;
                }
            }
            return false;
        }
    }
}