package com.aerofocus.app.data.db.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.aerofocus.app.data.db.entity.FocusSessionEntity;
import com.aerofocus.app.data.db.entity.UnlockedDestinationEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class AeroFocusDao_Impl implements AeroFocusDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FocusSessionEntity> __insertionAdapterOfFocusSessionEntity;

  private final EntityInsertionAdapter<UnlockedDestinationEntity> __insertionAdapterOfUnlockedDestinationEntity;

  private final SharedSQLiteStatement __preparedStmtOfUnlockDestination;

  private final SharedSQLiteStatement __preparedStmtOfUnlockEligibleDestinations;

  public AeroFocusDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFocusSessionEntity = new EntityInsertionAdapter<FocusSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `focus_sessions` (`sessionId`,`startTime`,`durationMinutes`,`focusTag`,`wasCompleted`,`earnedMiles`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FocusSessionEntity entity) {
        statement.bindLong(1, entity.getSessionId());
        statement.bindLong(2, entity.getStartTime());
        statement.bindLong(3, entity.getDurationMinutes());
        statement.bindString(4, entity.getFocusTag());
        final int _tmp = entity.getWasCompleted() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getEarnedMiles());
      }
    };
    this.__insertionAdapterOfUnlockedDestinationEntity = new EntityInsertionAdapter<UnlockedDestinationEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `unlocked_destinations` (`iataCode`,`cityName`,`latitude`,`longitude`,`requiredMiles`,`isUnlocked`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UnlockedDestinationEntity entity) {
        statement.bindString(1, entity.getIataCode());
        statement.bindString(2, entity.getCityName());
        statement.bindDouble(3, entity.getLatitude());
        statement.bindDouble(4, entity.getLongitude());
        statement.bindLong(5, entity.getRequiredMiles());
        final int _tmp = entity.isUnlocked() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__preparedStmtOfUnlockDestination = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE unlocked_destinations SET isUnlocked = 1 WHERE iataCode = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUnlockEligibleDestinations = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE unlocked_destinations SET isUnlocked = 1 WHERE requiredMiles <= ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertSession(final FocusSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFocusSessionEntity.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertDestination(final UnlockedDestinationEntity destination,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUnlockedDestinationEntity.insert(destination);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAllDestinations(final List<UnlockedDestinationEntity> destinations,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUnlockedDestinationEntity.insert(destinations);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object unlockDestination(final String iataCode,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUnlockDestination.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, iataCode);
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
          __preparedStmtOfUnlockDestination.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object unlockEligibleDestinations(final int totalMiles,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUnlockEligibleDestinations.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, totalMiles);
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
          __preparedStmtOfUnlockEligibleDestinations.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FocusSessionEntity>> getAllSessions() {
    final String _sql = "SELECT * FROM focus_sessions ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"focus_sessions"}, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfFocusTag = CursorUtil.getColumnIndexOrThrow(_cursor, "focusTag");
          final int _cursorIndexOfWasCompleted = CursorUtil.getColumnIndexOrThrow(_cursor, "wasCompleted");
          final int _cursorIndexOfEarnedMiles = CursorUtil.getColumnIndexOrThrow(_cursor, "earnedMiles");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final int _tmpSessionId;
            _tmpSessionId = _cursor.getInt(_cursorIndexOfSessionId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final String _tmpFocusTag;
            _tmpFocusTag = _cursor.getString(_cursorIndexOfFocusTag);
            final boolean _tmpWasCompleted;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfWasCompleted);
            _tmpWasCompleted = _tmp != 0;
            final int _tmpEarnedMiles;
            _tmpEarnedMiles = _cursor.getInt(_cursorIndexOfEarnedMiles);
            _item = new FocusSessionEntity(_tmpSessionId,_tmpStartTime,_tmpDurationMinutes,_tmpFocusTag,_tmpWasCompleted,_tmpEarnedMiles);
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
  public Flow<Integer> getTotalMilesFlown() {
    final String _sql = "SELECT COALESCE(SUM(earnedMiles), 0) FROM focus_sessions";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"focus_sessions"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Flow<Integer> getCompletedFlightCount() {
    final String _sql = "SELECT COUNT(*) FROM focus_sessions WHERE wasCompleted = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"focus_sessions"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Flow<Integer> getTotalFocusMinutes() {
    final String _sql = "SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions WHERE wasCompleted = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"focus_sessions"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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
  public Flow<List<UnlockedDestinationEntity>> getAllDestinations() {
    final String _sql = "SELECT * FROM unlocked_destinations ORDER BY requiredMiles ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"unlocked_destinations"}, new Callable<List<UnlockedDestinationEntity>>() {
      @Override
      @NonNull
      public List<UnlockedDestinationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIataCode = CursorUtil.getColumnIndexOrThrow(_cursor, "iataCode");
          final int _cursorIndexOfCityName = CursorUtil.getColumnIndexOrThrow(_cursor, "cityName");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRequiredMiles = CursorUtil.getColumnIndexOrThrow(_cursor, "requiredMiles");
          final int _cursorIndexOfIsUnlocked = CursorUtil.getColumnIndexOrThrow(_cursor, "isUnlocked");
          final List<UnlockedDestinationEntity> _result = new ArrayList<UnlockedDestinationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UnlockedDestinationEntity _item;
            final String _tmpIataCode;
            _tmpIataCode = _cursor.getString(_cursorIndexOfIataCode);
            final String _tmpCityName;
            _tmpCityName = _cursor.getString(_cursorIndexOfCityName);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final int _tmpRequiredMiles;
            _tmpRequiredMiles = _cursor.getInt(_cursorIndexOfRequiredMiles);
            final boolean _tmpIsUnlocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsUnlocked);
            _tmpIsUnlocked = _tmp != 0;
            _item = new UnlockedDestinationEntity(_tmpIataCode,_tmpCityName,_tmpLatitude,_tmpLongitude,_tmpRequiredMiles,_tmpIsUnlocked);
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
  public Flow<List<UnlockedDestinationEntity>> getUnlockedDestinations() {
    final String _sql = "SELECT * FROM unlocked_destinations WHERE isUnlocked = 1 ORDER BY requiredMiles ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"unlocked_destinations"}, new Callable<List<UnlockedDestinationEntity>>() {
      @Override
      @NonNull
      public List<UnlockedDestinationEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIataCode = CursorUtil.getColumnIndexOrThrow(_cursor, "iataCode");
          final int _cursorIndexOfCityName = CursorUtil.getColumnIndexOrThrow(_cursor, "cityName");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRequiredMiles = CursorUtil.getColumnIndexOrThrow(_cursor, "requiredMiles");
          final int _cursorIndexOfIsUnlocked = CursorUtil.getColumnIndexOrThrow(_cursor, "isUnlocked");
          final List<UnlockedDestinationEntity> _result = new ArrayList<UnlockedDestinationEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UnlockedDestinationEntity _item;
            final String _tmpIataCode;
            _tmpIataCode = _cursor.getString(_cursorIndexOfIataCode);
            final String _tmpCityName;
            _tmpCityName = _cursor.getString(_cursorIndexOfCityName);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final int _tmpRequiredMiles;
            _tmpRequiredMiles = _cursor.getInt(_cursorIndexOfRequiredMiles);
            final boolean _tmpIsUnlocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsUnlocked);
            _tmpIsUnlocked = _tmp != 0;
            _item = new UnlockedDestinationEntity(_tmpIataCode,_tmpCityName,_tmpLatitude,_tmpLongitude,_tmpRequiredMiles,_tmpIsUnlocked);
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
  public Object getNextDestinationToUnlock(
      final Continuation<? super UnlockedDestinationEntity> $completion) {
    final String _sql = "SELECT * FROM unlocked_destinations WHERE isUnlocked = 0 ORDER BY requiredMiles ASC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UnlockedDestinationEntity>() {
      @Override
      @Nullable
      public UnlockedDestinationEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfIataCode = CursorUtil.getColumnIndexOrThrow(_cursor, "iataCode");
          final int _cursorIndexOfCityName = CursorUtil.getColumnIndexOrThrow(_cursor, "cityName");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfRequiredMiles = CursorUtil.getColumnIndexOrThrow(_cursor, "requiredMiles");
          final int _cursorIndexOfIsUnlocked = CursorUtil.getColumnIndexOrThrow(_cursor, "isUnlocked");
          final UnlockedDestinationEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpIataCode;
            _tmpIataCode = _cursor.getString(_cursorIndexOfIataCode);
            final String _tmpCityName;
            _tmpCityName = _cursor.getString(_cursorIndexOfCityName);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final int _tmpRequiredMiles;
            _tmpRequiredMiles = _cursor.getInt(_cursorIndexOfRequiredMiles);
            final boolean _tmpIsUnlocked;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsUnlocked);
            _tmpIsUnlocked = _tmp != 0;
            _result = new UnlockedDestinationEntity(_tmpIataCode,_tmpCityName,_tmpLatitude,_tmpLongitude,_tmpRequiredMiles,_tmpIsUnlocked);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
