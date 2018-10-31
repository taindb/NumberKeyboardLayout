package com.taindb.library;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NumberKeyboardLayout extends FrameLayout implements View.OnClickListener {
    private static final long KEYBOARD_TRANSLATE_DURATION = 200;

    private boolean expandable = false;

    private ImageView deleteKey;

    private InputConnection mInputConnectionFocus;

    private EditText mEditTextFocus;

    private final List<TextView> mNumericKeys = new ArrayList<>();

    private final HashMap<EditText, InputConnection> mEditTexts = new HashMap<>();

    private final DecelerateInterpolator mInterpolator = new DecelerateInterpolator();

    private boolean hasLongPressDeleteKey = false;

    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    private int wrapContentHeight;

    private final float RATIO_WITH_HEIGHT = 1/3F;

    private View contentLayout;

    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener mDeleteKeyTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (hasLongPressDeleteKey) {
                    hasLongPressDeleteKey = false;
                }
            }
            return false;
        }
    };

    private final View.OnLongClickListener mDeleteKeyLongClickListener = new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mInputConnectionFocus == null) return false;
            hasLongPressDeleteKey = true;
            deleteSurroundingText();
            return false;
        }
    };

    private final Runnable mDeleteKeyRunnable = new Runnable() {
        @Override
        public void run() {
            deleteSurroundingText();
        }
    };

    public NumberKeyboardLayout(Context context) {
        super(context);
        inflateView();
        addViewListener();
    }

    public NumberKeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflateView();
        addViewListener();
    }

    public NumberKeyboardLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflateView();
        addViewListener();
    }

    public void expand() {
        contentLayout.setVisibility(VISIBLE);
        animate().translationY(0).setDuration(KEYBOARD_TRANSLATE_DURATION).setInterpolator(mInterpolator);
        expandable = true;
    }

    public void collapse() {
        animate().translationY(getHeight()).setDuration(KEYBOARD_TRANSLATE_DURATION).setInterpolator(mInterpolator);
        expandable = false;
    }

    public boolean expandable() {
        return expandable;
    }

    public boolean onBackPressed() {
        if (expandable) {
            collapse();
            return true;
        }

        return false;
    }

    public void registerEditText(final EditText editText) {
        if (editText == null) return;

        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setTextIsSelectable(true);
        editText.setSoundEffectsEnabled(false);
        editText.setLongClickable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setShowSoftInputOnFocus(false);
        }

        mEditTexts.put(editText, editText.onCreateInputConnection(new EditorInfo()));

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /*                if (!hasFocus) {
                    collapse();
                }
*/
                handleFocusEditText(editText, hasFocus);
            }
        });

        editText.addTextChangedListener(new AbstractTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                if (hasLongPressDeleteKey) {
                    mUIHandler.postDelayed(mDeleteKeyRunnable, 150L);
                }
            }
        });

        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFocusEditText(editText, true);
            }
        });
    }

    private void handleFocusEditText(EditText editText, boolean hasFocus) {
        hideSystemKeyboard(editText);
        if (hasFocus && !expandable) {
            mEditTextFocus = editText;
            mInputConnectionFocus = mEditTexts.get(mEditTextFocus);
//            expand();
        }
    }

    private void inflateView() {
        View keyboardView = inflate(getContext(), R.layout.number_keyboard_layout, this);
        contentLayout = keyboardView.findViewById(R.id.content_layout);
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.key0));
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.key1));
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.key2));
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.key3));
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.key4));
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.key5));
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.key6));
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.key7));
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.key8));
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.key9));
        mNumericKeys.add((TextView) keyboardView.findViewById(R.id.leftAuxBtn));

        deleteKey = keyboardView.findViewById(R.id.rightAuxBtn);
        wrapContentHeight = (int) (getResources().getDisplayMetrics().heightPixels * RATIO_WITH_HEIGHT);
//        keyboardView.setVisibility(View.INVISIBLE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addViewListener() {
        for (TextView textView : mNumericKeys) {
            textView.setOnClickListener(this);
        }
        deleteKey.setOnClickListener(this);
        deleteKey.setOnLongClickListener(mDeleteKeyLongClickListener);
        deleteKey.setOnTouchListener(mDeleteKeyTouchListener);
    }

    private void hideSystemKeyboard(@NonNull View view) {
        IBinder windowToken = view.getWindowToken();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    private void deleteSurroundingText() {
        if (mInputConnectionFocus == null) return;
        mInputConnectionFocus.deleteSurroundingText(1, 0);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUIHandler.removeCallbacksAndMessages(null);
        if (deleteKey != null) {
            deleteKey.setOnClickListener(null);
        }

        mNumericKeys.clear();
        mInputConnectionFocus = null;
        for (Map.Entry<EditText, InputConnection> entry : mEditTexts.entrySet()) {
            if (entry != null) {
                InputConnection value = entry.getValue();
                if (value != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        value.closeConnection();
                    }
                }
            }
        }
        mEditTexts.clear();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int newHeight = wrapContentHeight;
        if (heightMode == MeasureSpec.EXACTLY) {
            newHeight = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            newHeight = wrapContentHeight;
        }
        contentLayout.getLayoutParams().height = newHeight;
    }

    @Override
    public void onClick(View v) {
        if (mInputConnectionFocus == null) return;
        int i = v.getId();
        if (i == R.id.rightAuxBtn) {
            deleteSurroundingText();
        } else if (v instanceof TextView) {
                TextView key = (TextView) v;
                mInputConnectionFocus.commitText(key.getText(), 0);
            }
        }
}
