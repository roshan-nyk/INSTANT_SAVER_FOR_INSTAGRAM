package instant.saver.for_instagram.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import de.hdodenhof.circleimageview.CircleImageView;
import instant.saver.for_instagram.R;
import instant.saver.for_instagram.util.Utils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LogInFragment extends Fragment implements View.OnClickListener {

    private Utils utils;
    private WebView webView;
    private CircleImageView cancelButton;
    private String current_Activity;
    OkHttpClient okHttpClient;

    public LogInFragment() {
        // Required empty public constructor
    }

    public static LogInFragment newInstance(String activity) {
        LogInFragment fragment = new LogInFragment();
        Bundle args = new Bundle();
        args.putString("Current_Activity", activity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utils = new Utils(requireActivity());
        if (getArguments() != null)
            current_Activity = getArguments().getString("Current_Activity", null);
        okHttpClient = new OkHttpClient().newBuilder()
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_log_in, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GDPR_view(view);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void openInstagramOnWebView(@NotNull View view) {
        if (!utils.isNetworkAvailable())
            Toast.makeText(requireActivity(), "Enable Internet Connection And Try Again.", Toast.LENGTH_LONG).show();
        else {
            view.findViewById(R.id.GDPR_CardView).setVisibility(View.GONE);
            view.findViewById(R.id.webview_ScrollView).setVisibility(View.VISIBLE);
            cancelButton = view.findViewById(R.id.cancel_Button);
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setOnClickListener(this);
            webView = view.findViewById(R.id.instaWebview);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new MyWebViewClient());
            webView.clearCache(true);
            webView.clearHistory();
            webView.clearMatches();
            webView.loadUrl("https://www.instagram.com/accounts/login");
        }
    }

    private void GDPR_view(@NotNull View view) {
        view.findViewById(R.id.webview_ScrollView).setVisibility(View.GONE);
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.BL_TR,
                new int[]{Color.parseColor("#F58529"),
                        Color.parseColor("#FEDA77"),
                        Color.parseColor("#DD2A7B"),
                        Color.parseColor("#8134AF"),
                        Color.parseColor("#515BD4")
                });
        view.findViewById(R.id.login_notice_cardView).setBackground(gradientDrawable);

        TextView privacyTextView = view.findViewById(R.id.login_notice_textview2);
        SpannableString mySpannableString = new SpannableString("PRIVACY POLICY");
        mySpannableString.setSpan(new UnderlineSpan(), 0, mySpannableString.length(), 0);
        privacyTextView.setText(mySpannableString);
        privacyTextView.setOnClickListener(v -> {
                    if ("InstagramActivity" .equals(current_Activity))
                        getParentFragmentManager().beginTransaction().add(R.id.mainScrollView_constraint_Layout, PrivacyFragment.newInstance())
                                .addToBackStack(null)
                                .commit();
                    else
                        getParentFragmentManager().beginTransaction().add(R.id.all_users_activity_layout, PrivacyFragment.newInstance())
                                .addToBackStack(null)
                                .commit();
                }
        );
        view.findViewById(R.id.login_notice_insta_logo_layout).setOnClickListener(v -> openInstagramOnWebView(view));
    }

    @Override
    public void onClick(View v) {
        try {
            if (v == cancelButton)
                requireActivity().onBackPressed();
        } catch (Exception e) {
            Log.d("TAG", "onClick: " + e.getMessage());
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView webView, String str) {
            Log.w("TAG", "Entered onPageFinished() For URL:- " + str);
            super.onPageFinished(webView, str);
            String cookies = CookieManager.getInstance().getCookie(str);

            if (cookies != null && cookies.contains("ds_user_id") && cookies.contains("sessionid")) {
                utils.setCookies(cookies);
                int index = cookies.indexOf("ds_user_id=");
                cookies = cookies.substring(index + 11, index + 31);
                index = cookies.indexOf(';');
                cookies = cookies.substring(0, index);
                utils.setUserId(cookies);
                utils.setClipBoardClip(null);
                onClick(cancelButton);
            }
            else
                try {
                    Toast.makeText(requireActivity(), "You Need to LogIn to your Instagram Account.", Toast.LENGTH_SHORT).show();
                }
                catch (IllegalStateException e) {
                    Log.d("TAG", "onPageFinished: " + e);
                }
        }

        @Override
        public void onLoadResource(WebView webView, String str) {
            super.onLoadResource(webView, str);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
            Log.w("", "");
            Map<String,String> temp = webResourceRequest.getRequestHeaders();
            if (temp.size()>0) {
                Map<String, String> responseHeaders = new HashMap<>(temp);
                Stream<String> words  = Arrays.stream(responseHeaders.values().toArray()).flatMap((val) -> Stream.of("\n" + val ));
                String values = Arrays.toString(responseHeaders.values().toArray());
               /* Log.w("TAG", "\n\n====================================REQUEST START=============================================\n\n" +
                        "Request: " + webResourceRequest.getMethod() + "  ====>  "
                        + webResourceRequest.getUrl() + "\n"
                        + "Request Headers KEYS: " + responseHeaders.keySet() + "\n"
                        + "Request Headers VALUES: " + words.collect(Collectors.toList())
                        + "\n\n==========================================REQUEST END=========================\n\n"
                );*/
            }

//            Log.w("TAG", "Entered shouldInterceptRequest()");
            WebResourceResponse returnResponse  = super.shouldInterceptRequest(webView, webResourceRequest);
            Request okRequest = new Request.Builder()
                    .url(webResourceRequest.getUrl().toString())
//                        .post(null)
                    .build();
            try {
                Response okResponse = okHttpClient.newCall(okRequest).execute();
                int statusCode = okResponse.code();
                String encoding = "UTF-8";
                String mimeType = "application/json";
                String reasonPhrase = "OK";
                Map<String,String> responseHeaders = new HashMap<String,String>();
               /* Log.w("", "");
                Log.w("TAG", "\n\n===================================RESPONSE START==============================================\n\n" +
                        "okResponse status code:" + statusCode + "\n"
                        + "okResponse headers For Request: "
                        +  webResourceRequest.getUrl().toString() + "\n"
                        + okResponse.headers() + "\n\n==============================================RESPONSE END=============================\n\n"
                );*/
                okResponse.close();
//                    InputStream data = new ByteArrayInputStream(okResponse.body().string().getBytes(StandardCharsets.UTF_8));
//                    returnResponse = new WebResourceResponse(mimeType,encoding,statusCode,reasonPhrase,responseHeaders,data);
//                    Log.w("TAG", "okResponse InputStream:" + returnResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return returnResponse;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            return false;
//            return super.shouldOverrideUrlLoading(webView, webResourceRequest);
        }
    }
}