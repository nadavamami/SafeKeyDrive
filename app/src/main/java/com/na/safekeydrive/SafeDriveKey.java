/*
 * Copyright (C) 2008-2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.na.safekeydrive;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.na.safekeydrive.floatbutton.BouncingImageView;
import com.na.safekeydrive.floatbutton.FloatButtonService;

import java.util.ArrayList;
import java.util.List;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class SafeDriveKey extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static final boolean DEBUG = true;

    private static final long UPDATE_INTERVAL_MS = 500;
    private static final long FASTEST_INTERVAL_MS = 250;
    private static PendingIntent pIntent;
    private static PendingIntent lIntent;
    private static GoogleApiClient mGoogleApiClient;

    
    /**
     * This boolean indicates the optional example code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on 
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;
    private static final String CURRENT_LANGUAGE = "currentLanguage";

    private InputMethodManager mInputMethodManager;
    private boolean isDriveMode = false;
    private boolean isOverride = false;
    private LatinKeyboardView mDriveView;
    private LatinKeyboardView mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;

    private StringBuilder mComposing = new StringBuilder();
    private boolean mPredictionOn;
    private boolean mCompletionOn;
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    private long mMetaState;
    
    private LatinKeyboard mSymbolsKeyboard;
    private LatinKeyboard mSymbolsShiftedKeyboard;
    private LatinKeyboard mQwertyKeyboard;
    private LatinKeyboard mEmptyKeyBoard;
    private LatinKeyboard mHebrewKeyBoard;
    public static final String TAG = SafeDriveKey.class.getSimpleName();
    private LatinKeyboard mCurKeyboard;
    private LatinKeyboard[] keyBoards;
    private int currentLanguage;
    private Vibrator mVibrator;

//    private String[] subTypes = getResources().getStringArray(R.array.supported_languages);

    
    private String mWordSeparators;
    
    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
        super.onCreate();
        Log.e(TAG,"onCreate");
        startActivityRecognition();
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);
        IntentFilter intentFilter = new IntentFilter(ActivityRecognitionService.ACTION);
        registerReceiver(mReceiver,intentFilter);
        IntentFilter overrideFilter = new IntentFilter(BouncingImageView.OVERRIDE);
        registerReceiver(mOverrideReceiver,overrideFilter);
        keyBoards = new LatinKeyboard[2];
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        currentLanguage = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(CURRENT_LANGUAGE,0);
    }
    
    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() {
        Log.e(TAG,"onInitializeInterface");
        if (mQwertyKeyboard != null) {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mQwertyKeyboard = new LatinKeyboard(this, R.xml.qwerty);
        mSymbolsKeyboard = new LatinKeyboard(this, R.xml.symbols);
        mSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbols_shift);
        mEmptyKeyBoard = new LatinKeyboard(this, R.xml.empty_keyboard);
        mHebrewKeyBoard = new LatinKeyboard(this, R.xml.heb_keybaord);
        keyBoards[0] = mQwertyKeyboard;
        keyBoards[1] = mHebrewKeyBoard;

    }
    
    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override public View onCreateInputView() {
        Log.e(TAG,"onCreateInputView");
//        mInputView = (LatinKeyboardView) getLayoutInflater().inflate(
//                R.layout.input, null);
//        mInputView.setOnKeyboardActionListener(this);
//        mInputView.setKeyboard(mQwertyKeyboard);
//        return mInputView;

        mInputView = (LatinKeyboardView) getLayoutInflater().inflate(
                R.layout.input, null);
        mInputView.setOnKeyboardActionListener(this);
        if (isDriveMode){
//            startService(new Intent(getApplicationContext(), FloatButtonService.class));
            mInputView.setKeyboard(mEmptyKeyBoard);
        }
        else
        {
            mInputView.setKeyboard(keyBoards[currentLanguage]);
        }

        return mInputView;
    }

    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        return null; //mCandidateView;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
//        Log.e(TAG,"onStartInput");
//
        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        if (!isDriveMode){
            updateCandidates();
        }

        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }
        
        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;
        mCurKeyboard = mEmptyKeyBoard;
        if (!isDriveMode) {

            // We are now going to initialize our state based on the type of
            // text being edited.
            switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
                case InputType.TYPE_CLASS_NUMBER:
                case InputType.TYPE_CLASS_DATETIME:
                    // Numbers and dates default to the symbols keyboard, with
                    // no extra features.
                    mCurKeyboard = mSymbolsKeyboard;
                    break;

                case InputType.TYPE_CLASS_PHONE:
                    // Phones will also default to the symbols keyboard, though
                    // often you will want to have a dedicated phone keyboard.
                    mCurKeyboard = mSymbolsKeyboard;
                    break;

                case InputType.TYPE_CLASS_TEXT:
                    // This is general text editing.  We will default to the
                    // normal alphabetic keyboard, and assume that we should
                    // be doing predictive text (showing candidates as the
                    // user types).
                    mCurKeyboard = keyBoards[currentLanguage];
                    mPredictionOn = true;

                    // We now look for a few special variations of text that will
                    // modify our behavior.
                    int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                    if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                            variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                        // Do not display predictions / what the user is typing
                        // when they are entering a password.
                        mPredictionOn = false;
                    }

                    if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                            || variation == InputType.TYPE_TEXT_VARIATION_URI
                            || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                        // Our predictions are not useful for e-mail addresses
                        // or URIs.
                        mPredictionOn = false;
                    }

                    if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                        // If this is an auto-complete text view, then our predictions
                        // will not be shown and instead we will allow the editor
                        // to supply their own.  We only show the editor's
                        // candidates when in fullscreen mode, otherwise relying
                        // own it displaying its own UI.
                        mPredictionOn = false;
                        mCompletionOn = isFullscreenMode();
                    }

                    // We also want to look at the current state of the editor
                    // to decide whether our alphabetic keyboard should start out
                    // shifted.
                    updateShiftKeyState(attribute);
                    break;

                default:
                    // For all unknown input types, default to the alphabetic
                    // keyboard with no special features.
                    mCurKeyboard = keyBoards[currentLanguage];
                    updateShiftKeyState(attribute);
            }

            // Update the label on the enter key, depending on what the application
            // says it will do.
        }
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        Log.e(TAG,"onFinishInputView");
        synchronized (this){
            if (isGoogleClientConnected()){
                stopActivityRecognition();
            }
        }

    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        super.onFinishInput();
        
        // Clear current composing text and candidates.
        mComposing.setLength(0);
        if (!isDriveMode){
            updateCandidates();
        }

        
        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);
        
        mCurKeyboard = keyBoards[currentLanguage];
        if (mInputView != null) {
            mInputView.closing();
        }
        if (mInputView != null) {
            mInputView.closing();
        }
    }
    
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        Log.e(TAG,"onStartInputView");
        synchronized (this){
            if (!isGoogleClientConnected()){
                startActivityRecognition();
            }
        }

        // Apply the selected keyboard to the input view.
        mInputView.setKeyboard(mCurKeyboard);
        mInputView.closing();
        final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
//        mInputView.setSubtypeOnSpaceKey(subtype);
//        mInputView.setKeyboard(mEmptyKeyBoard);
//        mInputView.closing();
//        final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
//        mInputView.setSubtypeOnSpaceKey(subtype);
    }

    @Override
    public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) {
        if (mInputView != null && subtype != null){
            mInputView.setSubtypeOnSpaceKey(subtype);
        }
    }

    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        
        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
        if (mCompletionOn) {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }
            
            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < completions.length; i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }
    
    /**
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        mMetaState = MetaKeyKeyListener.handleKeyDown(mMetaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(mMetaState));
        mMetaState = MetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }
        
        boolean dead = false;

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }
        
        if (mComposing.length() > 0) {
            char accent = mComposing.charAt(mComposing.length() -1 );
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                mComposing.setLength(mComposing.length()-1);
            }
        }
        
        onKey(c, null);
        
        return true;
    }
    
    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;
                
            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;
                
            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;
                
            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
                        // A silly example: in our input method, Alt+Space
                        // is a shortcut for 'android' in lower case.
                        InputConnection ic = getCurrentInputConnection();
                        if (ic != null) {
                            // First, tell the editor that it is no longer in the
                            // shift state, since we are consuming this.
                            ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // And we consume this event.
                            return true;
                        }
                    }
                    if (mPredictionOn && translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }
        
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        if (PROCESS_HARD_KEYS) {
            if (mPredictionOn) {
                mMetaState = MetaKeyKeyListener.handleKeyUp(mMetaState,
                        keyCode, event);
            }
        }
        
        return super.onKeyUp(keyCode, event);
    }

    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null 
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }
    
    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) {
        if (Character.isLetter(code)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
    
    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    // Implementation of KeyboardViewListener

    public void onKey(int primaryCode, int[] keyCodes) {
        if (mVibrator.hasVibrator()){
            mVibrator.vibrate(20);
        }
        if (isWordSeparator(primaryCode)) {
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose();
            return;
        } else if (primaryCode == LatinKeyboardView.KEYCODE_OPTIONS) {
            switchToNextKeyBoard();
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE
                && mInputView != null) {
            Keyboard current = mInputView.getKeyboard();
            if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) {
                current = keyBoards[currentLanguage];//mQwertyKeyboard;
            } else {
                current = mSymbolsKeyboard;
            }
            mInputView.setKeyboard(current);
            if (current == mSymbolsKeyboard) {
                current.setShifted(false);
            }
        } else {
            handleCharacter(primaryCode, keyCodes);
        }
    }

    private void switchToNextKeyBoard() {
        if (currentLanguage == (keyBoards.length - 1)){
            currentLanguage = 0;
        }
        else
        {
            currentLanguage++;
        }

        mCurKeyboard = keyBoards[currentLanguage];
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putInt(CURRENT_LANGUAGE,currentLanguage).commit();
        mInputView.setKeyboard(mCurKeyboard);
    }

    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {
        if (!mCompletionOn) {
            if (mComposing.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(mComposing.toString());
                setSuggestions(list, true, true);
            } else {
                setSuggestions(null, false, false);
            }
        }
    }
    
    public void setSuggestions(List<String> suggestions, boolean completions,
            boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(false);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(false);
        }
        if (mCandidateView != null) {
//            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }
    
    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }
        
        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (mQwertyKeyboard == currentKeyboard || mHebrewKeyBoard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        } else if (currentKeyboard == mSymbolsKeyboard) {
            mSymbolsKeyboard.setShifted(true);
            mInputView.setKeyboard(mSymbolsShiftedKeyboard);
            mSymbolsShiftedKeyboard.setShifted(true);
        } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            mInputView.setKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
        }
    }
    
    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode) && mPredictionOn) {
            mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates();
        } else {
//            getCurrentInputConnection().commitText(
//                    String.valueOf((char) primaryCode), 1);
            mComposing.append(String.valueOf((char) primaryCode));
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
        }
    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }
    
    private String getWordSeparators() {
        return mWordSeparators;
    }
    
    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }

    public void pickDefaultCandidate() {
        pickSuggestionManually(0);
    }
    
    public void pickSuggestionManually(int index) {
        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {
            // If we were generating candidate suggestions for the current
            // text, we would commit one of them here.  But for this sample,
            // we will just commit the current text.
            commitTyped(getCurrentInputConnection());
        }
    }
    
    public void swipeRight() {
        if (mCompletionOn) {
            pickDefaultCandidate();
        }
    }
    
    public void swipeLeft() {
        handleBackspace();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
    }
    
    public void onPress(int primaryCode) {
    }
    
    public void onRelease(int primaryCode) {
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver(){



        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            int activityType = bundle.getInt(ActivityRecognitionService.ACTIVITY_TYPE, DetectedActivity.STILL);
            String name = bundle.getString(ActivityRecognitionService.ACTIVITY_TYPE_NAME,"");
            int confidence = bundle.getInt(ActivityRecognitionService.ACTIVITY_CONFIDENCE);
            if (BuildConfig.DEBUG){
                Log.i(TAG,"Detected activity: " + name);
//                Toast.makeText(getApplicationContext(), "activity - " + name, Toast.LENGTH_SHORT).show();
            }
//            isDriveMode = false;
//            if (confidence > 50){
                switch (activityType){
                    case DetectedActivity.STILL:
                    case DetectedActivity.UNKNOWN:
                        if (isDriveMode){
                            stopService(new Intent(getApplicationContext(),FloatButtonService.class));
                            if (mInputView != null){
                                mInputView.setKeyboard(keyBoards[currentLanguage]);
                            }
                            isDriveMode = false;
                            isOverride = false;
                        }
                        break;
                    case DetectedActivity.WALKING:
                    case DetectedActivity.RUNNING:
                    case DetectedActivity.ON_FOOT:
                    case DetectedActivity.IN_VEHICLE:
                    case DetectedActivity.ON_BICYCLE:
//                    case DetectedActivity.TILTING:
                        isDriveMode = true;
                        if (!isOverride){
                            mInputView.setKeyboard(mEmptyKeyBoard);
                            startService(new Intent(getApplicationContext(), FloatButtonService.class));
                        }
                        else if (mInputView.getKeyboard() == mEmptyKeyBoard)
                        {
                            mInputView.setKeyboard(keyBoards[currentLanguage]);
                        }
                        break;
                }
//                if (activityType != DetectedActivity.STILL && activityType != DetectedActivity.UNKNOWN && activityType != DetectedActivity.TILTING){
//                    isDriveMode = true;
//                    mInputView.setKeyboard(mEmptyKeyBoard);
//                    startService(new Intent(getApplicationContext(), FloatButtonService.class));
//
//                }
//                else if (isDriveMode)
//                {
//                    stopService(new Intent(getApplicationContext(),FloatButtonService.class));
//                    if (mInputView != null){
//                        mInputView.setKeyboard(keyBoards[currentLanguage]);
//                    }
//                    isDriveMode = false;
//                }
            }
//        }
    };

    private BroadcastReceiver mOverrideReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras().getBoolean(BouncingImageView.EXTRA,true)){
                mInputView.setKeyboard(keyBoards[currentLanguage]);
                Log.i("override","on");
                isOverride = true;
            }
            else if (isDriveMode && !intent.getExtras().getBoolean(BouncingImageView.EXTRA,false)){
                mInputView.setKeyboard(mEmptyKeyBoard);
                Log.i("override","of");
                isOverride = false;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null){
            unregisterReceiver(mReceiver);
        }
        if (mOverrideReceiver != null){
            unregisterReceiver(mOverrideReceiver);
        }
    }


    private synchronized void startActivityRecognition(){
        int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if(resp == ConnectionResult.SUCCESS){
            buildGoogleApiClient(getApplicationContext());
        }
        else
        {
            Intent googleDialog = new Intent(getApplicationContext(),GooglePlayDialogShellActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("errorCode",resp);
            googleDialog.putExtras(bundle);
            googleDialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(googleDialog);
        }
    }

    private synchronized void stopActivityRecognition(){
        if (mGoogleApiClient != null){
            if (mGoogleApiClient.isConnected()){
                if (BuildConfig.DEBUG){
                    Log.i(TAG,"Stop activity recognition updates");
                }
                ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient,pIntent);
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,pIntent);
                mGoogleApiClient.disconnect();
                stopService(new Intent(getApplicationContext(),FloatButtonService.class));
            }
            else
            {
                Log.e(TAG,"google client not connected");
            }
        }
        isOverride = false;
    }

    public synchronized  boolean isGoogleClientConnected(){
        if (mGoogleApiClient != null){
            return mGoogleApiClient.isConnected()?true:mGoogleApiClient.isConnecting();
        }
        return false;
    }
    private synchronized  void buildGoogleApiClient(Context context) {

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()){
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(ActivityRecognition.API)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }

    }

    @Override
    public void onConnected(Bundle bundle) {
        if (BuildConfig.DEBUG){
            Toast.makeText(getApplicationContext(), "google client connected", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(getApplicationContext(), ActivityRecognitionService.class);
        pIntent = PendingIntent.getService(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient,500,pIntent);

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS);
        Intent locationIntent = new Intent(getApplicationContext(),ActivityRecognitionService.class);
        lIntent = PendingIntent.getService(getApplicationContext(),1,locationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,locationRequest, lIntent);

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (BuildConfig.DEBUG){
            Toast.makeText(getApplicationContext(),"google connection suspended",Toast.LENGTH_SHORT).show();
            Log.e(InputMethodChangeReceiver.class.getSimpleName(), "google client connection suspended reconnecting");
        }
        mGoogleApiClient.reconnect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (BuildConfig.DEBUG){
            Toast.makeText(getApplicationContext(),"google client connection failed",Toast.LENGTH_SHORT).show();
            Log.e(InputMethodChangeReceiver.class.getSimpleName(),"google client connection failed with code " + connectionResult.getErrorCode());
        }
        mGoogleApiClient.connect();
    }
    private void startTimer(){
        new CountDownTimer(10000,2000){

            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG,"tick");
            }

            @Override
            public void onFinish() {
                if (!isInputViewShown()){
                    if (BuildConfig.DEBUG){
                        Log.i(TAG,"stop activity recognition");
                    }
                    stopActivityRecognition();
                }
            }
        }.start();
    }
}
