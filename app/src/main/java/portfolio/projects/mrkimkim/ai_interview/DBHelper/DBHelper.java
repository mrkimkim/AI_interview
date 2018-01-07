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
    private static final String DATABASE_NAME = "AI.db";
    private static final int DATABASE_VERSION = 1;
    public static DBHelper instance;

    public static String[] column_category = {"idx","parent_idx","title","description","n_subcategory","n_probset","n_problem"};
    public static String[] column_questioninfo = {"idx","category_idx","probset_idx","title","description","sub_chart","sub_text","duration","language"};
    SQLiteDatabase db;



    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 앱 정보 테이블 App
        db.execSQL("CREATE TABLE App (appVC INTEGER, " +
                    "appVN STRING," +
                    "dbVC INTEGER," +
                    "dbVN STRING);");

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

        // 카테고리 관리 테이블
        db.execSQL("CREATE TABLE Category (idx INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "parent_idx INTEGER, " +
                "title STRING, " +
                "description STRING, " +
                "n_subcategory INTEGER, " +
                "n_probset INTEGER, " +
                "n_problem INTEGER);");

        // 문제 관리 테이블
        db.execSQL("CREATE TABLE Problems (idx INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "category_idx INTEGER, " +
                "probset_idx INTEGER, " +
                "title STRING, " +
                "description STRING, " +
                "sub_chart STRING, " +
                "sub_text STRING, " +
                "duration INTEGER, " +
                "language INT);");
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
        try {
            db = getReadableDatabase();
            Cursor c = db.query(table_name, columns, selection, selectionArgs, groupBy, having, orderBy);
            int i = 0;
            int row_cnt = c.getCount();

            if (row_cnt < 1) return null;
            else {
                ContentValues[] values = new ContentValues[row_cnt];

                while (c.moveToNext()) {
                    values[i] = new ContentValues();
                    for (int j = 0; j < columns.length; ++j) {
                        values[i].put(columns[j], c.getString(j));
                    }
                    i += 1;
                }
                Log.d("DB SELECT : ", String.valueOf(values.length) + "rows are selected");
                db.close();
                return values;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void update(String table_name, String[] u_column, String[] u_value, String selection, String[] selectionArgs) {
        db = getWritableDatabase();
        ContentValues values = new ContentValues();
        for(int i = 0; i < u_column.length; ++i) values.put(u_column[i], u_value[i]);
        db.update(table_name, values, selection, selectionArgs);
    }

    public void delete(String table_name, String selection, String[] selectionArgs) {
        db = getWritableDatabase();
        db.execSQL("delete from " + table_name);
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void cleanTable(String tableName) {
        try {
            db = getWritableDatabase();
            Log.d("TableName : ", tableName);
            db.execSQL("delete from " + tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getDBVersion() {
        int dbVersion = -1;
        ContentValues[] values = select("App", new String[]{"dbVC"}, null, null, null, null, null);
        if (values != null) {
            dbVersion = values[0].getAsInteger("dbVC");
        }
        return dbVersion;
    }

    public boolean updateCategory(String[] csv) {
        // 기존 테이블 데이터
        cleanTable("Category");
        cleanTable("Problems");
        try {
            Log.d("CSV LENGTH : ", String.valueOf(csv.length));
            // 새로운 테이블 데이터를 삽입
            for (int i = 0; i < csv.length; ++i) {
                Log.d("CSV : ", csv[i]);
                String[] value = csv[i].split(",");
                // Category 데이터
                if (value.length == 7) {
                    this.insert("Category", column_category, value, null, null);
                } // QuestionInfo 데이터
                else if (value.length == 9) {
                    this.insert("Problems", column_questioninfo, value, null, null);
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
