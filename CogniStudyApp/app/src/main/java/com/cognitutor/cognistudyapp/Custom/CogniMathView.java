package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cognitutor.cognistudyapp.Activities.QuestionActivity;
import com.x5.template.Chunk;
import com.x5.template.Theme;
import com.x5.template.providers.AndroidTemplates;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.kexanie.library.R;

public class CogniMathView extends WebView {
    private static AtomicInteger numRunning = new AtomicInteger(0);

    private QuestionActivity mActivity;
    private String mText;
    private String mConfig;
    private int mEngine;

    public CogniMathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (QuestionActivity) context;
        getSettings().setJavaScriptEnabled(true);
        getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        setBackgroundColor(Color.TRANSPARENT);

        TypedArray mTypeArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MathView,
                0, 0
        );

        try { // the order of execution of setEngine() and setText() matters
            setEngine(mTypeArray.getInteger(R.styleable.MathView_engine, 0));
//            setText(mTypeArray.getString(R.styleable.MathView_text)); //This was causing problems on Kevin's phone because it was trying to set the text to null
        } finally {
            mTypeArray.recycle();
        }
    }

    public static void resetNumRunning() {
        numRunning.set(0);
    }

    private Chunk getChunk() {
        String TEMPLATE_KATEX = "katex";
        String TEMPLATE_MATHJAX = "mathjax";
        String template = TEMPLATE_KATEX;
        AndroidTemplates loader = new AndroidTemplates(getContext());
        switch (mEngine) {
            case Engine.KATEX: template = TEMPLATE_KATEX; break;
            case Engine.MATHJAX: template = TEMPLATE_MATHJAX; break;
        }

        return new Theme(loader).makeChunk(template);
    }

    public void setText(String text) {

        //text = addUndersets(text);

        mText = text;
        Chunk chunk = getChunk();

        String TAG_FORMULA = "formula";
        String TAG_CONFIG = "config";
        chunk.set(TAG_FORMULA, mText);
        chunk.set(TAG_CONFIG, mConfig);

        String chunkString = chunk.toString();

        String styledHtml = addStyle(chunkString);

//        String finalHtml = addUndersets(styledHtml);
//        String finalHtml = addUndersets(chunkString);

        //this.loadDataWithBaseURL(null, chunk.toString(), "text/html", "utf-8", "about:blank");
        this.loadDataWithBaseURL(null, styledHtml, "text/html", "utf-8", "about:blank");
        final CogniMathView mathView = this;
        numRunning.incrementAndGet();
        this.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                numRunning.incrementAndGet();
                mathView.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (numRunning.decrementAndGet() == 0) { // just "running--;" if you add a timer.
                    mActivity.loadingFinished();
                }
            }
        });
    }

    public String getText() {
        return mText;
    }

    /**
     * Tweak the configuration of MathJax.
     * The `config` string is a call statement for MathJax.Hub.Config().
     * For example, to enable auto line breaking, you can call:
     * config.("MathJax.Hub.Config({
     *      CommonHTML: { linebreaks: { automatic: true } },
     *      "HTML-CSS": { linebreaks: { automatic: true } },
     *      SVG: { linebreaks: { automatic: true } }
     *  });");
     *
     * This method should be call BEFORE setText() and AFTER setEngine().
     * PLEASE PAY ATTENTION THAT THIS METHOD IS FOR MATHJAX ONLY.
     * @param config
     */
    public void config(String config) {
        if (mEngine == Engine.MATHJAX) {
            this.mConfig = config;
        }
    }

    /**
     * Set the js engine used for rendering the formulas.
     * @param engine must be one of the constants in class Engine
     *
     * This method should be call BEFORE setText().
     */
    public void setEngine(int engine) {
        switch (engine) {
            case Engine.KATEX: {
                mEngine = Engine.KATEX;
                break;
            }
            case Engine.MATHJAX: {
                mEngine = Engine.MATHJAX;
                break;
            }
            default: mEngine = Engine.KATEX;
        }
    }

    public static class Engine {
        final public static int KATEX = 0;
        final public static int MATHJAX = 1;
    }

    private String addStyle(String input) {

        String style = "<style type=\"text/css\">" +
                "@font-face{" +
                "font-family: MyFont;" +
//                        "src: url(\"file:///android_asset/fonts/AftaSansThin-Regular.otf\")" +
                "src: url(\"file:///android_asset/fonts/lmroman10-regular.otf\")" +
                "}" +
                "body {" +
                "font-family: MyFont;" +
//                        "font-weight: bold;" +
                "}" +
                "p {" +
                    "line-height:2em;" +
                "}" +
                ".ques {" +
                    "padding-bottom:1.1em;" +
                    "white-space: nowrap;" +
                "}" +
                ".ques1 { background: url(file:///android_res/drawable/ques1_1000_40.png) 53% 100% no-repeat;  background-size: 500px 20px; }" +
                ".ques2 { background: url(file:///android_res/drawable/ques2_1000_40.png) 54% 100% no-repeat;  background-size: 500px 20px; }" +
                ".ques3 { background: url(file:///android_res/drawable/ques3_1000_40.png) 50% 100% no-repeat;  background-size: 500px 20px; }" +
                "</style>";
//        String style = "<style type=\"text/css\">";
//        String css = "";
//        try { css = IOUtils.toString(new URI("file:///android_asset/css/question.css")); }
//        catch (Exception e) {Log.e("IOUtils", e.getMessage());}
//        style += css + "</style>";

        String script = "<script type=\"text/javascript\" src=\"file:///android_asset/javascript/font-booster-fixer.js\"></script>";

        String head = "<head>";
        int idx = input.indexOf(head) + head.length();
        String beginning = input.substring(0, idx);
        String end = input.substring(idx + 1);
        return beginning + style + script + end;
    }

    private String addUndersets(String input) {

        if(!input.contains("\\underset"))
            return input;

        String startBody = "<body>";
        int startIdx = input.indexOf(startBody) + startBody.length();
        String endBody = "</body>";
        int endIdx = input.indexOf(endBody);

        String beginning = input.substring(0, startIdx);
        String body = input.substring(startIdx, endIdx);
        String end = input.substring(endIdx);

        String backslash = "\\\\";
        String openParen = "\\(";
        String closeParen = "\\)";
        String notBackslash = "[^" + backslash + "]";
//		String equation = "\\\\\\(([^\\\\]|\\\\(?!\\)))*\\\\\\)";
//		String equation = backslash + openParen + "([^\\\\]|\\\\(?!\\)))*" + backslash + closeParen;
        String equation = backslash + openParen + "("+notBackslash+"|"+backslash+"(?!"+closeParen+"))*" + backslash + closeParen;

        String word = "[^\\s\\\\]*\\w[^\\s\\\\]*";

        String notWord = "[\\s*[\\W&&[^\\\\]]*\\s*]+";

        String regex = equation + "|" + word + "|" + notWord;

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(body);

        StringBuilder sb = new StringBuilder();

        while(matcher.find()) {
            String found = matcher.group();
            if(Pattern.matches(word, found)) {
                found = "\\(\\underset{\\text{ }}{\\textrm{" + found + "}}\\)";
            }
            Log.i("line", found);
            sb.append(found);
        }

        return beginning + sb.toString() + end;
    }
}