package com.pulse.player.data.local.db.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.pulse.player.data.local.db.entity.ErrorLogEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ErrorLogDao_Impl implements ErrorLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ErrorLogEntity> __insertionAdapterOfErrorLogEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAllLogs;

  private final SharedSQLiteStatement __preparedStmtOfPruneOldLogs;

  public ErrorLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfErrorLogEntity = new EntityInsertionAdapter<ErrorLogEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `error_logs` (`id`,`timestamp`,`level`,`tag`,`message`,`stackTrace`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ErrorLogEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindString(3, entity.getLevel());
        statement.bindString(4, entity.getTag());
        statement.bindString(5, entity.getMessage());
        if (entity.getStackTrace() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getStackTrace());
        }
      }
    };
    this.__preparedStmtOfClearAllLogs = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM error_logs";
        return _query;
      }
    };
    this.__preparedStmtOfPruneOldLogs = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM error_logs WHERE id NOT IN (SELECT id FROM error_logs ORDER BY timestamp DESC LIMIT 500)";
        return _query;
      }
    };
  }

  @Override
  public Object insertLog(final ErrorLogEntity log, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfErrorLogEntity.insertAndReturnId(log);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllLogs(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllLogs.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllLogs.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object pruneOldLogs(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfPruneOldLogs.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfPruneOldLogs.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ErrorLogEntity>> getAllLogs() {
    final String _sql = "SELECT * FROM error_logs ORDER BY timestamp DESC LIMIT 500";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"error_logs"}, new Callable<List<ErrorLogEntity>>() {
      @Override
      @NonNull
      public List<ErrorLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfStackTrace = CursorUtil.getColumnIndexOrThrow(_cursor, "stackTrace");
          final List<ErrorLogEntity> _result = new ArrayList<ErrorLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ErrorLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpLevel;
            _tmpLevel = _cursor.getString(_cursorIndexOfLevel);
            final String _tmpTag;
            _tmpTag = _cursor.getString(_cursorIndexOfTag);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final String _tmpStackTrace;
            if (_cursor.isNull(_cursorIndexOfStackTrace)) {
              _tmpStackTrace = null;
            } else {
              _tmpStackTrace = _cursor.getString(_cursorIndexOfStackTrace);
            }
            _item = new ErrorLogEntity(_tmpId,_tmpTimestamp,_tmpLevel,_tmpTag,_tmpMessage,_tmpStackTrace);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<ErrorLogEntity>> getLogsByLevel(final String level) {
    final String _sql = "SELECT * FROM error_logs WHERE level = ? ORDER BY timestamp DESC LIMIT 500";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, level);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"error_logs"}, new Callable<List<ErrorLogEntity>>() {
      @Override
      @NonNull
      public List<ErrorLogEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "level");
          final int _cursorIndexOfTag = CursorUtil.getColumnIndexOrThrow(_cursor, "tag");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfStackTrace = CursorUtil.getColumnIndexOrThrow(_cursor, "stackTrace");
          final List<ErrorLogEntity> _result = new ArrayList<ErrorLogEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ErrorLogEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpLevel;
            _tmpLevel = _cursor.getString(_cursorIndexOfLevel);
            final String _tmpTag;
            _tmpTag = _cursor.getString(_cursorIndexOfTag);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final String _tmpStackTrace;
            if (_cursor.isNull(_cursorIndexOfStackTrace)) {
              _tmpStackTrace = null;
            } else {
              _tmpStackTrace = _cursor.getString(_cursorIndexOfStackTrace);
            }
            _item = new ErrorLogEntity(_tmpId,_tmpTimestamp,_tmpLevel,_tmpTag,_tmpMessage,_tmpStackTrace);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
