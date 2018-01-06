package portfolio.projects.mrkimkim.ai_interview.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by 킹조 on 2018-01-05.
 */

public class DBHelper extends SQLiteOpenHelper {
    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 사용자 관리 테이블 UserInfo
        db.execSQL("CREATE TABLE UserInfo (idx INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "user_nickname TEXT," +
                "user_email TEXT," +
                "user_msg TEXT," +
                "user_img TEXT," +
                "user_numtry INTEGER," +
                "user_upvote INTEGER," +
                "user_credit INTEGER);");
    }

    public void insert(String table_name, String[] i_column, String[] i_value, String[] w_column, String[] w_value) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        for(int i = 0; i < i_column.length; ++i) {
            values.put(i_column[i], i_value[i]);
        }
        db.insert(table_name, null, values);

    }

    public ContentValues[] select(String table_name, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(table_name, columns, selection, selectionArgs, groupBy, having, orderBy);

        int i = 0;
        int row_cnt = c.getCount();
        ContentValues[] values = new ContentValues[row_cnt];
        while (c.moveToNext()) {
            values[i] = new ContentValues();
            for(int j = 0; j < columns.length; ++j) {
                values[i].put(columns[j], c.getString(j));
            }
            i += 1;
        }
        Log.d("DB SELECT : ", String.valueOf(values.length) + "rows are selected");
        return values;
    }

    public void update(String table_name, String[] u_column, String[] u_value, String selection, String[] selectionArgs) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        for(int i = 0; i < u_column.length; ++i) values.put(u_column[i], u_value[i]);
        db.update(table_name, values, selection, selectionArgs);
    }

    public void delete(String table_name, String selection, String[] selectionArgs) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(table_name, selection, selectionArgs);
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
