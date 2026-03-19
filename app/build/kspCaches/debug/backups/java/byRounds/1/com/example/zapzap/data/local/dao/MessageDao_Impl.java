package com.example.zapzap.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.zapzap.data.local.entity.MessageEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MessageEntity> __insertionAdapterOfMessageEntity;

  private final EntityDeletionOrUpdateAdapter<MessageEntity> __deletionAdapterOfMessageEntity;

  private final EntityDeletionOrUpdateAdapter<MessageEntity> __updateAdapterOfMessageEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageStatus;

  private final SharedSQLiteStatement __preparedStmtOfUpdatePinnedStatus;

  private final SharedSQLiteStatement __preparedStmtOfUnpinAllMessages;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsSynced;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessageById;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMessagesByConversation;

  private final SharedSQLiteStatement __preparedStmtOfUpdateMessageText;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMessageEntity = new EntityInsertionAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `messages` (`id`,`conversationId`,`senderId`,`senderName`,`text`,`type`,`mediaUrl`,`localMediaPath`,`latitude`,`longitude`,`timestamp`,`status`,`isPinned`,`isEncrypted`,`isEdited`,`isSynced`,`repliedMessageId`,`repliedMessageText`,`repliedMessageSender`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getConversationId());
        statement.bindString(3, entity.getSenderId());
        statement.bindString(4, entity.getSenderName());
        statement.bindString(5, entity.getText());
        statement.bindString(6, entity.getType());
        statement.bindString(7, entity.getMediaUrl());
        statement.bindString(8, entity.getLocalMediaPath());
        statement.bindDouble(9, entity.getLatitude());
        statement.bindDouble(10, entity.getLongitude());
        statement.bindLong(11, entity.getTimestamp());
        statement.bindString(12, entity.getStatus());
        final int _tmp = entity.isPinned() ? 1 : 0;
        statement.bindLong(13, _tmp);
        final int _tmp_1 = entity.isEncrypted() ? 1 : 0;
        statement.bindLong(14, _tmp_1);
        final int _tmp_2 = entity.isEdited() ? 1 : 0;
        statement.bindLong(15, _tmp_2);
        final int _tmp_3 = entity.isSynced() ? 1 : 0;
        statement.bindLong(16, _tmp_3);
        statement.bindString(17, entity.getRepliedMessageId());
        statement.bindString(18, entity.getRepliedMessageText());
        statement.bindString(19, entity.getRepliedMessageSender());
      }
    };
    this.__deletionAdapterOfMessageEntity = new EntityDeletionOrUpdateAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `messages` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfMessageEntity = new EntityDeletionOrUpdateAdapter<MessageEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `messages` SET `id` = ?,`conversationId` = ?,`senderId` = ?,`senderName` = ?,`text` = ?,`type` = ?,`mediaUrl` = ?,`localMediaPath` = ?,`latitude` = ?,`longitude` = ?,`timestamp` = ?,`status` = ?,`isPinned` = ?,`isEncrypted` = ?,`isEdited` = ?,`isSynced` = ?,`repliedMessageId` = ?,`repliedMessageText` = ?,`repliedMessageSender` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MessageEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getConversationId());
        statement.bindString(3, entity.getSenderId());
        statement.bindString(4, entity.getSenderName());
        statement.bindString(5, entity.getText());
        statement.bindString(6, entity.getType());
        statement.bindString(7, entity.getMediaUrl());
        statement.bindString(8, entity.getLocalMediaPath());
        statement.bindDouble(9, entity.getLatitude());
        statement.bindDouble(10, entity.getLongitude());
        statement.bindLong(11, entity.getTimestamp());
        statement.bindString(12, entity.getStatus());
        final int _tmp = entity.isPinned() ? 1 : 0;
        statement.bindLong(13, _tmp);
        final int _tmp_1 = entity.isEncrypted() ? 1 : 0;
        statement.bindLong(14, _tmp_1);
        final int _tmp_2 = entity.isEdited() ? 1 : 0;
        statement.bindLong(15, _tmp_2);
        final int _tmp_3 = entity.isSynced() ? 1 : 0;
        statement.bindLong(16, _tmp_3);
        statement.bindString(17, entity.getRepliedMessageId());
        statement.bindString(18, entity.getRepliedMessageText());
        statement.bindString(19, entity.getRepliedMessageSender());
        statement.bindString(20, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateMessageStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET status = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdatePinnedStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET isPinned = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUnpinAllMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET isPinned = 0 WHERE conversationId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAsSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET isSynced = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMessageById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteMessagesByConversation = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE conversationId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateMessageText = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET text = ?, isEdited = 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMessageEntity.insert(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertMessages(final List<MessageEntity> messages,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMessageEntity.insert(messages);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMessageEntity.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessage(final MessageEntity message,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMessageEntity.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessageStatus(final String messageId, final String status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMessageStatus.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfUpdateMessageStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePinnedStatus(final String messageId, final boolean isPinned,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdatePinnedStatus.acquire();
        int _argIndex = 1;
        final int _tmp = isPinned ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfUpdatePinnedStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object unpinAllMessages(final String conversationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUnpinAllMessages.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, conversationId);
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
          __preparedStmtOfUnpinAllMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsSynced(final String messageId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsSynced.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfMarkAsSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessageById(final String messageId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessageById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfDeleteMessageById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessagesByConversation(final String conversationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMessagesByConversation.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, conversationId);
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
          __preparedStmtOfDeleteMessagesByConversation.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessageText(final String messageId, final String text,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateMessageText.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, text);
        _argIndex = 2;
        _stmt.bindString(_argIndex, messageId);
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
          __preparedStmtOfUpdateMessageText.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MessageEntity>> getMessagesByConversation(final String conversationId) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, conversationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfLocalMediaPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localMediaPath");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfIsEdited = CursorUtil.getColumnIndexOrThrow(_cursor, "isEdited");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfRepliedMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageId");
          final int _cursorIndexOfRepliedMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageText");
          final int _cursorIndexOfRepliedMessageSender = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageSender");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpSenderName;
            _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpMediaUrl;
            _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            final String _tmpLocalMediaPath;
            _tmpLocalMediaPath = _cursor.getString(_cursorIndexOfLocalMediaPath);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsPinned;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp != 0;
            final boolean _tmpIsEncrypted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp_1 != 0;
            final boolean _tmpIsEdited;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEdited);
            _tmpIsEdited = _tmp_2 != 0;
            final boolean _tmpIsSynced;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_3 != 0;
            final String _tmpRepliedMessageId;
            _tmpRepliedMessageId = _cursor.getString(_cursorIndexOfRepliedMessageId);
            final String _tmpRepliedMessageText;
            _tmpRepliedMessageText = _cursor.getString(_cursorIndexOfRepliedMessageText);
            final String _tmpRepliedMessageSender;
            _tmpRepliedMessageSender = _cursor.getString(_cursorIndexOfRepliedMessageSender);
            _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderId,_tmpSenderName,_tmpText,_tmpType,_tmpMediaUrl,_tmpLocalMediaPath,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpStatus,_tmpIsPinned,_tmpIsEncrypted,_tmpIsEdited,_tmpIsSynced,_tmpRepliedMessageId,_tmpRepliedMessageText,_tmpRepliedMessageSender);
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
  public Object getMessageById(final String messageId,
      final Continuation<? super MessageEntity> $completion) {
    final String _sql = "SELECT * FROM messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, messageId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<MessageEntity>() {
      @Override
      @Nullable
      public MessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfLocalMediaPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localMediaPath");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfIsEdited = CursorUtil.getColumnIndexOrThrow(_cursor, "isEdited");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfRepliedMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageId");
          final int _cursorIndexOfRepliedMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageText");
          final int _cursorIndexOfRepliedMessageSender = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageSender");
          final MessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpSenderName;
            _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpMediaUrl;
            _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            final String _tmpLocalMediaPath;
            _tmpLocalMediaPath = _cursor.getString(_cursorIndexOfLocalMediaPath);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsPinned;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp != 0;
            final boolean _tmpIsEncrypted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp_1 != 0;
            final boolean _tmpIsEdited;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEdited);
            _tmpIsEdited = _tmp_2 != 0;
            final boolean _tmpIsSynced;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_3 != 0;
            final String _tmpRepliedMessageId;
            _tmpRepliedMessageId = _cursor.getString(_cursorIndexOfRepliedMessageId);
            final String _tmpRepliedMessageText;
            _tmpRepliedMessageText = _cursor.getString(_cursorIndexOfRepliedMessageText);
            final String _tmpRepliedMessageSender;
            _tmpRepliedMessageSender = _cursor.getString(_cursorIndexOfRepliedMessageSender);
            _result = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderId,_tmpSenderName,_tmpText,_tmpType,_tmpMediaUrl,_tmpLocalMediaPath,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpStatus,_tmpIsPinned,_tmpIsEncrypted,_tmpIsEdited,_tmpIsSynced,_tmpRepliedMessageId,_tmpRepliedMessageText,_tmpRepliedMessageSender);
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

  @Override
  public Flow<List<MessageEntity>> searchMessages(final String conversationId, final String query) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? AND text LIKE '%' || ? || '%' ORDER BY timestamp ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, conversationId);
    _argIndex = 2;
    _statement.bindString(_argIndex, query);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfLocalMediaPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localMediaPath");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfIsEdited = CursorUtil.getColumnIndexOrThrow(_cursor, "isEdited");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfRepliedMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageId");
          final int _cursorIndexOfRepliedMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageText");
          final int _cursorIndexOfRepliedMessageSender = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageSender");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpSenderName;
            _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpMediaUrl;
            _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            final String _tmpLocalMediaPath;
            _tmpLocalMediaPath = _cursor.getString(_cursorIndexOfLocalMediaPath);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsPinned;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp != 0;
            final boolean _tmpIsEncrypted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp_1 != 0;
            final boolean _tmpIsEdited;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEdited);
            _tmpIsEdited = _tmp_2 != 0;
            final boolean _tmpIsSynced;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_3 != 0;
            final String _tmpRepliedMessageId;
            _tmpRepliedMessageId = _cursor.getString(_cursorIndexOfRepliedMessageId);
            final String _tmpRepliedMessageText;
            _tmpRepliedMessageText = _cursor.getString(_cursorIndexOfRepliedMessageText);
            final String _tmpRepliedMessageSender;
            _tmpRepliedMessageSender = _cursor.getString(_cursorIndexOfRepliedMessageSender);
            _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderId,_tmpSenderName,_tmpText,_tmpType,_tmpMediaUrl,_tmpLocalMediaPath,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpStatus,_tmpIsPinned,_tmpIsEncrypted,_tmpIsEdited,_tmpIsSynced,_tmpRepliedMessageId,_tmpRepliedMessageText,_tmpRepliedMessageSender);
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
  public Flow<MessageEntity> getPinnedMessage(final String conversationId) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? AND isPinned = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, conversationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<MessageEntity>() {
      @Override
      @Nullable
      public MessageEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfLocalMediaPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localMediaPath");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfIsEdited = CursorUtil.getColumnIndexOrThrow(_cursor, "isEdited");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfRepliedMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageId");
          final int _cursorIndexOfRepliedMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageText");
          final int _cursorIndexOfRepliedMessageSender = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageSender");
          final MessageEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpSenderName;
            _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpMediaUrl;
            _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            final String _tmpLocalMediaPath;
            _tmpLocalMediaPath = _cursor.getString(_cursorIndexOfLocalMediaPath);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsPinned;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp != 0;
            final boolean _tmpIsEncrypted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp_1 != 0;
            final boolean _tmpIsEdited;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEdited);
            _tmpIsEdited = _tmp_2 != 0;
            final boolean _tmpIsSynced;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_3 != 0;
            final String _tmpRepliedMessageId;
            _tmpRepliedMessageId = _cursor.getString(_cursorIndexOfRepliedMessageId);
            final String _tmpRepliedMessageText;
            _tmpRepliedMessageText = _cursor.getString(_cursorIndexOfRepliedMessageText);
            final String _tmpRepliedMessageSender;
            _tmpRepliedMessageSender = _cursor.getString(_cursorIndexOfRepliedMessageSender);
            _result = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderId,_tmpSenderName,_tmpText,_tmpType,_tmpMediaUrl,_tmpLocalMediaPath,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpStatus,_tmpIsPinned,_tmpIsEncrypted,_tmpIsEdited,_tmpIsSynced,_tmpRepliedMessageId,_tmpRepliedMessageText,_tmpRepliedMessageSender);
          } else {
            _result = null;
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
  public Object getUnsyncedMessages(final Continuation<? super List<MessageEntity>> $completion) {
    final String _sql = "SELECT * FROM messages WHERE isSynced = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MessageEntity>>() {
      @Override
      @NonNull
      public List<MessageEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfSenderName = CursorUtil.getColumnIndexOrThrow(_cursor, "senderName");
          final int _cursorIndexOfText = CursorUtil.getColumnIndexOrThrow(_cursor, "text");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfMediaUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "mediaUrl");
          final int _cursorIndexOfLocalMediaPath = CursorUtil.getColumnIndexOrThrow(_cursor, "localMediaPath");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsEncrypted = CursorUtil.getColumnIndexOrThrow(_cursor, "isEncrypted");
          final int _cursorIndexOfIsEdited = CursorUtil.getColumnIndexOrThrow(_cursor, "isEdited");
          final int _cursorIndexOfIsSynced = CursorUtil.getColumnIndexOrThrow(_cursor, "isSynced");
          final int _cursorIndexOfRepliedMessageId = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageId");
          final int _cursorIndexOfRepliedMessageText = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageText");
          final int _cursorIndexOfRepliedMessageSender = CursorUtil.getColumnIndexOrThrow(_cursor, "repliedMessageSender");
          final List<MessageEntity> _result = new ArrayList<MessageEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MessageEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpConversationId;
            _tmpConversationId = _cursor.getString(_cursorIndexOfConversationId);
            final String _tmpSenderId;
            _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            final String _tmpSenderName;
            _tmpSenderName = _cursor.getString(_cursorIndexOfSenderName);
            final String _tmpText;
            _tmpText = _cursor.getString(_cursorIndexOfText);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final String _tmpMediaUrl;
            _tmpMediaUrl = _cursor.getString(_cursorIndexOfMediaUrl);
            final String _tmpLocalMediaPath;
            _tmpLocalMediaPath = _cursor.getString(_cursorIndexOfLocalMediaPath);
            final double _tmpLatitude;
            _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            final double _tmpLongitude;
            _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsPinned;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp != 0;
            final boolean _tmpIsEncrypted;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEncrypted);
            _tmpIsEncrypted = _tmp_1 != 0;
            final boolean _tmpIsEdited;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsEdited);
            _tmpIsEdited = _tmp_2 != 0;
            final boolean _tmpIsSynced;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsSynced);
            _tmpIsSynced = _tmp_3 != 0;
            final String _tmpRepliedMessageId;
            _tmpRepliedMessageId = _cursor.getString(_cursorIndexOfRepliedMessageId);
            final String _tmpRepliedMessageText;
            _tmpRepliedMessageText = _cursor.getString(_cursorIndexOfRepliedMessageText);
            final String _tmpRepliedMessageSender;
            _tmpRepliedMessageSender = _cursor.getString(_cursorIndexOfRepliedMessageSender);
            _item = new MessageEntity(_tmpId,_tmpConversationId,_tmpSenderId,_tmpSenderName,_tmpText,_tmpType,_tmpMediaUrl,_tmpLocalMediaPath,_tmpLatitude,_tmpLongitude,_tmpTimestamp,_tmpStatus,_tmpIsPinned,_tmpIsEncrypted,_tmpIsEdited,_tmpIsSynced,_tmpRepliedMessageId,_tmpRepliedMessageText,_tmpRepliedMessageSender);
            _result.add(_item);
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
