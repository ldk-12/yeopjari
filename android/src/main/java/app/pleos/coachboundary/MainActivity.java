package app.pleos.coachboundary;
import android.app.Activity;
import android.car.Car;
import android.car.drivingstate.CarUxRestrictions;
import android.car.drivingstate.CarUxRestrictionsManager;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
/** Local synthetic coach + separate SDK diagnostic label. No JS bridge. */
public final class MainActivity extends Activity {
    private static final String ASSET_HOST = "appassets.androidplatform.net";
    private static final String START_URL = "https://" + ASSET_HOST + "/index.html?offline=1";
    WebView webView;
    android.widget.TextView live;
    ai.pleos.playground.vehicle.Vehicle vehicle;
    android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());
    String gear="UNKNOWN",failure="";
    Boolean brake=null;
    long gearAt=0,brakeAt=0;
    boolean active=false,dead=false,wasAllowed=false;
    Car car;
    CarUxRestrictionsManager uxManager;
    boolean uxKnown = false, uxRequiresDo = true;
    int uxFlags = -1;
    void invalidateUx() {
        uxKnown = false;
        uxRequiresDo = true;
        uxFlags = -1;
        updateBoundary();
    }
    void acceptUx(CarUxRestrictions value) {
        if (dead) return;
        uxKnown = value != null;
        uxRequiresDo = value == null || value.isRequiresDistractionOptimization();
        uxFlags = value == null ? -1 : value.getActiveRestrictions();
        updateBoundary();
    }
    void refreshUx() {
        try {
            acceptUx(uxManager == null ? null : uxManager.getCurrentCarUxRestrictions());
        } catch (RuntimeException unavailable) {
            invalidateUx();
        }
    }
    final ServiceConnection carConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (dead) return;
            try {
                uxManager = (CarUxRestrictionsManager) car.getCarManager(Car.CAR_UX_RESTRICTION_SERVICE);
                uxManager.registerListener(value -> runOnUiThread(() -> acceptUx(value)));
                refreshUx();
            } catch (RuntimeException unavailable) {
                invalidateUx();
            }
        }
        public void onServiceDisconnected(ComponentName name) {
            uxManager = null;
            invalidateUx();
        }
    };
    final ai.pleos.playground.vehicle.listener.BrakePedalStatusListener listener=new ai.pleos.playground.vehicle.listener.BrakePedalStatusListener() {
        public void onBrakePedalStatusUpdated(Boolean value) {
            runOnUiThread(()-> {
                if(dead)return;
                brake=value;
                brakeAt=android.os.SystemClock.elapsedRealtime();
                android.util.Log.i("CoachBoundary","SDK brake callback "+value);
                updateBoundary();
            });
        }
        public void onFailed(Exception e) {
            runOnUiThread(()-> {
                brake=null;
                brakeAt=0;
                failure="SDK callback 확인 불가";
                updateBoundary();
            });
        }
    };
    boolean allowed() {
        return active && uxKnown && !uxRequiresDo && uxFlags == 0
            && gear.equals("GEAR_PARK")
            && android.os.SystemClock.elapsedRealtime() - gearAt < 2500;
    }
    void updateBoundary() {
        if(live==null||dead)return;
        long now=android.os.SystemClock.elapsedRealtime();
        boolean ok=allowed();
        live.setText("[실제 SDK 진단 · 에뮬레이터] 기어: "+gear+" / Brake boolean: "+(brake==null||now-brakeAt>3000?"확인 불가":brake)+"\n"+"아래 경로·메모·교통 장면은 전부 시연용 예시 데이터. SDK값을 비교/판단에 넣지 않습니다. "+failure+(ok?"":"\n주차/UX 제한 미확인 또는 제한 중: 화면 잠금"));
        webView.setVisibility(ok?View.VISIBLE:View.GONE);
        android.util.Log.i("IntegratedGate", "active="+active+" uxKnown="+uxKnown+" requiresDo="+uxRequiresDo+" flags="+uxFlags+" gear="+gear+" allowed="+ok+" visibility="+webView.getVisibility());
        if(!ok&&wasAllowed)webView.reload();
        wasAllowed=ok;
    }
    void poll() {
        if(dead)return;
        refreshUx();
        try {
            if(vehicle!=null) {
                vehicle.getGear().getCurrentGearSelectionState(g-> {
                    runOnUiThread(()-> {
                        gear=g==null?"UNKNOWN":g.name();
                        gearAt=android.os.SystemClock.elapsedRealtime();
                        android.util.Log.i("CoachBoundary","SDK gear "+gear);
                        updateBoundary();
                    });
                    return kotlin.Unit.INSTANCE;
                },e-> {
                    runOnUiThread(()-> {
                        gear="UNKNOWN";
                        gearAt=0;
                        updateBoundary();
                    });
                    return kotlin.Unit.INSTANCE;
                });
                vehicle.getBrake().getCurrentBrakePedalStatus(v-> {
                    runOnUiThread(()-> {
                        brake=v;
                        brakeAt=android.os.SystemClock.elapsedRealtime();
                        updateBoundary();
                    });
                    return kotlin.Unit.INSTANCE;
                },e-> {
                    runOnUiThread(()-> {
                        brake=null;
                        brakeAt=0;
                        updateBoundary();
                    });
                    return kotlin.Unit.INSTANCE;
                });
            }
        }
        catch(Throwable t) {
            gear="UNKNOWN";
            gearAt=0;
            brake=null;
            failure="SDK 연결 확인 불가";
        }
        updateBoundary();
        handler.postDelayed(()->poll(),1000);
    }
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(18, 31, 44));
        getWindow().setNavigationBarColor(Color.rgb(18, 31, 44));
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.rgb(18, 31, 44));
        getWindow().setDecorFitsSystemWindows(false);
        container.setOnApplyWindowInsetsListener((view, insets) -> {
            android.graphics.Insets bars = insets.getInsets(
                android.view.WindowInsets.Type.systemBars() | android.view.WindowInsets.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        TextView badge = new TextView(this);
        badge.setText("아래 시연은 시연용 예시 데이터 · 위쪽 SDK 진단과 연결되지 않음");
        badge.setTextSize(12);
        badge.setTextColor(Color.WHITE);
        int padding = (int) (10 * getResources().getDisplayMetrics().density);
        badge.setPadding(padding, padding / 2, padding, padding / 2);
        badge.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        container.addView(badge, new LinearLayout.LayoutParams(-1, -2));
        live=new TextView(this);
        live.setTextColor(Color.WHITE);
        live.setTextSize(16);
        live.setPadding(16,12,16,12);
        container.addView(live,new LinearLayout.LayoutParams(-1,-2));
        WebView.setWebContentsDebuggingEnabled(false);
        webView = new WebView(this);
        configureWebView();
        container.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        webView.setVisibility(View.GONE);
        setContentView(container);
        container.requestApplyInsets();
        try {
            car = Car.createCar(this, carConnection);
            if (car != null) car.connect();
        } catch (RuntimeException unavailable) {
            invalidateUx();
        }
        webView.loadUrl(START_URL);
        updateBoundary();
        try {
            vehicle=new ai.pleos.playground.vehicle.Vehicle(this);
            vehicle.initialize();
            handler.postDelayed(()-> {
                try {
                    vehicle.getBrake().checkBrakePedalStatusCapabilities(c-> {
                        android.util.Log.i("CoachBoundary","SDK Brake capability "+c);
                        vehicle.getBrake().registerBrakePedalStatus(listener);
                        return kotlin.Unit.INSTANCE;
                    },e-> {
                        failure="Brake 지원 확인 불가";
                        return kotlin.Unit.INSTANCE;
                    });
                }
                catch(Throwable t) {
                    failure="SDK 초기화 확인 불가";
                }
                poll();
            },1800);
        }
        catch(Throwable t) {
            failure="SDK 연결 확인 불가";
        }
    }
    @SuppressWarnings("deprecation") private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        // Only packaged, allowlisted scripts below.
        settings.setDomStorageEnabled(false);
        // Demo state is intentionally in memory.
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setBlockNetworkLoads(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setGeolocationEnabled(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        CookieManager.getInstance().setAcceptCookie(false);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, false);
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest( WebView view, WebResourceRequest request) {
                if (!"GET".equals(request.getMethod())) return blocked();
                String asset = allowedAsset(request.getUrl());
                if (asset == null) return blocked();
                try {
                    InputStream stream = getAssets().open(asset);
                    Map<String, String> headers = securityHeaders();
                    return new WebResourceResponse(mimeType(asset), "UTF-8", 200, "OK", headers, stream);
                }
                catch (IOException missing) {
                    return blocked();
                }
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !isLocalDocument(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !isLocalDocument(Uri.parse(url));
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                request.deny();
            }
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, false, false);
            }
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                callback.onReceiveValue(null);
                return true;
            }
        });
        // Never dispatch downloads, intents, links, or external activities.
        webView.setDownloadListener((url, userAgent, disposition, mime, size) -> {
        });
    }
    private static String allowedAsset(Uri uri) {
        if (!"https".equals(uri.getScheme()) || !ASSET_HOST.equals(uri.getHost()) || uri.getPort() != -1 || uri.getUserInfo() != null) return null;
        String path = uri.getEncodedPath();
        if (path == null || path.contains("%") || path.contains("..") || path.contains("\\")) return null;
        if ("/index.html".equals(path) || "/app.js".equals(path) || "/engine.js".equals(path) || "/styles.css".equals(path)) {
            return path.substring(1);
        }
        if (path.matches("/assets/[A-Za-z0-9_/-]+\\.svg")) return path.substring(1);
        return null;
    }
    private static boolean isLocalDocument(Uri uri) {
        return "index.html".equals(allowedAsset(uri));
    }
    private static String mimeType(String asset) {
        if (asset.endsWith(".js")) return "application/javascript";
        if (asset.endsWith(".css")) return "text/css";
        if (asset.endsWith(".svg")) return "image/svg+xml";
        return "text/html";
    }
    private static Map<String, String> securityHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Security-Policy", "default-src 'none'; " + "script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; " + "connect-src 'none'; media-src 'none'; object-src 'none'; " + "frame-src 'none'; worker-src 'none'; base-uri 'none'; form-action 'none'");
        headers.put("X-Content-Type-Options", "nosniff");
        headers.put("Cache-Control", "no-store");
        headers.put("Referrer-Policy", "no-referrer");
        headers.put("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        return headers;
    }
    private static WebResourceResponse blocked() {
        return new WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", securityHeaders(), new ByteArrayInputStream( "Offline demo: this resource is blocked.".getBytes(StandardCharsets.UTF_8)));
    }
    @Override protected void onPause() {
        active=false;
        uxKnown=false;
        gearAt=0;
        updateBoundary();
        if (webView != null) webView.onPause();
        super.onPause();
    }
    @Override protected void onResume() {
        active=true;
        uxKnown=false;
        gearAt=0;
        refreshUx();
        updateBoundary();
        super.onResume();
        if (webView != null) webView.onResume();
    }
    @Override protected void onDestroy() {
        dead=true;
        handler.removeCallbacksAndMessages(null);
        if (uxManager != null) {
            try { uxManager.unregisterListener(); } catch (RuntimeException ignored) { }
        }
        if (car != null) car.disconnect();
        if(vehicle!=null)vehicle.release();
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
