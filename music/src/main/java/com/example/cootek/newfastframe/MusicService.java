package com.example.cootek.newfastframe;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.example.commonlibrary.bean.music.DaoSession;
import com.example.commonlibrary.bean.music.MusicHistoryInfo;
import com.example.commonlibrary.bean.music.MusicHistoryInfoDao;
import com.example.commonlibrary.bean.music.MusicPlayBean;
import com.example.commonlibrary.bean.music.MusicPlayBeanDao;
import com.example.commonlibrary.utils.CommonLogger;
import com.example.commonlibrary.utils.FileUtil;
import com.example.commonlibrary.utils.Httputil;
import com.example.commonlibrary.utils.ToastUtils;
import com.example.cootek.newfastframe.receiver.MediaButtonIntentReceiver;
import com.example.cootek.newfastframe.ui.MainActivity;
import com.example.cootek.newfastframe.util.MusicUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;


/**
 * Created by 闄堥敠鍐?on 2017/8/7.
 */

public class MusicService extends Service {

    public static final int MAX_HISTORY_SIZE = 1000;
    public static final String MEDIA_BUTTON_PAUSE = "pause";
    public static final String MEDIA_BUTTON_ACTION = "media_button_action";
    public static final String COMMAND = "command";
    public static final String MEDIA_BUTTON_TOGGLE_PAUSE = "toggle_pause";
    public static final String MEDIA_BUTTON_STOP = "stop";
    public static final String MEDIA_BUTTON_PLAY = "play";
    public static final String MEDIA_BUTTON_NEXT = "next";
    public static final String MEDIA_BUTTON_PREVIOUS = "previous";
    public static final String META_CHANGED = "meta_changed";
    public static final String REFRESH_DATA = "refresh_data";
    public static final String PLAYSTATE_CHANGED = "play_state_changed";
    public static final int MODE_NORMAL = 0;
    public static final int MODE_LOOP = 1;
    public static final int MODE_SHUFFLE = 2;
    public static final String BUFFER_UPDATE_CHANGED = "butter_update_changed";

    private int mode = MODE_NORMAL;
    public static final int FADEDOWN = 6;
    public static final int FADEUP = 7;
    public static final String PREVIOUS_ACTION = "previous_action";
    public static final String NEXT_ACTION = "next_action";
    public static final String TOGGLEPAUSE_ACTION = "toggle_pause_action";
    private MusicServiceStub bind = new MusicServiceStub(this);
    private NotificationManagerCompat notificationManagerCompat;
    private DaoSession daoSession;
    private HandlerThread handlerThread;
    private MusicServiceHandler musicServiceHandler;
    private MediaSessionCompat mediaSessionCompat;
    private long notificationPostTime = 0;
    private PowerManager.WakeLock mWakeLock;
    private int mStartId;
    private WrappedMediaPlayer wrappedMediaPlayer;
    private SharedPreferences mPreferences;
    private ArrayList<MusicPlayBean> mPlaylist = new ArrayList<>(100);
    private int playPosition = -1;
    private int mOpenFailedCounter = 0;
    private static LinkedList<Integer> mHistory = new LinkedList<>();
    private boolean isPlaying = false;
    private Shuffler mShuffler = new Shuffler();
    private int nextPlayPosition = -1;
    private float mCurrentVolume = 1.0f;
    private int notificationId;

    @Override
    public void onCreate() {
        super.onCreate();
//        鍙戦€佸墠鍙伴€氱煡
        CommonLogger.e("鏈嶅姟鐨刼nCreate1");
        notificationManagerCompat = NotificationManagerCompat.from(this);
        daoSession = VideoApplication.getMainComponent().getDaoSession();
        handlerThread = new HandlerThread("music_handler_thread", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        musicServiceHandler = new MusicServiceHandler(handlerThread.getLooper(), this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            妗岄潰閿佸睆鐨勪細璇濆洖璋?            setUpMediaSession();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName()));
        wrappedMediaPlayer = new WrappedMediaPlayer(this);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.setReferenceCounted(false);
        mPreferences = getSharedPreferences(MusicUtil.SHARED_PREFERENCES_NAME, 0);
    }


    private void reloadQueue() {
        List<MusicPlayBean> list = daoSession.getMusicPlayBeanDao().queryBuilder().where(MusicPlayBeanDao.Properties.IsRecent.eq(Boolean.TRUE)).list();
        if (list.size() > 0) {
            mPlaylist.clear();
            mPlaylist.addAll(list);
        }
        if (mPlaylist.size() > 0) {
            final int pos = mPreferences.getInt(MusicUtil.POSITION, 0); //璁板綍浜嗕笂娆￠€€鍑烘椂鎾斁鏇茬洰鍦ㄦ挱鏀鹃槦鍒椾腑鐨勪綅缃?            if (pos < 0 || pos >= mPlaylist.size()) {
                mPlaylist.clear();
                return;
            }
            playPosition = pos;
            openCurrentAndNext();
            long seekpos = mPreferences.getLong(MusicUtil.SEEK, 0);
            seek(seekpos >= 0 && seekpos < duration() ? seekpos : 0);
            mode = mPreferences.getInt(MusicUtil.PLAY_MODE, 0);
            if (mode == MODE_SHUFFLE) {
//                鍙栧嚭鍘嗗彶鎾斁鍒楄〃
                mHistory.clear();
                List<MusicHistoryInfo> historyInfos = daoSession.getMusicHistoryInfoDao().queryBuilder().where(MusicHistoryInfoDao.Properties.Position.le(mPlaylist.size())).build().list();
                for (MusicHistoryInfo info :
                        historyInfos) {
                    mHistory.add(info.getPosition());
                }
            }
        }
    }

    private long duration() {
        CommonLogger.e("鑾峰彇姝屾洸鐨勬椂闀?);
        return wrappedMediaPlayer.duration();
    }


    private void notifyChange(String action) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            updateMediaSession(action);
        final Intent intent = new Intent(action);
        if (!action.equals(BUFFER_UPDATE_CHANGED)) {
            intent.putExtra("id", getAudioId());
            intent.putExtra("artistName", getArtistName());
            intent.putExtra("albumUrl", getAlbumUrl());
            intent.putExtra("albumName", getAlbumName());
            intent.putExtra("isPlaying", isPlaying());
            intent.putExtra("songName", getSongName());
            intent.putExtra("maxProgress", duration());
            intent.putExtra("mode", getPlayMode());
        } else {
            intent.putExtra("buffer_update", wrappedMediaPlayer.getSecondProgress());
        }
        sendBroadcast(intent);
        CommonLogger.e("宸茬粡鍙戦€佸箍鎾?);
        switch (action) {
            case META_CHANGED:
            case REFRESH_DATA:
                updateNotification();
                break;
            case PLAYSTATE_CHANGED:
                updateNotification();
                saveQueue(false);
                break;
            default:
                saveQueue(false);
                break;
        }
    }

    private int getPlayMode() {
        return mode;
    }

    private String getAlbumUrl() {
        String result = getCurrentMusicPlayInfo() != null ? getCurrentMusicPlayInfo().getAlbumUrl() : null;
        CommonLogger.e("service:" + result);
        return result;
    }

    private void updateMediaSession(String action) {
        CommonLogger.e("updateMediaSession锛? + action);
        final int playState = isPlaying
                ? PlaybackStateCompat.STATE_PLAYING
                : PlaybackStateCompat.STATE_PAUSED;
        if (action.equals(PLAYSTATE_CHANGED)) { //鎾斁鐘舵€佹敼鍙樻椂
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(playState, position(), 1.0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                        .build());
            }
        } else if (action.equals(META_CHANGED)) { //褰撳墠鎾斁姝屾洸鐨勪俊鎭垨鑰呮挱鏀鹃槦鍒楁敼鍙?            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (getCurrentMusicPlayInfo() != null) {
                    if (getCurrentMusicPlayInfo().isLocal()) {
                        refreshMediaSession(playState, BitmapFactory.decodeResource(getResources(), R.drawable.icon_album_default));
                    } else {
//                        杩欓噷涓嶈兘鐢℅lide鍔犺浇锛屽湪into鏂规硶涓細鍑虹幇鍒や富绾跨▼鐨勫垽鏂粠鑰屽嚭閿?                        String path = mPreferences.getString(String.valueOf(getAudioId()), null);
                        if (path == null || !new File(path).exists()) {
                            loadAlbum(new AlbumCallBack() {
                                @Override
                                public void onCallBack(Bitmap bitmap) {
                                    refreshMediaSession(playState, bitmap);
                                }
                            });
                        } else {
                            try {
                                refreshMediaSession(playState, BitmapFactory.decodeStream(new FileInputStream(new File(path))));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                CommonLogger.e("鏂囦欢娌℃湁琚壘鍒? + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    private void refreshMediaSession(int playState, Bitmap resource) {
        if (resource == null) {
            resource = BitmapFactory.decodeResource(getResources(), R.drawable.icon_album_default);
        }
        mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getArtistName())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, getArtistName())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, getAlbumName())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getSongName())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration())
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, getQueuePosition())
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, getQueue().length)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, getGenreName())
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, resource)
                .build());
        mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(playState, position(), 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                .build());
    }


    /**
     * 鑾峰彇姝屾洸鎵€灞炵被鍨?     *
     * @return GenreName
     */
    private String getGenreName() {
        synchronized (this) {
            if (getCurrentMusicPlayInfo() != null && !getCurrentMusicPlayInfo().isLocal()) {
                return null;
            }
            String[] genreProjection = {MediaStore.Audio.Genres.NAME};
            Uri genreUri = MediaStore.Audio.Genres.getContentUriForAudioId("external",
                    (int) mPlaylist.get(playPosition).getSongId());
            Cursor genreCursor = getContentResolver().query(genreUri, genreProjection,
                    null, null, null);
            if (genreCursor != null) {
                try {
                    if (genreCursor.moveToFirst()) {
                        return genreCursor.getString(
                                genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME));
                    }
                } finally {
                    genreCursor.close();
                }
            }
            return null;
        }

    }

    private long[] getQueue() {
        CommonLogger.e("鑾峰彇鐜板湪鐨勬挱鏀剧殑闃熷垪");
        synchronized (this) {
            final int len = mPlaylist.size();
            final long[] list = new long[len];
            for (int i = 0; i < len; i++) {
                list[i] = mPlaylist.get(i).getSongId();
            }
            return list;
        }
    }

    private int getQueuePosition() {
        CommonLogger.e("鑾峰彇鐜板湪鎾斁闃熷垪鐨勪綅缃?);
        synchronized (this) {
            return playPosition;
        }
    }

    private void saveQueue(boolean isAll) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        if (isAll) {
//            娓呴櫎鏃ф暟鎹紝淇濇寔鏂版暟鎹?            if (mode == MODE_SHUFFLE) {
                List<MusicHistoryInfo> list = new ArrayList<>();
                for (int i = 0; i < mHistory.size(); i++) {
                    MusicHistoryInfo musicHistoryInfo = new MusicHistoryInfo(mHistory.get(i));
                    list.add(musicHistoryInfo);
                }
                daoSession.getMusicHistoryInfoDao().deleteAll();
                if (list.size() > 0) {
                    daoSession.getMusicHistoryInfoDao().insertInTx(list);
                }
            }
            List<MusicPlayBean> lastPlayList = daoSession.getMusicPlayBeanDao().queryBuilder().where(MusicPlayBeanDao.Properties.IsRecent.eq(Boolean.TRUE)).list();

            if (lastPlayList.size() > 0) {
                for (MusicPlayBean bean :
                        lastPlayList) {
                    bean.setIsRecent(false);
                }
                daoSession.getMusicPlayBeanDao().updateInTx(lastPlayList);
            }
            daoSession.getMusicPlayBeanDao().insertOrReplaceInTx(mPlaylist);
        }
        editor.putInt("position", playPosition);
        if (wrappedMediaPlayer.isInitialized()) {
            editor.putLong("seek", wrappedMediaPlayer.position());
        }
        editor.putInt("play_mode", mode);
        editor.apply();
    }

    private void setUpMediaSession() {
        mediaSessionCompat = new MediaSessionCompat(this, "listener");
        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                CommonLogger.e("mediaSession:onPlay");
                play();

            }

            @Override
            public void onPause() {
                CommonLogger.e("mediaSession:onPause");
                pause();
            }

            @Override
            public void onSkipToNext() {
                CommonLogger.e("mediaSession:onSkipToNext");
                next(true);
            }

            @Override
            public void onSkipToPrevious() {
                CommonLogger.e("mediaSession:onSkipToPrevious");
                previous();
            }

            @Override
            public void onStop() {

            }

            @Override
            public void onSeekTo(long pos) {
                CommonLogger.e("mediaSession:onSeekTo");
            }
        });
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }


    private void cancelNotification() {
        stopForeground(false);
        notificationManagerCompat.cancel(hashCode());
        notificationPostTime = 0;
    }

    private boolean isPlaying() {
        return isPlaying;
    }

    private long seek(long position) {
        if (wrappedMediaPlayer.isInitialized()) {
            if (position < 0) {
                position = 0;
            } else if (position > wrappedMediaPlayer.duration()) {
                position = wrappedMediaPlayer.duration();
            }
            return wrappedMediaPlayer.seek(position);
        }
        return -1;
    }

    private void next(boolean force) {
        stop(false);
        prepareNext(force);
        if (nextPlayPosition < 0) {
//            娌℃湁涓嬩竴棣?//            璁剧疆鍋滄鐘舵€佸苟閫氱煡
            setIsSupposedToBePlaying(false, true);
        } else {
            play();
        }
    }


    private void previous() {
        synchronized (this) {
            stop(false);
            int previousPosition = playPosition;
            playPosition = getPreviousPlayPosition(true);
            if (playPosition >= 0) {
                openCurrentAndMaybeNext(false);
                setNextMusicPlayInfo(previousPosition);
                play();
            } else {
                setIsSupposedToBePlaying(false, true);
            }
        }
    }


    /**
     * 鑾峰彇鎾斁鍒楄〃涓殑鍓嶄竴棣栨瓕鏇?     *
     * @param removeFromHistory 鏄惁浠庡巻鍙蹭腑鍒犻櫎鏈€杩戜竴涓挱鏀捐褰?     * @return 浣嶇疆
     */
    public int getPreviousPlayPosition(boolean removeFromHistory) {
        CommonLogger.e("鑾峰彇鎾斁鍒楄〃涓殑鍓嶄竴棣栨瓕鏇?);
        synchronized (this) {
            if (mode == MODE_SHUFFLE) { //鏅€氶殢鏈烘ā寮?浠庡巻鍙蹭腑鑾峰彇
                final int size = mHistory.size();
                if (size == 0) {
                    return -1;
                }
                final Integer pos = mHistory.get(size - 1);
                if (removeFromHistory) {
                    mHistory.remove(size - 1);
                }
                return pos;
            } else if (mode == MODE_LOOP) {
                return playPosition;
            } else {
                if (playPosition > 0) {
                    return playPosition - 1;
                } else {
                    return mPlaylist.size() - 1;
                }
            }
        }
    }

    private long position() {
        if (wrappedMediaPlayer.isInitialized) {
            return wrappedMediaPlayer.position();
        }
        return -1;
    }


    private void stop(boolean died) {
        CommonLogger.e("鍋滄鎾斁");
        if (wrappedMediaPlayer.isInitialized()) {
            wrappedMediaPlayer.stop();
        }
        if (died) {
            setIsSupposedToBePlaying(false, false);
        } else {
            CommonLogger.e("鍋滄鍓嶅彴");
            stopForeground(false);
        }

    }


    /**
     * 鏄惁閲嶆柊鑾峰彇涓嬩竴棣栨瓕鐨勪綅缃?褰撴挱鏀惧墠涓€棣栨瓕鐨勬椂鍊欙紝灏变笉闇€瑕侀噸鏂拌幏鍙栦笅涓€棣栨瓕鐨勪綅缃簡
     */
    private void play() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mediaSessionCompat.setActive(true);
        if (wrappedMediaPlayer.isInitialized()) {
            CommonLogger.e("寮€濮嬫挱鏀?);
            wrappedMediaPlayer.start();
            CommonLogger.e("杩欓噷閫氳繃绉婚櫎鍑忓急鐨勫０闊崇殑娑堟伅鍜屽彂閫佸鍔犲０闊崇殑娑堟伅鏉ヨ皟鏁村埌姝ｅ父鐨勯煶閲?);
            musicServiceHandler.removeMessages(FADEDOWN);
            musicServiceHandler.sendEmptyMessage(FADEUP); //缁勪欢璋冨埌姝ｅ父闊抽噺
            CommonLogger.e("璁剧疆鎾斁鐘舵€?骞堕€氱煡鎾斁鐘舵€佺殑鏀瑰彉");
            setIsSupposedToBePlaying(true, true);
            notifyChange(META_CHANGED);
        } else {
            CommonLogger.e("鎾斁鐨勬椂鍊欒祫婧愯繕娌℃湁鍑嗗鎴栧嚭閿?);
        }
    }


    /**
     * 鏇存柊閫氱煡鏍?     */
    private void updateNotification() {
        if (!isPlaying()) {
            stopForeground(false);
        }
        notificationId = hashCode();
        if (notificationPostTime == 0) {
            if (getCurrentMusicPlayInfo() != null) {
                if (getCurrentMusicPlayInfo().isLocal()) {
                    startForeground(notificationId, getBuildNotification(BitmapFactory.decodeResource(getResources(), R.drawable.icon_album_default)));
                } else {
                    String imagePath = mPreferences.getString(String.valueOf(getAudioId()), null);
                    if (imagePath == null || !new File(imagePath).exists()) {
                        loadAlbum(new AlbumCallBack() {
                            @Override
                            public void onCallBack(Bitmap bitmap) {
                                startForeground(notificationId, getBuildNotification(bitmap));
                            }
                        });
                    } else {
                        startForeground(notificationId, getBuildNotification(BitmapFactory.decodeFile(MusicUtil.getMusicImageCacheDir() + getCurrentMusicPlayInfo().getSongId() + ".jpg")));
                    }
                }
            }
        } else {
            if (getCurrentMusicPlayInfo() != null) {
                if (getCurrentMusicPlayInfo().isLocal()) {
                    notificationManagerCompat.notify(notificationId, getBuildNotification(BitmapFactory.decodeResource(getResources(), R.drawable.icon_album_default)));
                } else {
                    String imagePath = mPreferences.getString(String.valueOf(getAudioId()), null);
                    if (imagePath == null || !new File(imagePath).exists()) {
                        loadAlbum(new AlbumCallBack() {
                            @Override
                            public void onCallBack(Bitmap bitmap) {
                                notificationManagerCompat.notify(notificationId, getBuildNotification(bitmap));
                            }
                        });
                    } else {
                        notificationManagerCompat.notify(notificationId, getBuildNotification(BitmapFactory.decodeFile(MusicUtil.getMusicImageCacheDir() + getCurrentMusicPlayInfo().getSongId() + ".jpg")));
                    }
                }
            }
        }
    }

    interface AlbumCallBack {
        void onCallBack(Bitmap bitmap);
    }

    private void loadAlbum(final AlbumCallBack albumCallBack) {
        new AsyncTask<String, Void, Bitmap>() {
            String songId;

            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    songId = params[1];
                    URL url = new URL(params[0]);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setRequestMethod("GET");
                    if (httpURLConnection.getResponseCode() == 200) {
                        return BitmapFactory.decodeStream(httpURLConnection.getInputStream());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (albumCallBack != null) {
                    albumCallBack.onCallBack(bitmap);
                }
                try {
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(FileUtil.newFile(MusicUtil.getMusicImageCacheDir() + songId + ".jpg")));
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putString(songId, MusicUtil.getMusicImageCacheDir() + songId + ".jpg");
                        editor.apply();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }.execute(getCurrentMusicPlayInfo().getAlbumUrl(), String.valueOf(getCurrentMusicPlayInfo().getSongId()));
    }

    private Notification getBuildNotification(Bitmap bitmap) {
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_album_default);
        }
        String albumName = getAlbumName();
        String artistName = getArtistName();
        boolean isPlaying = isPlaying();
        String text = TextUtils.isEmpty(albumName)
                ? artistName : artistName + " - " + albumName;

        int playButtonResId = isPlaying
                ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_arrow_black_24dp;
        if (notificationPostTime == 0) {
            notificationPostTime = System.currentTimeMillis();
        }
        androidx.core.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext())
                .setLargeIcon(bitmap)
                .setSmallIcon(R.drawable.ic_music_note_white_24dp)
                .setContentIntent(getMainIntent())
                .setContentTitle(getSongName())
                .setContentText(text)
                .setOngoing(true)
                .setWhen(notificationPostTime)
                .addAction(R.drawable.ic_skip_previous_black_24dp,
                        "",
                        getPendingIntent(PREVIOUS_ACTION))
                .addAction(playButtonResId, "",
                        getPendingIntent(TOGGLEPAUSE_ACTION))
                .addAction(R.drawable.ic_skip_next_black_24dp,
                        "",
                        getPendingIntent(NEXT_ACTION));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setShowWhen(false);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionCompat.getSessionToken())
                    .setShowActionsInCompactView(0, 1, 2, 3);
            builder.setStyle(style);
        }
        return builder.build();
    }

    private PendingIntent getMainIntent() {
        Intent nowPlayingIntent = new Intent(this, MainActivity.class);
//        nowPlayingIntent.setComponent(new ComponentName("com.example.cootek.newfastframe","com.example.cootek.newfastframe."));
//        nowPlayingIntent.setAction(Constants.NAVIGATE_LIBRARY);
        return PendingIntent.getActivity(this, 0, nowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, 0);
    }

    private String getArtistName() {
        CommonLogger.e("鑾峰彇姝屾墜鍚?);
        synchronized (this) {
            if (getCurrentMusicPlayInfo() != null) {
                return getCurrentMusicPlayInfo().getArtistName();
            }
            return null;
        }
    }

    private String getAlbumName() {
        CommonLogger.e("鑾峰彇涓撹緫鍚?);
        synchronized (this) {
            if (getCurrentMusicPlayInfo() != null) {
                return getCurrentMusicPlayInfo().getAlbumName();
            }
            return null;
        }
    }


    private void setIsSupposedToBePlaying(boolean isPlaying, boolean notify) {
        if (this.isPlaying != isPlaying) {
            this.isPlaying = isPlaying;

            if (notify) {
                notifyChange(PLAYSTATE_CHANGED);
            }
        }
    }


    private void pause() {
        CommonLogger.e("鏆傚仠");
        synchronized (this) {
            musicServiceHandler.removeMessages(FADEUP);
            musicServiceHandler.sendEmptyMessage(FADEDOWN);
            wrappedMediaPlayer.pause();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CommonLogger.e("鏈嶅姟鐨刼nStartCommand");
        mStartId = startId;
        if (intent != null) {
            String action = intent.getAction();
            String value = intent.getStringExtra(COMMAND);
            if (action != null) {
                switch (action) {
//                    濯掍綋鎸夐挳
                    case MEDIA_BUTTON_ACTION:
                        switch (value) {
                            case MEDIA_BUTTON_PAUSE:
                                CommonLogger.e("MEDIA_BUTTON_PAUSE");
                                pause();
                                break;
                            case MEDIA_BUTTON_TOGGLE_PAUSE:
                                CommonLogger.e("MEDIA_BUTTON_TOGGLE_PAUSE");
                                if (isPlaying()) {
                                    pause();
                                } else {
                                    play();
                                }
                                break;
                            case MEDIA_BUTTON_NEXT:
                                CommonLogger.e("MEDIA_BUTTON_NEXT_next");
                                next(true);
                                break;
                            case MEDIA_BUTTON_PREVIOUS:
                                previous();
                                break;
                            case MEDIA_BUTTON_STOP:
                                pause();
                                seek(0);
                                break;
                            case MEDIA_BUTTON_PLAY:
                                play();
                                break;
                        }
                        break;
                    case PREVIOUS_ACTION:
                        ToastUtils.showShortToast("鍓嶄竴棣?);
                        previous();
                        break;
                    case NEXT_ACTION:
                        next(true);
                        break;
                    case TOGGLEPAUSE_ACTION:
                        if (isPlaying()) {
                            pause();
                        } else {
                            play();
                        }
                        break;
                }
            }
        }
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        saveQueue(true);
        musicServiceHandler.removeCallbacksAndMessages(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            handlerThread.quitSafely();
        } else {
            handlerThread.quit();
        }
        wrappedMediaPlayer.release();
        wrappedMediaPlayer = null;
        cancelNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mediaSessionCompat.setActive(false);
        mediaSessionCompat.release();
        mWakeLock.release();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        CommonLogger.e("1鏈嶅姟鐨刼nBind");
        reloadQueue();
        return bind;
    }


    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        CommonLogger.e("onUnbind");
        if (isPlaying()) {
            return true;
        }
        saveQueue(true);
        stopSelf(mStartId);

        return true;
    }

    private class MusicServiceStub extends IMusicService.Stub {
        private WeakReference<MusicService> musicServiceWeakReference;

        MusicServiceStub(MusicService service) {
            musicServiceWeakReference = new WeakReference<>(service);
        }


        @Override
        public void open(List<MusicPlayBean> list, int position) throws RemoteException {
            musicServiceWeakReference.get().open(list, position);
        }

        @Override
        public void stop() throws RemoteException {
            musicServiceWeakReference.get().stop();
        }

        @Override
        public void pause() throws RemoteException {
            musicServiceWeakReference.get().pause();
        }

        @Override
        public void play() throws RemoteException {
            musicServiceWeakReference.get().play();
        }

        @Override
        public void prev() throws RemoteException {
            musicServiceWeakReference.get().previous();
        }

        @Override
        public void next() throws RemoteException {
            musicServiceWeakReference.get().next(true);
        }

        @Override
        public void setPlayMode(int mode) throws RemoteException {
            musicServiceWeakReference.get().setPlayMode(mode);
        }


        @Override
        public void refresh() throws RemoteException {
            musicServiceWeakReference.get().refresh();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return musicServiceWeakReference.get().isPlaying();
        }

        @Override
        public long[] getQueue() throws RemoteException {
            return musicServiceWeakReference.get().getQueue();
        }

        @Override
        public int getQueuePosition() throws RemoteException {
            return musicServiceWeakReference.get().getQueuePosition();
        }

        @Override
        public long duration() throws RemoteException {
            return musicServiceWeakReference.get().duration();
        }

        @Override
        public long position() throws RemoteException {
            return musicServiceWeakReference.get().position();
        }

        @Override
        public long seek(long pos) throws RemoteException {
            return musicServiceWeakReference.get().seek(pos);
        }

        @Override
        public MusicPlayBean getCurrentPlayInfo() throws RemoteException {
            return musicServiceWeakReference.get().getCurrentMusicPlayInfo();
        }

        @Override
        public MusicPlayBean getPlayInfo(int index) throws RemoteException {
            return musicServiceWeakReference.get().getMusicPlayInfo(index);
        }


        @Override
        public void remove(int position) throws RemoteException {
            musicServiceWeakReference.get().remove(position);
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return musicServiceWeakReference.get().getAudioSessionId();
        }
    }

    private int getAudioSessionId() {
        return wrappedMediaPlayer.getAudioSessionId();
    }

    private void setPlayMode(int mode) {
        if (this.mode != mode) {
            this.mode = mode;
//            閲嶆柊閫夋嫨涓嬩竴棣栨瓕鏇?            setNextMusicPlayInfo();
        }
    }

    private void remove(int position) {
        if (mPlaylist != null && position >= 0 && position < mPlaylist.size()) {
            if (playPosition == position) {
                if (mPlaylist.size() == 1) {
                    mPlaylist.remove(position);
                    stop(true);
                } else {
                    mPlaylist.remove(position);
                    next(true);
                }
            } else if (nextPlayPosition == position) {
                if (mPlaylist.size() == 1) {
                    mPlaylist.remove(position);
                    stop(true);
                } else {
                    mPlaylist.remove(position);
//                    鏇存柊涓嬩竴棣栨瓕璧勬簮
                    setNextMusicPlayInfo();
                }
            } else {
                mPlaylist.remove(position);
            }
        }
    }


    private void refresh() {
        notifyChange(REFRESH_DATA);
    }


    private void stop() {
        stop(true);
    }

    private void open(List<MusicPlayBean> list, int position) {
        if (!list.equals(mPlaylist)) {
            addToPlayList(list, -1); //娣诲姞鍒版挱鏀惧垪琛?骞舵竻绌哄師鎾斁鍒楄〃
            CommonLogger.e("娓呯┖涔嬪墠鐨勬挱鏀鹃槦鍒楀悗閫氱煡闃熷垪鐨勬敼鍙?);
        }
        if (position >= 0) {
            playPosition = position;
        } else {
            playPosition = mShuffler.nextInt(mPlaylist.size());
        }
        mHistory.clear();
        openCurrentAndMaybeNext(true);
    }


    /**
     * 娣诲姞鎵€鏈夌殑item鍒版挱鏀鹃槦鍒椾腑
     *
     * @param list     鎾斁鍒楄〃
     * @param position -1琛ㄧず娓呴櫎涔嬪墠鐨勬挱鏀惧垪琛?     */
    private void addToPlayList(List<MusicPlayBean> list, int position) {
        final int size = list.size();
        if (position < 0) {
            mPlaylist.clear();
            position = 0;
        }
        mPlaylist.ensureCapacity(mPlaylist.size() + size);
        if (position > mPlaylist.size()) {
            position = mPlaylist.size();
        }
        mPlaylist.addAll(position, list);
        daoSession.getMusicPlayBeanDao().insertOrReplaceInTx(list);
    }

    /**
     * 鏍规嵁path鏇存柊Cursor,鍜屽噯澶囨挱鏀捐祫婧?     *
     * @param path   鏈湴璺緞鎴栫綉缁滆矾寰?     * @param lrcUrl url
     * @return 鎴愬姛涓庡惁
     */
    private boolean openFile(String path, String lrcUrl, long songId) {
        CommonLogger.e("鎵撳紑鏂囦欢鐨勮矾寰? + path);
        synchronized (this) {
            if (path == null) {
                return false;
            }
            wrappedMediaPlayer.setDataSource(path, lrcUrl, songId);
            if (wrappedMediaPlayer.isInitialized()) {
//                鍙鏈変竴娆℃垚鍔燂紝閲嶇疆鍙け璐ョ殑娆℃暟
                mOpenFailedCounter = 0;
                return true;
            }
            return false;
        }
    }


    private class MusicServiceHandler extends Handler {
        private WeakReference<MusicService> musicServiceWeakReference;

        MusicServiceHandler(Looper looper, MusicService musicService) {
            super(looper);
            musicServiceWeakReference = new WeakReference<>(musicService);
        }


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FADEDOWN:
                    mCurrentVolume -= 0.05f;
                    if (mCurrentVolume > 0.2f) {
                        sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = 0.2f;
                    }
                    musicServiceWeakReference.get().setVolume(mCurrentVolume);
                    break;
                case FADEUP:
                    mCurrentVolume += 0.01f;
                    if (mCurrentVolume < 1.0f) {
                        sendEmptyMessageDelayed(FADEUP, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    musicServiceWeakReference.get().setVolume(mCurrentVolume);
                    break;
                default:
                    break;
            }

        }
    }

    private void setVolume(float volume) {
        wrappedMediaPlayer.setVolume(volume);
    }


    /**
     * 鑾峰彇骞跺噯澶囩幇鍦ㄧ殑鏇茬洰璧勬簮鍜屼笅涓€棣栬祫婧?     */
    private void openCurrentAndNext() {
        openCurrentAndMaybeNext(true);
    }

    private void openCurrentAndMaybeNext(boolean openNext) {
        CommonLogger.e("鑾峰彇骞跺噯澶囩幇鍦ㄧ殑鏇茬洰璧勬簮鍜屼笅涓€棣栬祫婧?);
        synchronized (this) {
            if (mPlaylist.size() == 0) {
                return;
            }
            boolean shutdown = false;
            final MusicPlayBean musicPlayBean = mPlaylist.get(playPosition);
            if (musicPlayBean.isLocal()) {
                while (true) {
                    if (openFile(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + musicPlayBean.getSongId(), musicPlayBean.getLrcUrl(), musicPlayBean.getSongId())) {
                        break;
                    }
                    CommonLogger.e("涓婇潰鎵撳紑鏂囦欢澶辫触锛岃繘琛屼笅闈㈢殑鎿嶄綔");
                    if (mOpenFailedCounter++ < 10 && mPlaylist.size() > 1) {
                        CommonLogger.e("鏇存柊涓嬩竴棣栨瓕鏇茬殑Cursor");
//                    鍒囨崲涓嬩竴棣?                        int position = getNextPosition(true);
                        if (position < 0) {
                            shutdown = true;
                            break;
                        }
                        playPosition = position;
                    } else {
//                    澶氭閲嶅澶辫触锛岄噸缃叧闂?                        mOpenFailedCounter = 0;
                        shutdown = true;
                        break;
                    }
                }
            } else {
                shutdown = !openFile(musicPlayBean.getSongUrl(), musicPlayBean.getLrcUrl(), musicPlayBean.getSongId());
            }
            if (shutdown) {
                stop(true);
            } else if (openNext) {
                setNextMusicPlayInfo();
            }
        }
    }

    private void loadLrc(final long songId, String url) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                return Httputil.getContent(params[0]);
            }

            @Override
            protected void onPostExecute(String content) {
                if (content != null) {
                    FileUtil.writeToFile(MusicUtil.getLyricPath(songId), content);
                } else {
                    CommonLogger.e("鍐呭涓虹┖?");
                }
            }
        }.execute(url);
    }

    private void setNextMusicPlayInfo() {
        setNextMusicPlayInfo(getNextPosition(true));
    }

    private void setNextMusicPlayInfo(int nextPosition) {
        CommonLogger.e("璁剧疆涓嬩竴棣栫殑璧勬簮");
        nextPlayPosition = nextPosition;
        if (nextPlayPosition >= 0 && mPlaylist != null && nextPlayPosition < mPlaylist.size()) {
            MusicPlayBean nextBean = mPlaylist.get(nextPosition);
            if (nextBean.isLocal()) {
                wrappedMediaPlayer.setNextDataSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + nextBean.getSongId(), nextBean.getLrcUrl(), nextBean.getSongId());
            } else {
                wrappedMediaPlayer.setNextDataSource(nextBean.getSongUrl(), nextBean.getLrcUrl(), nextBean.getSongId());
            }
        }
    }

    /**
     * 鏍规嵁閫夋嫨鐨勬挱鏀鹃『搴忕被鍨嬫潵鍐冲畾涓嬩竴棣栨挱鏀剧殑浣嶇疆
     *
     * @param force 鏄惁寮哄埗鑾峰彇涓嬩竴涓挱鏀句綅缃?     * @return 鎾斁浣嶇疆
     */
    private int getNextPosition(boolean force) {
        CommonLogger.e("鑾峰彇涓嬩竴棣栨瓕鏇茬殑浣嶇疆" + force);
        if (mPlaylist == null || mPlaylist.size() == 0) {
            return -1;
        }
        if (mode == MODE_LOOP) {
//            寰幆鎾斁
            return playPosition;
        } else if (mode == MODE_SHUFFLE) {
//            闅忔満閫夋嫨妯″紡
            final int numTracks = mPlaylist.size();
            final int[] trackNumPlays = new int[numTracks]; //鐢ㄦ潵璁板綍姣忛姝屾挱鏀剧殑娆℃暟
            for (int i = 0; i < numTracks; i++) {
                trackNumPlays[i] = 0;
            }
            final int numHistory = mHistory.size();
            for (int i = 0; i < numHistory; i++) { //閬嶅巻鍘嗗彶璁板綍,璁板綍姣忛姝屾挱鏀剧殑娆℃暟
                final int idx = mHistory.get(i);
                if (idx >= 0 && idx < numTracks) {
                    trackNumPlays[idx]++;
                }
            }
            if (playPosition >= 0 && playPosition < numTracks) {
                trackNumPlays[playPosition]++;
            }
            int minNumPlays = Integer.MAX_VALUE; //鎵€鏈夋瓕鏇蹭腑鎾斁娆℃暟鏈€灏戠殑鏁扮洰
            int numTracksWithMinNumPlays = 0; //鎾斁娆℃暟涓€鏍锋渶灏戠殑姝屾洸鐨勬暟鐩?            for (int trackNumPlay : trackNumPlays) {
                if (trackNumPlay < minNumPlays) {
                    minNumPlays = trackNumPlay;
                    numTracksWithMinNumPlays = 1;
                } else if (trackNumPlay == minNumPlays) {
                    numTracksWithMinNumPlays++;
                }
            }
//            鍦ㄧ浉鍚屾挱鏀炬鏁版渶灏戠殑姝屾洸涓殢鏈洪€夋嫨
            int skip = mShuffler.nextInt(numTracksWithMinNumPlays);
            for (int i = 0; i < trackNumPlays.length; i++) {//閬嶅巻,鏍规嵁闅忔満鏁板瓧閫夊彇瀵瑰簲鐨勬瓕鏇?                if (trackNumPlays[i] == minNumPlays) {
                    if (skip == 0) {
                        return i;
                    } else {
                        skip--;
                    }
                }
            }
            return -1;
        } else {
//            姝ｅ父鐨勯『搴忔挱鏀?            if (playPosition >= mPlaylist.size() - 1) {
                return 0;
            } else {
                return playPosition + 1;
            }
        }
    }


    private class WrappedMediaPlayer implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener {
        private WeakReference<MusicService> musicServiceWeakReference;
        private MediaPlayer mediaPlayer;
        private MediaPlayer nextMediaPlayer;
        private boolean isInitialized;
        private int secondProgress;

        WrappedMediaPlayer(MusicService musicService) {
            musicServiceWeakReference = new WeakReference<>(musicService);
            mediaPlayer = new MediaPlayer();
//            璁剧疆浣庤€楃數妯″紡
            mediaPlayer.setWakeMode(musicServiceWeakReference.get(), PowerManager.PARTIAL_WAKE_LOCK);
        }


        void setDataSource(String path, String lrcUrl, long songId) {
            isInitialized = realSetDataSource(mediaPlayer, path, lrcUrl, songId);
        }


        void setNextDataSource(String path, String lrcUrl, long songId) {
            CommonLogger.e("璁剧疆涓嬩竴棣栨瓕鐨勮祫婧? + path);
            if (path == null) {
                return;
            }
            try {
                mediaPlayer.setNextMediaPlayer(null);
            } catch (Exception e) {
                e.printStackTrace();
                CommonLogger.e("璁剧疆涓嬩竴涓挱鏀惧櫒鍑洪敊" + e.getMessage());
            }
            if (nextMediaPlayer != null) {
                nextMediaPlayer.release();
                nextMediaPlayer = null;
            }
            nextMediaPlayer = new MediaPlayer();
            nextMediaPlayer.setWakeMode(musicServiceWeakReference.get(), PowerManager.PARTIAL_WAKE_LOCK);
            if (realSetDataSource(nextMediaPlayer, path, lrcUrl, songId)) {
                mediaPlayer.setNextMediaPlayer(nextMediaPlayer);
            } else {
                nextMediaPlayer.release();
                nextMediaPlayer = null;
            }
        }

        private boolean realSetDataSource(MediaPlayer mediaPlayer, String path, String lrcUrl, long songId) {
            CommonLogger.e("璁剧疆鎾斁鐨勮祫婧愯矾寰? + path);
            if (path == null) {
                return false;
            }
            try {
                if (lrcUrl != null) {
                    loadLrc(songId, lrcUrl);
                }
                mediaPlayer.reset();
                mediaPlayer.setOnPreparedListener(null);
                if (path.startsWith("content://")) {
                    mediaPlayer.setDataSource(musicServiceWeakReference.get(), Uri.parse(path));
                    mediaPlayer.setOnBufferingUpdateListener(null);
                } else {
                    mediaPlayer.setOnBufferingUpdateListener(this);
                    mediaPlayer.setDataSource(path);
                }
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                CommonLogger.e("鍑嗗鎾斁璧勬簮鐨勬椂鍊欏嚭閿? + e.getMessage());
                return false;
            } catch (IllegalStateException e) {
                CommonLogger.e("鍑嗗鎾斁璧勬簮鐨勬椂鍊欏嚭閿?2" + e.getMessage());
            }
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            return true;
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
//            杩欓噷杩斿洖true,闃叉鐩存帴璋冪敤onCompletion鏂规硶锛屽鑷翠笉鏂垏鎹笅涓€棣?            CommonLogger.e("12鎾斁澹伴煶鍑洪敊" + what + "  " + extra);
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    break;
                default:
                    break;
            }
            return true;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mp == mediaPlayer && nextMediaPlayer != null) {
                prepareNext(false);
                notifyChange(META_CHANGED);
                CommonLogger.e("鎾斁瀹?杩欓噷浼氱户缁挱鏀句笅涓€棣栧悧");
            }
        }

        boolean isInitialized() {
            return isInitialized;
        }

        public long duration() {
            if (mediaPlayer!=null) {
                return mediaPlayer.getDuration();
            }else {
                return -1;
            }
        }

        void start() {
            CommonLogger.e("鎾斁鐪熸寮€濮嬪暒鍟?);
            if (mediaPlayer!=null) {
                mediaPlayer.start();
            }
        }

        public long position() {
            return mediaPlayer.getCurrentPosition();
        }

        void stop() {
            mediaPlayer.stop();
        }

        long seek(long position) {
            mediaPlayer.seekTo((int) position);
            return position;
        }


        void pause() {
            CommonLogger.e("鐪熸鍋滄");
            mediaPlayer.pause();
            setIsSupposedToBePlaying(false, true);
        }

        void setVolume(float volume) {
            mediaPlayer.setVolume(volume, volume);
        }

        void release() {
            if (mediaPlayer!=null) {
                mediaPlayer.release();
            }
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            CommonLogger.e("杩欓噷缂撳瓨");
            if (mp == mediaPlayer) {
//                鍙幏鍙栫幇鍦ㄦ挱鏀剧殑鍔犺浇杩涘害鏉?鍥犱负鎾斁涓€棣栨椂浼氭彁鍓嶅姞杞戒笅涓€棣?                secondProgress = (int) (percent * (mp.getDuration()) / 100.0);
                CommonLogger.e("鍔犺浇缂撳瓨杩涘害" + secondProgress);
                notifyChange(BUFFER_UPDATE_CHANGED);
            }
        }

        int getSecondProgress() {
            return secondProgress;
        }

        int getAudioSessionId() {
            return mediaPlayer.getAudioSessionId();
        }
    }

    private void prepareNext(boolean force) {
        synchronized (this) {
            if (wrappedMediaPlayer.mediaPlayer != null) {
                wrappedMediaPlayer.mediaPlayer.release();
                wrappedMediaPlayer.mediaPlayer = wrappedMediaPlayer.nextMediaPlayer;
                wrappedMediaPlayer.nextMediaPlayer = null;
                if (mode != MODE_SHUFFLE) {
                    //                濡傛灉鏄殢鏈洪渶瑕佹坊鍔犲埌鍘嗗彶鎾斁璁板綍涓?                    mHistory.add(playPosition);
                    if (mHistory.size() > MAX_HISTORY_SIZE) {
                        mHistory.remove(0);
                    }
                }
                playPosition = nextPlayPosition;
                nextPlayPosition = getNextPosition(force);
                CommonLogger.e("涓嬩竴棣栦綅缃? + nextPlayPosition);
                setNextMusicPlayInfo(nextPlayPosition);
                play();
            }
        }
    }


    private String getSongName() {
        CommonLogger.e("鑾峰彇姝屽悕");
        synchronized (this) {
            if (getCurrentMusicPlayInfo() != null) {
                return getCurrentMusicPlayInfo().getSongName();
            }
            return null;
        }
    }

    private long getAudioId() {
        CommonLogger.e("getAudioId");
        MusicPlayBean musicPlayInfo = getCurrentMusicPlayInfo();
        CommonLogger.e("杩欓噷鍒颁簡鍚?);
        if (musicPlayInfo != null) {
            CommonLogger.e("1杩欓噷鍒颁簡鍚?);
            return musicPlayInfo.getSongId();
        }
        return -1;
    }


    private MusicPlayBean getCurrentMusicPlayInfo() {
        CommonLogger.e("getCurrentMusicPlayInfo" + playPosition);
        return getMusicPlayInfo(playPosition);
    }

    private MusicPlayBean getMusicPlayInfo(int position) {
        if (position >= 0 && mPlaylist != null && position < mPlaylist.size()) {
            return mPlaylist.get(position);
        }
        CommonLogger.e("getMusicPlayInfo" + position);
        return null;
    }


    private static final class Shuffler {


        private final LinkedList<Integer> mHistoryOfNumbers = new LinkedList<>();

        private final TreeSet<Integer> mPreviousNumbers = new TreeSet<>();

        private final Random mRandom = new Random();

        private int mPrevious;


        Shuffler() {
            super();
        }

        int nextInt(final int interval) {
            CommonLogger.e("闅忔満鎾斁鐨勬椂鍊欒幏鍙栦笅涓€棣栨瓕鐨勪綅缃? + interval);
            int next;
            do {
                next = mRandom.nextInt(interval);
            } while (!(next != mPrevious && interval > 1
                    && !mPreviousNumbers.contains(next)));
            mPrevious = next;
            mHistoryOfNumbers.add(mPrevious);
            mPreviousNumbers.add(mPrevious);
            if (!mHistoryOfNumbers.isEmpty() && mHistoryOfNumbers.size() >= MAX_HISTORY_SIZE) {
                for (int i = 0; i < Math.max(1, MAX_HISTORY_SIZE / 2); i++) {
                    mPreviousNumbers.remove(mHistoryOfNumbers.removeFirst());
                }
            }
            return next;
        }
    }
}

