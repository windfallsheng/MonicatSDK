package com.windfallsheng.monicat.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.content.SharedPreferencesCompat;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CreateDate: 2018//1/10
 * Author: lzsheng
 * Description: SharedPreferences操作的工具类
 * 使用EasySP的工具类https://github.com/WhiteDG/EasySP
 * todo  待完善：添加对SharedPreferences的Key、Value的加密存储功能；优化逻辑代码
 * Version:
 */
public class SharedPrefUtil {

    private static final String TAG = "SharedPrefUtil";
    private static SharedPrefUtil sSharedPrefUtil;
    private static SharedPreferences sSharedPreferences;
    private static SharedPreferences.Editor sEditor;
    private static SharedPreferencesCompat.EditorCompat editorCompat = SharedPreferencesCompat.EditorCompat.getInstance();
    private static final String DEFAULT_SP_NAME = "sharedPref_config";
    private static final int DEFAULT_INT = 0;
    private static final float DEFAULT_FLOAT = 0.0f;
    private static final String DEFAULT_STRING = "";
    private static final boolean DEFAULT_BOOLEAN = false;
    private static final Set<String> DEFAULT_STRING_SET = new HashSet<>(0);

    private static String mCurSPName = DEFAULT_SP_NAME;
    private static Context mContext;

    private SharedPrefUtil(Context context) {
        this(context, DEFAULT_SP_NAME);
    }

    private SharedPrefUtil(Context context, String spName) {
        mContext = context.getApplicationContext();
        sSharedPreferences = mContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
        sEditor = sSharedPreferences.edit();
        mCurSPName = spName;
    }

    public static SharedPrefUtil init(Context context) {
        if (sSharedPrefUtil == null || !mCurSPName.equals(DEFAULT_SP_NAME)) {
            sSharedPrefUtil = new SharedPrefUtil(context);
        }
        return sSharedPrefUtil;
    }

    public static SharedPrefUtil init(Context context, String spName) {
        if (sSharedPrefUtil == null) {
            sSharedPrefUtil = new SharedPrefUtil(context, spName);
        } else if (!spName.equals(mCurSPName)) {
            sSharedPrefUtil = new SharedPrefUtil(context, spName);
        }
        return sSharedPrefUtil;
    }

    public SharedPrefUtil put(@StringRes int key, Object value) {
        return put(mContext.getString(key), value);
    }

    public SharedPrefUtil put(String key, Object value) {
        if (value instanceof String) {
            sEditor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            sEditor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            sEditor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Float) {
            sEditor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            sEditor.putLong(key, (Long) value);
        } else {
            sEditor.putString(key, value.toString());
        }
        editorCompat.apply(sEditor);
        return sSharedPrefUtil;
    }

    public Object get(@StringRes int key, Object defaultObject) {
        return get(mContext.getString(key), defaultObject);
    }

    public Object get(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return sSharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sSharedPreferences.getInt(key, (int) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sSharedPreferences.getBoolean(key, (boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sSharedPreferences.getFloat(key, (float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sSharedPreferences.getLong(key, (long) defaultObject);
        }
        return null;
    }

    public SharedPrefUtil putInt(String key, int value) {
        sEditor.putInt(key, value);
        editorCompat.apply(sEditor);
        return this;
    }

    public SharedPrefUtil putInt(@StringRes int key, int value) {
        return putInt(mContext.getString(key), value);
    }

    public int getInt(@StringRes int key) {
        return getInt(mContext.getString(key));
    }

    public int getInt(@StringRes int key, int defValue) {
        return getInt(mContext.getString(key), defValue);
    }

    public int getInt(String key) {
        return getInt(key, DEFAULT_INT);
    }


    public int getInt(String key, int defValue) {
        return sSharedPreferences.getInt(key, defValue);
    }

    public SharedPrefUtil putFloat(@StringRes int key, float value) {
        return putFloat(mContext.getString(key), value);
    }

    public SharedPrefUtil putFloat(String key, float value) {
        sEditor.putFloat(key, value);
        editorCompat.apply(sEditor);
        return sSharedPrefUtil;
    }

    public float getFloat(String key) {
        return getFloat(key, DEFAULT_FLOAT);
    }

    public float getFloat(String key, float defValue) {
        return sSharedPreferences.getFloat(key, defValue);
    }

    public float getFloat(@StringRes int key) {
        return getFloat(mContext.getString(key));
    }

    public float getFloat(@StringRes int key, float defValue) {
        return getFloat(mContext.getString(key), defValue);
    }

    public SharedPrefUtil putLong(@StringRes int key, long value) {
        return putLong(mContext.getString(key), value);
    }

    public SharedPrefUtil putLong(String key, long value) {
        sEditor.putLong(key, value);
        editorCompat.apply(sEditor);
        return sSharedPrefUtil;
    }

    public long getLong(String key) {
        return getLong(key, DEFAULT_INT);
    }

    public long getLong(String key, long defValue) {
        return sSharedPreferences.getLong(key, defValue);
    }

    public long getLong(@StringRes int key) {
        return getLong(mContext.getString(key));
    }

    public long getLong(@StringRes int key, long defValue) {
        return getLong(mContext.getString(key), defValue);
    }

    public SharedPrefUtil putString(@StringRes int key, String value) {
        return putString(mContext.getString(key), value);
    }

    public SharedPrefUtil putString(String key, String value) {
        sEditor.putString(key, value);
        editorCompat.apply(sEditor);
        return sSharedPrefUtil;
    }

    public String getString(String key) {
        return getString(key, DEFAULT_STRING);
    }

    public String getString(String key, String defValue) {
        return sSharedPreferences.getString(key, defValue);
    }

    public String getString(@StringRes int key) {
        return getString(mContext.getString(key), DEFAULT_STRING);
    }

    public String getString(@StringRes int key, String defValue) {
        return getString(mContext.getString(key), defValue);
    }

    public SharedPrefUtil putBoolean(@StringRes int key, boolean value) {
        return putBoolean(mContext.getString(key), value);
    }

    public SharedPrefUtil putBoolean(String key, boolean value) {
        sEditor.putBoolean(key, value);
        editorCompat.apply(sEditor);
        return sSharedPrefUtil;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, DEFAULT_BOOLEAN);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sSharedPreferences.getBoolean(key, defValue);
    }

    public boolean getBoolean(@StringRes int key) {
        return getBoolean(mContext.getString(key));
    }

    public boolean getBoolean(@StringRes int key, boolean defValue) {
        return getBoolean(mContext.getString(key), defValue);
    }

    public SharedPrefUtil putStringSet(@StringRes int key, Set<String> value) {
        return putStringSet(mContext.getString(key), value);
    }

    public SharedPrefUtil putStringSet(String key, Set<String> value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            sEditor.putStringSet(key, value);
            editorCompat.apply(sEditor);
        }
        return sSharedPrefUtil;
    }

    public Set<String> getStringSet(String key) {
        return getStringSet(key, DEFAULT_STRING_SET);
    }


    public Set<String> getStringSet(String key, Set<String> defValue) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return sSharedPreferences.getStringSet(key, defValue);
        } else {
            return DEFAULT_STRING_SET;
        }
    }

    public Set<String> getStringSet(@StringRes int key) {
        return getStringSet(mContext.getString(key));
    }

    public Set<String> getStringSet(@StringRes int key, Set<String> defValue) {
        return getStringSet(mContext.getString(key), defValue);
    }


    public boolean contains(String key) {
        return sSharedPreferences.contains(key);
    }

    public boolean contains(@StringRes int key) {
        return contains(mContext.getString(key));
    }

    public Map<String, ?> getAll() {
        return sSharedPreferences.getAll();
    }

    public SharedPrefUtil remove(@StringRes int key) {
        return remove(mContext.getString(key));
    }

    public SharedPrefUtil remove(String key) {
        sEditor.remove(key);
        editorCompat.apply(sEditor);
        return sSharedPrefUtil;
    }

    public SharedPrefUtil clear() {
        sEditor.clear();
        editorCompat.apply(sEditor);
        return sSharedPrefUtil;
    }

    public SharedPreferences getSharedPreferences() {
        return sSharedPreferences;
    }

    /*private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public SharedPrefUtil(Context context, String fileName) {
        preferences = context.getSharedPreferences(fileName, context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    *//**
     * 保存数据的方法，拿到数据保存数据的基本类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     *//*
    public void put(String key, Object object) {
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }
//        SharedPreferencesCompat.apply(editor);
    }

    *//**
     * 获取保存数据的方法，我们根据默认值的到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key           键的值
     * @param defaultObject 默认值
     * @return
     *//*

    public Object get(String key, Object defaultObject) {
        if (defaultObject instanceof String) {
            return sharedPreferences.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sharedPreferences.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sharedPreferences.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sharedPreferences.getLong(key, (Long) defaultObject);
        } else {
            return sharedPreferences.getString(key, null);
        }

    }

    *//**
     * 返回所有的键值对
     *
     * @return
     *//*
    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }


    *//**
     * 向SP存入指定key对应的数据
     * 其中value可以是String、boolean、float、int、long等各种基本类型的值
     *
     * @param key
     * @param value
     *//*
    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void putFloat(String key, float value) {
        editor.putFloat(key, value);
        editor.commit();
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void putLong(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
    }

    *//**
     * 清空SP里所有数据
     *//*
    public void clear() {
        editor.clear();
        editor.commit();
    }

    *//**
     * 删除SP里指定key对应的数据项
     *
     * @param key
     *//*
    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    *//**
     * 获取SP数据里指定key对应的value。如果key不存在，则返回默认值defValue。
     *
     * @param key
     * @param defValue
     * @return
     *//*
    public String getString(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return preferences.getFloat(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return preferences.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return preferences.getLong(key, defValue);
    }

    *//**
     * 判断SP是否包含特定key的数据
     *
     * @param key
     * @return
     *//*
    public boolean contains(String key) {
        return preferences.contains(key);
    }*/
}
