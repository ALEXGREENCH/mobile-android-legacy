package uk.openvk.android.legacy.cache;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.services.AudioPlayerService;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class AudioCacheDB {
    public static Vector<String> cachedIDs = new Vector<>();
    public static Vector<String> cacheReqs = new Vector<>();
    private static Context ctx;

    public static class CacheOpenHelper extends SQLiteOpenHelper {

        public CacheOpenHelper(Context ctx, String db_name) {
            super(ctx, db_name, null, 1);
        }

        public CacheOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            CacheDatabaseTables.createAudioTracksTable(database);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVer, int newVer) {
            if (oldVer == 1 && newVer >= oldVer) {
                // TODO: Add database auto-upgrade to new versions
                return;
            }
            onCreate(database);
        }
    }

    public void putTrack(Context ctx, Audio track, boolean forced) {
        CacheOpenHelper helper2 = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx));
        SQLiteDatabase db2 = helper2.getWritableDatabase();
        if (!isExist(ctx, track.id)) {
            if (!cachedIDs.contains(String.valueOf(track.owner_id) + "_" + track.id)) {
                cachedIDs.add(String.valueOf(track.owner_id) + "_" + track.id);
            }
            Intent intent = new Intent(AudioPlayerService.ACTION_UPDATE_PLAYLIST);
            ctx.getApplicationContext().sendBroadcast(intent);
            try {
                ContentValues values2 = new ContentValues();
                values2.put("audio_id", track.id);
                values2.put("owner_id", track.owner_id);
                values2.put("artist", track.artist);
                values2.put("duration", track.getDurationInSeconds());
                values2.put("lastplay", System.currentTimeMillis() / 1000);
                values2.put("user", forced);
                values2.put("lyrics", track.lyrics);
                db2.insert("tracks", null, values2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (forced) {
            CacheOpenHelper helper = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx));
            SQLiteDatabase db = helper.getWritableDatabase();
            try {
                ContentValues values = new ContentValues();
                values.put("user", true);
                db.update("tracks", values,
                        "audio_id=" + track.id
                                + " and owner_id=" + track.owner_id,
                        null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        db2.close();
        helper2.close();
    }

    private boolean isExist(Context ctx, long track_id) {
        getCurrentDatabasePath(ctx);
        File dir = getCurrentDatabasePath(ctx);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        boolean result = false;
        CacheOpenHelper helper = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx));
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Cursor cursor = db.query("tracks", new String[]{"count(*)"},
                    "`audio_id`=" + track_id,
                    null, null, null, null);
            result = cursor.getCount() > 0 && cursor.moveToFirst() && cursor.getInt(0) > 0;
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        helper.close();
        return result;
    }

    public static File getCurrentDatabasePath(Context ctx) {
        OvkApplication app = (OvkApplication) ctx.getApplicationContext();
        String instance = app.getCurrentInstance();
        long user_id = app.getCurrentUserId();
        return ctx.getDatabasePath(String.format("audio_%s_a%s.db", instance, user_id));
    }

    public static String getCurrentDatabaseName(Context ctx) {
        OvkApplication app = (OvkApplication) ctx.getApplicationContext();
        String instance = app.getCurrentInstance();
        long user_id = app.getCurrentUserId();
        return String.format("audio_%s_a%s.db", instance, user_id);
    }

    public static void fillDatabase(Context ctx2, ArrayList<Audio> audios, boolean clear) {
        CacheOpenHelper helper = new CacheOpenHelper(ctx2, getCurrentDatabaseName(ctx2));
        Cursor cursor = null;
        SQLiteDatabase db = helper.getWritableDatabase();
        if(clear) {
            cursor = db.query("tracks", new String[]{"owner_id", "audio_id"},
                    null, null, null, null, null);
            cursor.moveToFirst();
            cachedIDs.clear();
        }
        for(int i = 0; i < audios.size(); i++) {
            Audio track = audios.get(i);
            ContentValues values = new ContentValues();
            values.put("audio_id", track.id);
            values.put("owner_id", track.owner_id);
            values.put("title", track.title);
            values.put("artist", track.artist);
            values.put("duration", track.getDurationInSeconds());
            values.put("lastplay", 0);
            values.put("user", true);
            values.put("lyrics", track.lyrics != null ? track.lyrics : "");
            values.put("url", track.url);
            values.put("status", track.status);
            db.insert("tracks", null, values);
            String track_name = String.format("%s_%s", track.id, track.owner_id);
            cachedIDs.add(track_name);
        }
        db.close();
        helper.close();
        if (cursor != null) {
            cursor.close();
        }
    }

    public static ArrayList<Audio> getCachedAudiosList(Context ctx2) {
        CacheOpenHelper helper = new CacheOpenHelper(ctx2, getCurrentDatabaseName(ctx2));
        SQLiteDatabase db = helper.getWritableDatabase();
        ArrayList<Audio> list = new ArrayList<>();
        try {
            Cursor cursor = db.query("tracks", null, null,
                    null, null, null, "user desc");
            cursor.moveToFirst();
            int i = 0;
            do {
                Audio track = new Audio();
                track.sender = new User();
                track.sender.id = cursor.getInt(0);
                track.id = cursor.getInt(1);
                track.title = cursor.getString(2);
                track.artist = cursor.getString(3);
                track.setDuration(cursor.getInt(4));
                track.lyrics = cursor.getString(7);
                list.add(track);
                i++;
            } while (cursor.moveToNext());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        helper.close();
        ctx = ctx2;
        deleteOldTrack(ctx);
        return list;
    }

    private static void deleteOldTrack(Context ctx) {
        CacheOpenHelper helper = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx));
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            Cursor cursor = db.query("tracks",
                    null, "user=0", null, null, null,
                    "lastplay asc");
            cursor.moveToFirst();
            if (cursor.getCount() > 10) {
                int nDel = cursor.getCount() - 10;
                String where = "";
                for (int i = 0; i < nDel; i++) {
                    where = String.valueOf(where) + "or(owner_id=" + cursor.getInt(0) +
                            " AND audio_id=" + cursor.getInt(1) + ")";
                    cachedIDs.remove(String.valueOf(cursor.getInt(0)) + "_" + cursor.getInt(1));
                    cursor.moveToNext();
                }
                cursor.close();
                db.delete("tracks", where.substring(2), null);
                Intent intent = new Intent(AudioPlayerService.ACTION_UPDATE_PLAYLIST);
                intent.putExtra("reload_cached_list", true);
                ctx.getApplicationContext().sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        helper.close();
    }

    public static void clear(Context ctx) {
        CacheOpenHelper helper = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx));
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.delete("tracks", null, null);
            cachedIDs.clear();
            Intent intent = new Intent(AudioPlayerService.ACTION_UPDATE_PLAYLIST);
            intent.putExtra("reload_cached_list", true);
            ctx.getApplicationContext().sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        helper.close();
    }

    public static void updatePlayTime(Context ctx, int owner_id, int audio_id) {
        CacheOpenHelper helper = new CacheOpenHelper(ctx, getCurrentDatabaseName(ctx));
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("lastplay", (System.currentTimeMillis() / 1000));
            db.update("files", values,
                    "audio_id=" + audio_id + " and owner_id=" + owner_id,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        helper.close();
    }
}
