package com.example.contentprovider;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ContentProvider";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Xóa toàn bộ dữ liệu cũ
        getContentResolver().delete(StudentProvider.CONTENT_URI, null, null);
        Log.d(TAG, "Deleted all existing students");

        // 2. Thêm mới 3 sinh viên
        insertStudent("Nguyen Van An", 20, "CNTT");
        insertStudent("Tran Thi Binh", 21, "Kế toán");
        insertStudent("Le Hoang Cuong", 19, "ĐTVT");

        // 3. Đọc và in danh sách lần 1 (sắp xếp theo tên)
        Log.d(TAG, "=== Student list (after insert) ===");
        queryAllStudents();

        // 4. Cập nhật sinh viên ID = 1
        updateStudent(1, "Nguyen Van An (Updated)", 21, "KTMT");

        // 5. Đọc lại danh sách lần 2
        Log.d(TAG, "=== Student list (after update) ===");
        queryAllStudents();

        // 6. Xóa sinh viên ID = 2
        deleteStudent(2);

        // 7. Đọc lại danh sách lần cuối
        Log.d(TAG, "=== Student list (after delete) ===");
        queryAllStudents();
    }

    private Uri insertStudent(String name, int age, String major) {
        ContentValues values = new ContentValues();
        values.put(StudentDBHelper.COLUMN_NAME, name);
        values.put(StudentDBHelper.COLUMN_AGE, age);
        values.put(StudentDBHelper.COLUMN_MAJOR, major);

        Uri uri = getContentResolver().insert(StudentProvider.CONTENT_URI, values);
        Log.d(TAG, "Inserted student: " + name + " -> " + uri);
        return uri;
    }

    private void queryAllStudents() {
        String[] projection = {
                StudentDBHelper.COLUMN_ID,
                StudentDBHelper.COLUMN_NAME,
                StudentDBHelper.COLUMN_AGE,
                StudentDBHelper.COLUMN_MAJOR
        };

        try (Cursor cursor = getContentResolver().query(
                StudentProvider.CONTENT_URI,
                projection,
                null,
                null,
                StudentDBHelper.COLUMN_NAME + " ASC")) {

            if (cursor == null) {
                Log.d(TAG, "Query returned null cursor");
                return;
            }

            if (cursor.getCount() == 0) {
                Log.d(TAG, "No students found");
                return;
            }

            int idIndex = cursor.getColumnIndexOrThrow(StudentDBHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(StudentDBHelper.COLUMN_NAME);
            int ageIndex = cursor.getColumnIndexOrThrow(StudentDBHelper.COLUMN_AGE);
            int majorIndex = cursor.getColumnIndexOrThrow(StudentDBHelper.COLUMN_MAJOR);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                int age = cursor.getInt(ageIndex);
                String major = cursor.getString(majorIndex);
                Log.d(TAG, "ID=" + id + ", Name=" + name
                        + ", Age=" + age + ", Major=" + major);
            }
        }
    }

    private int updateStudent(long id, String name, int age, String major) {
        ContentValues values = new ContentValues();
        values.put(StudentDBHelper.COLUMN_NAME, name);
        values.put(StudentDBHelper.COLUMN_AGE, age);
        values.put(StudentDBHelper.COLUMN_MAJOR, major);

        Uri studentUri = Uri.withAppendedPath(StudentProvider.CONTENT_URI, String.valueOf(id));
        int rows = getContentResolver().update(studentUri, values, null, null);
        Log.d(TAG, "Updated student ID=" + id + ", rows affected=" + rows);
        return rows;
    }

    private int deleteStudent(long id) {
        Uri studentUri = Uri.withAppendedPath(StudentProvider.CONTENT_URI, String.valueOf(id));
        int rows = getContentResolver().delete(studentUri, null, null);
        Log.d(TAG, "Deleted student ID=" + id + ", rows affected=" + rows);
        return rows;
    }
}
