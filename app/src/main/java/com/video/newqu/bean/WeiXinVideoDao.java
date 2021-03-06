package com.video.newqu.bean;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "WEI_XIN_VIDEO".
*/
public class WeiXinVideoDao extends AbstractDao<WeiXinVideo, Long> {

    public static final String TABLENAME = "WEI_XIN_VIDEO";

    /**
     * Properties of entity WeiXinVideo.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property ID = new Property(0, Long.class, "ID", true, "_id");
        public final static Property FileName = new Property(1, String.class, "fileName", false, "FILE_NAME");
        public final static Property VideoPath = new Property(2, String.class, "videoPath", false, "VIDEO_PATH");
        public final static Property VideoCreazeTime = new Property(3, Long.class, "videoCreazeTime", false, "VIDEO_CREAZE_TIME");
        public final static Property VideoDortion = new Property(4, int.class, "videoDortion", false, "VIDEO_DORTION");
        public final static Property IsSelector = new Property(5, boolean.class, "isSelector", false, "IS_SELECTOR");
        public final static Property VidepThbunPath = new Property(6, String.class, "videpThbunPath", false, "VIDEP_THBUN_PATH");
        public final static Property FileKey = new Property(7, String.class, "fileKey", false, "FILE_KEY");
    }


    public WeiXinVideoDao(DaoConfig config) {
        super(config);
    }
    
    public WeiXinVideoDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"WEI_XIN_VIDEO\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: ID
                "\"FILE_NAME\" TEXT," + // 1: fileName
                "\"VIDEO_PATH\" TEXT," + // 2: videoPath
                "\"VIDEO_CREAZE_TIME\" INTEGER," + // 3: videoCreazeTime
                "\"VIDEO_DORTION\" INTEGER NOT NULL ," + // 4: videoDortion
                "\"IS_SELECTOR\" INTEGER NOT NULL ," + // 5: isSelector
                "\"VIDEP_THBUN_PATH\" TEXT," + // 6: videpThbunPath
                "\"FILE_KEY\" TEXT);"); // 7: fileKey
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"WEI_XIN_VIDEO\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, WeiXinVideo entity) {
        stmt.clearBindings();
 
        Long ID = entity.getID();
        if (ID != null) {
            stmt.bindLong(1, ID);
        }
 
        String fileName = entity.getFileName();
        if (fileName != null) {
            stmt.bindString(2, fileName);
        }
 
        String videoPath = entity.getVideoPath();
        if (videoPath != null) {
            stmt.bindString(3, videoPath);
        }
 
        Long videoCreazeTime = entity.getVideoCreazeTime();
        if (videoCreazeTime != null) {
            stmt.bindLong(4, videoCreazeTime);
        }
        stmt.bindLong(5, entity.getVideoDortion());
        stmt.bindLong(6, entity.getIsSelector() ? 1L: 0L);
 
        String videpThbunPath = entity.getVidepThbunPath();
        if (videpThbunPath != null) {
            stmt.bindString(7, videpThbunPath);
        }
 
        String fileKey = entity.getFileKey();
        if (fileKey != null) {
            stmt.bindString(8, fileKey);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, WeiXinVideo entity) {
        stmt.clearBindings();
 
        Long ID = entity.getID();
        if (ID != null) {
            stmt.bindLong(1, ID);
        }
 
        String fileName = entity.getFileName();
        if (fileName != null) {
            stmt.bindString(2, fileName);
        }
 
        String videoPath = entity.getVideoPath();
        if (videoPath != null) {
            stmt.bindString(3, videoPath);
        }
 
        Long videoCreazeTime = entity.getVideoCreazeTime();
        if (videoCreazeTime != null) {
            stmt.bindLong(4, videoCreazeTime);
        }
        stmt.bindLong(5, entity.getVideoDortion());
        stmt.bindLong(6, entity.getIsSelector() ? 1L: 0L);
 
        String videpThbunPath = entity.getVidepThbunPath();
        if (videpThbunPath != null) {
            stmt.bindString(7, videpThbunPath);
        }
 
        String fileKey = entity.getFileKey();
        if (fileKey != null) {
            stmt.bindString(8, fileKey);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public WeiXinVideo readEntity(Cursor cursor, int offset) {
        WeiXinVideo entity = new WeiXinVideo( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // ID
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // fileName
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // videoPath
            cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // videoCreazeTime
            cursor.getInt(offset + 4), // videoDortion
            cursor.getShort(offset + 5) != 0, // isSelector
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // videpThbunPath
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7) // fileKey
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, WeiXinVideo entity, int offset) {
        entity.setID(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setFileName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setVideoPath(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setVideoCreazeTime(cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3));
        entity.setVideoDortion(cursor.getInt(offset + 4));
        entity.setIsSelector(cursor.getShort(offset + 5) != 0);
        entity.setVidepThbunPath(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setFileKey(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(WeiXinVideo entity, long rowId) {
        entity.setID(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(WeiXinVideo entity) {
        if(entity != null) {
            return entity.getID();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(WeiXinVideo entity) {
        return entity.getID() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
