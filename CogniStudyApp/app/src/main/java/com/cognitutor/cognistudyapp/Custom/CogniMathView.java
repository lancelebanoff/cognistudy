package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import io.github.kexanie.library.R;

import com.x5.template.Chunk;
import com.x5.template.Theme;
import com.x5.template.providers.AndroidTemplates;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CogniMathView extends WebView {
    private String mText;
    private String mConfig;
    private int mEngine;

    public CogniMathView(Context context, AttributeSet attrs) {
        super(context, attrs);
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
            setText(mTypeArray.getString(R.styleable.MathView_text));
        } finally {
            mTypeArray.recycle();
        }
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
                ".passage p {" +
                    "line-height:3em;" +
                "}" +
                ".passage span {" +
                    "padding-bottom:.5em;" +
                    "margin-top:3em;" +
                "}" +
                ".ansA { background: url(http://media.actstudent.org/designimages/one.gif) 50% 100% no-repeat; }" +
                ".ansB { background: url(http://media.actstudent.org/designimages/two.gif) 50% 100% no-repeat; }" +
                ".ansB { background: url(http://media.actstudent.org/designimages/three.gif) 50% 100% no-repeat; }" +
                ".ansD { background: url(http://media.actstudent.org/designimages/four.gif) 50% 100% no-repeat; }" +
                ".ansE { background: url(http://media.actstudent.org/designimages/five.gif) 50% 100% no-repeat; }" +
                "</style>";
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