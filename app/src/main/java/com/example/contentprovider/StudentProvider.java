package com.example.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class StudentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.contentprovider.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/students");

    private static final int STUDENTS = 1;
    private static final int STUDENT_ID = 2;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, "students", STUDENTS);
        URI_MATCHER.addURI(AUTHORITY, "students/#", STUDENT_ID);
    }

    private static final String STUDENTS_DIR_TYPE =
            "vnd.android.cursor.dir/vnd." + AUTHORITY + ".students";
    private static final String STUDENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd." + AUTHORITY + ".students";

    private StudentDBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new StudentDBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(StudentDBHelper.TABLE_STUDENTS);

        switch (URI_MATCHER.match(uri)) {
            case STUDENTS:
                break;
            case STUDENT_ID:
                queryBuilder.appendWhere(StudentDBHelper.COLUMN_ID + "="
                        + ContentUris.parseId(uri));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (URI_MATCHER.match(uri) != STUDENTS) {
            throw new IllegalArgumentException("Invalid URI for insert: " + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = db.insert(StudentDBHelper.TABLE_STUDENTS, null, values);
        if (id > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        throw new android.database.SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        switch (URI_MATCHER.match(uri)) {
            case STUDENTS:
                count = db.update(StudentDBHelper.TABLE_STUDENTS, values,
                        selection, selectionArgs);
                break;
            case STUDENT_ID:
                String idSelection = StudentDBHelper.COLUMN_ID + "="
                        + ContentUris.parseId(uri);
                if (selection != null) {
                    idSelection += " AND (" + selection + ")";
                }
                count = db.update(StudentDBHelper.TABLE_STUDENTS, values,
                        idSelection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;

        switch (URI_MATCHER.match(uri)) {
            case STUDENTS:
                count = db.delete(StudentDBHelper.TABLE_STUDENTS,
                        selection, selectionArgs);
                break;
            case STUDENT_ID:
                String idSelection = StudentDBHelper.COLUMN_ID + "="
                        + ContentUris.parseId(uri);
                if (selection != null) {
                    idSelection += " AND (" + selection + ")";
                }
                count = db.delete(StudentDBHelper.TABLE_STUDENTS,
                        idSelection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case STUDENTS:
                return STUDENTS_DIR_TYPE;
            case STUDENT_ID:
                return STUDENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
}
