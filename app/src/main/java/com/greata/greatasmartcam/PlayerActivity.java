package com.greata.greatasmartcam;

import android.app.Activity;

import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.Bundle;

import android.os.Handler;

import android.support.annotation.NonNull;

import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import android.util.Log;
import android.view.KeyEvent;

import android.view.View;

import android.view.View.OnClickListener;

import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.Button;

import android.widget.FrameLayout;

import android.widget.ImageButton;
import android.widget.LinearLayout;

import android.widget.ProgressBar;
import android.widget.TextView;

import android.widget.Toast;

import com.google.android.exoplayer2.C;

import com.google.android.exoplayer2.DefaultRenderersFactory;

import com.google.android.exoplayer2.ExoPlaybackException;

import com.google.android.exoplayer2.ExoPlayerFactory;

import com.google.android.exoplayer2.PlaybackParameters;

import com.google.android.exoplayer2.Player;

import com.google.android.exoplayer2.Player.EventListener;

import com.google.android.exoplayer2.SimpleExoPlayer;

import com.google.android.exoplayer2.Timeline;

import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;

import com.google.android.exoplayer2.drm.DrmSessionManager;

import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;

import com.google.android.exoplayer2.drm.FrameworkMediaDrm;

import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;

import com.google.android.exoplayer2.drm.UnsupportedDrmException;

import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;

import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;

import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;

import com.google.android.exoplayer2.source.BehindLiveWindowException;

import com.google.android.exoplayer2.source.ConcatenatingMediaSource;

import com.google.android.exoplayer2.source.ExtractorMediaSource;

import com.google.android.exoplayer2.source.MediaSource;

import com.google.android.exoplayer2.source.TrackGroupArray;

import com.google.android.exoplayer2.source.dash.DashMediaSource;

import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;

import com.google.android.exoplayer2.source.hls.HlsMediaSource;

import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;

import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;

import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;

import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;

import com.google.android.exoplayer2.trackselection.TrackSelection;

import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import com.google.android.exoplayer2.ui.DebugTextViewHelper;

import com.google.android.exoplayer2.ui.PlaybackControlView;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import com.google.android.exoplayer2.upstream.DataSource;

import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import com.google.android.exoplayer2.upstream.HttpDataSource;

import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

import java.lang.reflect.Method;

import java.net.CookieHandler;

import java.net.CookieManager;

import java.net.CookiePolicy;

import java.util.UUID;


/**
 * An activity that plays media using {@link SimpleExoPlayer}.
 */

public class PlayerActivity extends Activity implements OnClickListener, EventListener,

        PlaybackControlView.VisibilityListener {

    public static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";

    public static final String DRM_LICENSE_URL = "drm_license_url";

    public static final String DRM_KEY_REQUEST_PROPERTIES = "drm_key_request_properties";

    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";


    public static final String ACTION_VIEW = "com.google.android.exoplayer.demo.action.VIEW";

    public static final String EXTENSION_EXTRA = "extension";


    public static final String ACTION_VIEW_LIST =

            "com.google.android.exoplayer.demo.action.VIEW_LIST";

    public static final String URI_LIST_EXTRA = "uri_list";

    public static final String EXTENSION_LIST_EXTRA = "extension_list";

    public static final String AD_TAG_URI_EXTRA = "ad_tag_uri";


    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {

        DEFAULT_COOKIE_MANAGER = new CookieManager();

        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);

    }


    private ProgressBar mProgressBar;

    private Handler mainHandler;

    private EventLogger eventLogger;

    private SimpleExoPlayerView simpleExoPlayerView;

    private LinearLayout debugRootView;

    private TextView debugTextView;
    private TextView playerTitle;

    private Button retryButton;
    private ImageButton lockScreenButton;

    private DataSource.Factory mediaDataSourceFactory;

    private SimpleExoPlayer player;

    private DefaultTrackSelector trackSelector;

    private TrackSelectionHelper trackSelectionHelper;

    private DebugTextViewHelper debugViewHelper;

    private boolean inErrorState;

    private TrackGroupArray lastSeenTrackGroupArray;


    private boolean shouldAutoPlay;

    private int resumeWindow;

    private long resumePosition;


    // Fields used only for ad playback. The ads loader is loaded via reflection.


    private Object imaAdsLoader; // com.google.android.exoplayer2.ext.ima.ImaAdsLoader

    private Uri loadedAdTagUri;

    private ViewGroup adOverlayViewGroup;
    // Activity lifecycle

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {

        if (e.type != ExoPlaybackException.TYPE_SOURCE) {

            return false;

        }

        Throwable cause = e.getSourceException();

        while (cause != null) {

            if (cause instanceof BehindLiveWindowException) {

                return true;

            }

            cause = cause.getCause();

        }

        return false;

    }

    public void playerOnClick(View view) {
        switch (view.getId()) {
            case R.id.lockscreen_btn:
                Log.d("Test", "" + getRequestedOrientation());
                if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LOCKED) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
                break;
        }
    }

    public void saveBitmap() {
        Log.e("save", "保存图片");
        File f = new File(this.getExternalFilesDir(null), Long.toString(System.currentTimeMillis()) + ".png");
        if (f.exists()) {
            f.delete();
        }
        try {

            //Bitmap bm = simpleExoPlayerView.getBitmap();
            FileOutputStream out = new FileOutputStream(f);
            //bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i("save", "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public boolean isLandscape() {
        /*   * 通过API动态改变当前屏幕的显示方向   */

        // 取得当前屏幕方向
        WindowManager wm = this.getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        return width > height;
    }

    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        shouldAutoPlay = false;

        clearResumePosition();

        mediaDataSourceFactory = buildDataSourceFactory(true);

        mainHandler = new Handler();

        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {

            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);

        }


        setContentView(R.layout.player_activity);

        lockScreenButton = findViewById(R.id.lockscreen_btn);
        if (!isLandscape()) {
            lockScreenButton.setVisibility(View.GONE);
        }

        View rootView = findViewById(R.id.root);

        rootView.setOnClickListener(this);

        debugRootView = findViewById(R.id.controls_root);

        debugTextView = findViewById(R.id.debug_text_view);

        retryButton = findViewById(R.id.retry_button);

        retryButton.setOnClickListener(this);

        playerTitle = findViewById(R.id.player_title);
        playerTitle.setText(getIntent().getCharSequenceExtra("title"));

        simpleExoPlayerView = findViewById(R.id.player_view);
        simpleExoPlayerView.hideController();

        mProgressBar = findViewById(R.id.progressBar_play);
        mProgressBar.setVisibility(View.INVISIBLE);

        simpleExoPlayerView.setControllerVisibilityListener(this);

        simpleExoPlayerView.requestFocus();
        showNormalDialog();


    }

    private void showNormalDialog() {
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(PlayerActivity.this);
        normalDialog.setMessage("你的网络不是wifi，是否继续");
        normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                shouldAutoPlay = true;
                player.setPlayWhenReady(shouldAutoPlay);
                if(!player.isLoading()){
                    // TODO: 2017/10/25 not start to play before on click
                }
            }
        });
        normalDialog.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                PlayerActivity.this.finish();
            }
        });
        // 显示
        normalDialog.show();
    }

    @Override

    public void onNewIntent(Intent intent) {

        releasePlayer();

        shouldAutoPlay = true;

        clearResumePosition();

        setIntent(intent);

    }

    @Override

    public void onStart() {

        super.onStart();

        if (Util.SDK_INT > 23) {

            initializePlayer();

        }

    }

    @Override

    public void onResume() {

        super.onResume();

        if ((Util.SDK_INT <= 23 || player == null)) {

            initializePlayer();

        }

    }

    @Override

    public void onPause() {

        super.onPause();

        if (Util.SDK_INT <= 23) {

            releasePlayer();

        }

    }

    @Override

    public void onStop() {

        super.onStop();

        if (Util.SDK_INT > 23) {

            releasePlayer();

        }

    }

    @Override

    public void onDestroy() {

        super.onDestroy();

        releaseAdsLoader();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "横屏模式", Toast.LENGTH_SHORT).show();
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            lockScreenButton.setVisibility(View.VISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "竖屏模式", Toast.LENGTH_SHORT).show();
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            lockScreenButton.setVisibility(View.GONE);
        }
        Log.d("Test", "rotate");
    }


    // Activity input

    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,

                                           @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            initializePlayer();

        } else {

            showToast(R.string.storage_permission_denied);

            finish();

        }

    }


    // OnClickListener methods

    @Override

    public boolean dispatchKeyEvent(KeyEvent event) {

        // If the event was not handled then see if the player view can handle it.

        return super.dispatchKeyEvent(event) || simpleExoPlayerView.dispatchKeyEvent(event);

    }


    // PlaybackControlView.VisibilityListener implementation

    @Override

    public void onClick(View view) {

        if (view == retryButton) {

            initializePlayer();

        } else if (view.getParent() == debugRootView) {

            MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

            if (mappedTrackInfo != null) {

                trackSelectionHelper.showSelectionDialog(this, ((Button) view).getText(),

                        trackSelector.getCurrentMappedTrackInfo(), (int) view.getTag());

            }

        }

    }


    // Internal methods

    @Override

    public void onVisibilityChange(int visibility) {

        debugRootView.setVisibility(visibility);

    }

    private void initializePlayer() {

        Intent intent = getIntent();

        boolean needNewPlayer = player == null;

        if (needNewPlayer) {

            TrackSelection.Factory adaptiveTrackSelectionFactory =

                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);

            trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);

            trackSelectionHelper = new TrackSelectionHelper(trackSelector, adaptiveTrackSelectionFactory);

            lastSeenTrackGroupArray = null;

            eventLogger = new EventLogger(trackSelector);


            UUID drmSchemeUuid = intent.hasExtra(DRM_SCHEME_UUID_EXTRA)

                    ? UUID.fromString(intent.getStringExtra(DRM_SCHEME_UUID_EXTRA)) : null;

            DrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;

            if (drmSchemeUuid != null) {

                String drmLicenseUrl = intent.getStringExtra(DRM_LICENSE_URL);

                String[] keyRequestPropertiesArray = intent.getStringArrayExtra(DRM_KEY_REQUEST_PROPERTIES);

                int errorStringId = R.string.error_drm_unknown;

                if (Util.SDK_INT < 18) {

                    errorStringId = R.string.error_drm_not_supported;

                } else {

                    try {

                        drmSessionManager = buildDrmSessionManagerV18(drmSchemeUuid, drmLicenseUrl,

                                keyRequestPropertiesArray);

                    } catch (UnsupportedDrmException e) {

                        errorStringId = e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME

                                ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;

                    }

                }

                if (drmSessionManager == null) {

                    showToast(errorStringId);

                    return;

                }

            }


            boolean preferExtensionDecoders = intent.getBooleanExtra(PREFER_EXTENSION_DECODERS, false);

            @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =

                    ((DemoApplication) getApplication()).useExtensionRenderers()

                            ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER

                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)

                            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;

            DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this,

                    drmSessionManager, extensionRendererMode);


            player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector);

            player.addListener(this);

            player.addListener(eventLogger);

            player.addMetadataOutput(eventLogger);

            player.setAudioDebugListener(eventLogger);

            player.setVideoDebugListener(eventLogger);


            simpleExoPlayerView.setPlayer(player);

            player.setPlayWhenReady(shouldAutoPlay);

            debugViewHelper = new DebugTextViewHelper(player, debugTextView);

            debugViewHelper.start();

        }

        String action = intent.getAction();

        Uri[] uris;

        String[] extensions;

        if (ACTION_VIEW.equals(action)) {

            uris = new Uri[]{intent.getData()};

            extensions = new String[]{intent.getStringExtra(EXTENSION_EXTRA)};

        } else if (ACTION_VIEW_LIST.equals(action)) {

            String[] uriStrings = intent.getStringArrayExtra(URI_LIST_EXTRA);

            uris = new Uri[uriStrings.length];

            for (int i = 0; i < uriStrings.length; i++) {

                uris[i] = Uri.parse(uriStrings[i]);

            }

            extensions = intent.getStringArrayExtra(EXTENSION_LIST_EXTRA);

            if (extensions == null) {

                extensions = new String[uriStrings.length];

            }

        } else {

            showToast(getString(R.string.unexpected_intent_action, action));

            return;

        }

        if (Util.maybeRequestReadExternalStoragePermission(this, uris)) {

            // The player will be reinitialized if the permission is granted.

            return;

        }

        MediaSource[] mediaSources = new MediaSource[uris.length];

        for (int i = 0; i < uris.length; i++) {

            mediaSources[i] = buildMediaSource(uris[i], extensions[i]);

        }

        MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]

                : new ConcatenatingMediaSource(mediaSources);

        String adTagUriString = intent.getStringExtra(AD_TAG_URI_EXTRA);

        if (adTagUriString != null) {

            Uri adTagUri = Uri.parse(adTagUriString);

            if (!adTagUri.equals(loadedAdTagUri)) {

                releaseAdsLoader();

                loadedAdTagUri = adTagUri;

            }

            try {

                mediaSource = createAdsMediaSource(mediaSource, Uri.parse(adTagUriString));

            } catch (Exception e) {

                showToast(R.string.ima_not_loaded);

            }

        } else {

            releaseAdsLoader();

        }

        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;

        if (haveResumePosition) {

            player.seekTo(resumeWindow, resumePosition);

        }

        player.prepare(mediaSource, !haveResumePosition, false);

        inErrorState = false;

        updateButtonVisibilities();

    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {

        int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)

                : Util.inferContentType("." + overrideExtension);

        switch (type) {

            case C.TYPE_SS:

                return new SsMediaSource(uri, buildDataSourceFactory(false),

                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);

            case C.TYPE_DASH:

                return new DashMediaSource(uri, buildDataSourceFactory(false),

                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory), mainHandler, eventLogger);

            case C.TYPE_HLS:

                return new HlsMediaSource(uri, mediaDataSourceFactory, mainHandler, eventLogger);

            case C.TYPE_OTHER:

                return new ExtractorMediaSource(uri, mediaDataSourceFactory, new DefaultExtractorsFactory(),

                        mainHandler, eventLogger);

            default: {

                throw new IllegalStateException("Unsupported type: " + type);

            }

        }

    }

    private DrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(UUID uuid,

                                                                              String licenseUrl, String[] keyRequestPropertiesArray) throws UnsupportedDrmException {

        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl,

                buildHttpDataSourceFactory(false));

        if (keyRequestPropertiesArray != null) {

            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {

                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],

                        keyRequestPropertiesArray[i + 1]);

            }

        }

        return new DefaultDrmSessionManager<>(uuid, FrameworkMediaDrm.newInstance(uuid), drmCallback,

                null, mainHandler, eventLogger);

    }

    private void releasePlayer() {

        if (player != null) {

            debugViewHelper.stop();

            debugViewHelper = null;

            shouldAutoPlay = player.getPlayWhenReady();

            updateResumePosition();

            player.release();

            player = null;

            trackSelector = null;

            trackSelectionHelper = null;

            eventLogger = null;

        }

    }

    private void updateResumePosition() {

        resumeWindow = player.getCurrentWindowIndex();

        resumePosition = Math.max(0, player.getContentPosition());

    }

    private void clearResumePosition() {

        resumeWindow = C.INDEX_UNSET;

        resumePosition = C.TIME_UNSET;

    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          <p>
     *                          DataSource factory.
     * @return A new DataSource factory.
     */

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {

        return ((DemoApplication) getApplication())

                .buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);

    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          <p>
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */

    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {

        return ((DemoApplication) getApplication())

                .buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);

    }

    /**
     * Returns an ads media source, reusing the ads loader if one exists.
     *
     * @throws Exception Thrown if it was not possible to create an ads media source, for example, due
     *                   <p>
     *                   to a missing dependency.
     */

    private MediaSource createAdsMediaSource(MediaSource mediaSource, Uri adTagUri) throws Exception {

        // Load the extension source using reflection so the demo app doesn't have to depend on it.

        // The ads loader is reused for multiple playbacks, so that ad playback can resume.

        Class<?> loaderClass = Class.forName("com.google.android.exoplayer2.ext.ima.ImaAdsLoader");

        if (imaAdsLoader == null) {

            imaAdsLoader = loaderClass.getConstructor(Context.class, Uri.class)

                    .newInstance(this, adTagUri);

            adOverlayViewGroup = new FrameLayout(this);

            // The demo app has a non-null overlay frame layout.

            simpleExoPlayerView.getOverlayFrameLayout().addView(adOverlayViewGroup);

        }

        Class<?> sourceClass =

                Class.forName("com.google.android.exoplayer2.ext.ima.ImaAdsMediaSource");

        Constructor<?> constructor = sourceClass.getConstructor(MediaSource.class,

                DataSource.Factory.class, loaderClass, ViewGroup.class);

        return (MediaSource) constructor.newInstance(mediaSource, mediaDataSourceFactory, imaAdsLoader,

                adOverlayViewGroup);

    }


    // Player.EventListener implementation

    private void releaseAdsLoader() {

        if (imaAdsLoader != null) {

            try {

                Class<?> loaderClass = Class.forName("com.google.android.exoplayer2.ext.ima.ImaAdsLoader");

                Method releaseMethod = loaderClass.getMethod("release");

                releaseMethod.invoke(imaAdsLoader);

            } catch (Exception e) {

                // Should never happen.

                throw new IllegalStateException(e);

            }

            imaAdsLoader = null;

            loadedAdTagUri = null;

            simpleExoPlayerView.getOverlayFrameLayout().removeAllViews();

        }

    }

    @Override

    public void onLoadingChanged(boolean isLoading) {

        // Do nothing.
        if (isLoading) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }

    }

    @Override

    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == Player.STATE_ENDED) {

            showControls();

        }

        updateButtonVisibilities();

    }

    @Override

    public void onRepeatModeChanged(int repeatMode) {

        // Do nothing.

    }

    @Override

    public void onPositionDiscontinuity() {

        if (inErrorState) {

            // This will only occur if the user has performed a seek whilst in the error state. Update the

            // resume position so that if the user then retries, playback will resume from the position to

            // which they seeked.

            updateResumePosition();

        }

    }

    @Override

    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        // Do nothing.

    }

    @Override

    public void onTimelineChanged(Timeline timeline, Object manifest) {

        // Do nothing.

    }

    @Override

    public void onPlayerError(ExoPlaybackException e) {

        String errorString = null;

        if (e.type == ExoPlaybackException.TYPE_RENDERER) {

            Exception cause = e.getRendererException();

            if (cause instanceof DecoderInitializationException) {

                // Special case for decoder initialization failures.

                DecoderInitializationException decoderInitializationException =

                        (DecoderInitializationException) cause;

                if (decoderInitializationException.decoderName == null) {

                    if (decoderInitializationException.getCause() instanceof DecoderQueryException) {

                        errorString = getString(R.string.error_querying_decoders);

                    } else if (decoderInitializationException.secureDecoderRequired) {

                        errorString = getString(R.string.error_no_secure_decoder,

                                decoderInitializationException.mimeType);

                    } else {

                        errorString = getString(R.string.error_no_decoder,

                                decoderInitializationException.mimeType);

                    }

                } else {

                    errorString = getString(R.string.error_instantiating_decoder,

                            decoderInitializationException.decoderName);

                }

            }

        }

        if (errorString != null) {

            showToast(errorString);

        }

        inErrorState = true;

        if (isBehindLiveWindow(e)) {

            clearResumePosition();

            initializePlayer();

        } else {

            updateResumePosition();

            updateButtonVisibilities();

            showControls();

        }

    }


    // User controls

    @Override

    @SuppressWarnings("ReferenceEquality")

    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        updateButtonVisibilities();

        if (trackGroups != lastSeenTrackGroupArray) {

            MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

            if (mappedTrackInfo != null) {

                if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_VIDEO)

                        == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {

                    showToast(R.string.error_unsupported_video);

                }

                if (mappedTrackInfo.getTrackTypeRendererSupport(C.TRACK_TYPE_AUDIO)

                        == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {

                    showToast(R.string.error_unsupported_audio);

                }

            }

            lastSeenTrackGroupArray = trackGroups;

        }

    }

    private void updateButtonVisibilities() {

        debugRootView.removeAllViews();


        retryButton.setVisibility(inErrorState ? View.VISIBLE : View.GONE);

        debugRootView.addView(retryButton);


        if (player == null) {

            return;

        }


        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();

        if (mappedTrackInfo == null) {

            return;

        }


        for (int i = 0; i < mappedTrackInfo.length; i++) {

            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(i);

            if (trackGroups.length != 0) {

                Button button = new Button(this);

                int label;

                switch (player.getRendererType(i)) {

                    case C.TRACK_TYPE_AUDIO:

                        label = R.string.audio;

                        break;

                    case C.TRACK_TYPE_VIDEO:

                        label = R.string.video;

                        break;

                    case C.TRACK_TYPE_TEXT:

                        label = R.string.text;

                        break;

                    default:

                        continue;

                }

                button.setText(label);

                button.setTag(i);

                button.setOnClickListener(this);

                debugRootView.addView(button, debugRootView.getChildCount() - 1);

            }

        }

    }

    private void showControls() {

        debugRootView.setVisibility(View.VISIBLE);

    }

    private void showToast(int messageId) {

        showToast(getString(messageId));

    }

    private void showToast(String message) {

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

    }


}
