/*
 * Created by ekirei
 * UDOO Team
 */

package org.udoo.udoodroidcondemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import me.palazzetti.adktoolkit.AdkManager;

import org.udoo.udoodroidcondemo.sounds.Effect;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final String TAG = "UDOOMario";
	private final String PREFS_NAME = "MarioPrefs";
	
	// ADK
	private AdkManager mAdkManager;
	
	// command to arduino	
	private final String FORWARD_SENDSTRING = "0";
	private final String BACK_SENDSTRING 	= "1";
	private final String RIGHT_SENDSTRING 	= "2";
	private final String LEFT_SENDSTRING 	= "3";
	private final String GOODBOY_SENDSTRING = "4";
	private final String BADBOY_SENDSTRING 	= "5";
	private final String CUTEBOY_SENDSTRING = "6";
	private final String HELLO_SENDSTRING 	= "7";
	private final String MOONWALK_SENDSTRING= "8";
	
	// speech variables
	SpeechRecognizer mSpeechRecognizer;
	
	// speech key
	private ArrayList<String> goodboy_strings = new ArrayList<String>(Arrays.asList("good boy",
			"well done", "very good", "good work", "good guy", "great", "bravo", "grande", "idolo", "fantastico"));
	private ArrayList<String> badboy_strings = new ArrayList<String>(Arrays.asList("vaffanculo", "bad boy",
			"fuck you", "bad guy", "cattivo", "brutto", "merda", "culo"));
	private ArrayList<String> cuteboy_strings = new ArrayList<String>(Arrays.asList("cute boy", "cute",
			"so cute", "nice", "carino"));
	private ArrayList<String> forward_strings = new ArrayList<String>(Arrays.asList("go forward",
			"go straight on", "forward", "avanti", "diritto", "dritto"));
	private ArrayList<String> backward_strings = new ArrayList<String>(Arrays.asList("go backward",
			"go back", "back", "indietro", "dietro"));
	private ArrayList<String> right_strings = new ArrayList<String>(Arrays.asList("turn right",
			"look right", "right", "destra"));
	private ArrayList<String> left_strings = new ArrayList<String>(Arrays.asList("turn left",
			"look left", "left", "sinistra"));
	private ArrayList<String> pizza_strings = new ArrayList<String>(Arrays.asList("pizza", "like pizza",
			"do you like pizza"));
	private ArrayList<String> hi_strings = new ArrayList<String>(Arrays.asList("ciao" , "saluti"));
	private ArrayList<String> name_strings = new ArrayList<String>(Arrays.asList("name", "your name",
			"who are you", "what's your name", "chi", "chi sei", "nome", "chiami", "chi", "ci sei", "sei", "ti sei"));
	private ArrayList<String> hello_strings = new ArrayList<String>(Arrays.asList( "hello", "ciao", "saluti", "saluta"));
	private ArrayList<String> comefrom_strings = new ArrayList<String>(Arrays.asList("come from", " are you from"));
	private ArrayList<String> goodbye_strings = new ArrayList<String>(Arrays.asList("goodbye", "arrivederci"));
	private ArrayList<String> moonwalk_strings = new ArrayList<String>(Arrays.asList("moonwalk", "moon walk",
			"Michael Jackson", "star", "stop", "dollar", "stock"));
	private ArrayList<String> campus_strings = new ArrayList<String>(Arrays.asList("Campus", "party", "parti", "tenda", "campo", "campu"));
	private ArrayList<String> bari_strings = new ArrayList<String>(Arrays.asList("turisti", "anziani", "bari", "bragiuole", "braciuole"));

	private ArrayList<String> allwords = new ArrayList<String>();
	
	// gui
	TextView debug_tv;
	ImageButton voiceButton;
	ImageView faceImage;
	Animation animationFadeIn;	
	Animation animationFadeOut;
	ImageButton twitterButton;

	private boolean running = false; 
	private String mLastFetchedId = "1";

	TextToSpeech tts;
	
	Effect cuteEffect; 
	Effect goodEffect;
	Effect badEffect;
	Effect moonwalk;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mAdkManager = new AdkManager((UsbManager) getSystemService(Context.USB_SERVICE));
		registerReceiver(mAdkManager.getUsbReceiver(), mAdkManager.getDetachedFilter());
		
		debug_tv = (TextView) findViewById(R.id.textView);
//	    debug_tv.setVisibility(View.GONE);
		
		faceImage = (ImageView) findViewById(R.id.face_imageView);
		
	    voiceButton = (ImageButton) findViewById(R.id.voice_imageButton);
	    voiceButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}
		});

	    ImageButton helloButton = (ImageButton) findViewById(R.id.imageButtonHello);
	    helloButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendHello();
			}
		});
	    
	    ImageButton moonWalkButton = (ImageButton) findViewById(R.id.ImageButtonMoonWalk);
	    moonWalkButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				moonWalkCase();
			}
		});

	    allwords.addAll(hello_strings);
        allwords.addAll(hi_strings);
	    allwords.addAll(name_strings);
	    allwords.addAll(goodboy_strings);
        allwords.addAll(badboy_strings);
        allwords.addAll(cuteboy_strings);
        allwords.addAll(forward_strings);
        allwords.addAll(backward_strings);
        allwords.addAll(right_strings);
        allwords.addAll(left_strings);
		allwords.addAll(moonwalk_strings);
		allwords.addAll(pizza_strings);
		allwords.addAll(comefrom_strings);
		allwords.addAll(goodbye_strings);
		allwords.addAll(campus_strings);
		allwords.addAll(bari_strings);

        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        
        cuteEffect = new Effect(this, R.raw.thanku); 
    	goodEffect = new Effect(this, R.raw.whohoo);
    	badEffect = new Effect(this, R.raw.no);
    	moonwalk = new Effect(this, R.raw.moonwalk);
        
        faceImage.setImageResource(R.drawable.normal);
        
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        MyRecognitionListener listener = new MyRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(listener);
               
        // Get last stored values
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mLastFetchedId = settings.getString("lastId", "1");
        
        tts = new TextToSpeech(getApplicationContext(), 
        	      new TextToSpeech.OnInitListener() {
	            @Override
	            public void onInit(int status) {
	            	if (status == TextToSpeech.SUCCESS) {

						int result = tts.setLanguage(Locale.ITALIAN);
	                    //int result = tts.setLanguage(Locale.US);
	                    tts.setPitch(0.7F);
	         
	                    if (result == TextToSpeech.LANG_MISSING_DATA) {
	                        Log.e("TTS", "Lang missing data");
	                            // missing data, install it
	                            Intent installIntent = new Intent();
	                            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	                            startActivity(installIntent);          
	                    } else {
	                    	tts.speak("Ciao Campus Party", TextToSpeech.QUEUE_FLUSH, null);
	                    }
	         
	                } else {
	                    Log.e("TTS", "Initilization Failed!");          
	                }
	            }
            });
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    if(mSpeechRecognizer!=null){
	    	mSpeechRecognizer.stopListening();
	    	mSpeechRecognizer.cancel();
	    	mSpeechRecognizer.destroy();              

	    }
	    if(tts !=null){
	         tts.stop();
	         tts.shutdown();
	    }
	    mSpeechRecognizer = null;
	    tts = null;
	    mAdkManager.close();    
	}
	
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastId", mLastFetchedId);
        editor.commit();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cuteEffect.release(); 
    	goodEffect.release();
    	badEffect.release();
        unregisterReceiver(mAdkManager.getUsbReceiver());
    }

	@Override
	protected void onResume() {
	    super.onResume();
	    if(mSpeechRecognizer == null){
        	mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
            MyRecognitionListener listener = new MyRecognitionListener();
            mSpeechRecognizer.setRecognitionListener(listener);
        }
	    mAdkManager.open();
	}

    private boolean searchCommands (String result) {   	
        boolean found = false;
        String stringFounded = "";        
        for (String string : allwords ) {
        	if (result.toLowerCase(Locale.ITALIAN).contains(string.toLowerCase(Locale.ITALIAN))) {
        		stringFounded = string;
        		found = true;
        		
//        		if (hi_strings.contains(stringFounded)) tts.speak("Hi", TextToSpeech.QUEUE_FLUSH, null);
				if (hi_strings.contains(stringFounded)) tts.speak("Ciao Campus Parti", TextToSpeech.QUEUE_FLUSH, null);
        		else if (hello_strings.contains(stringFounded)) sendHello();
        		else if (name_strings.contains(stringFounded)) speakCase("Sono Mario, un robot fatto con la iudù  per il Campus Parti");
        		else if (comefrom_strings.contains(stringFounded)) tts.speak("Io vengo dal laboratorio di iudù  ", TextToSpeech.QUEUE_FLUSH, null);
        		else if (goodboy_strings.contains(stringFounded)) sendGoodCase();
    	        else if (badboy_strings.contains(stringFounded)) sendBadCase();
    	        else if (cuteboy_strings.contains(stringFounded)) sendCuteCase();
    	        else if (forward_strings.contains(stringFounded)) mAdkManager.writeSerial(FORWARD_SENDSTRING);
    	        else if (backward_strings.contains(stringFounded)) mAdkManager.writeSerial(BACK_SENDSTRING);
    	        else if (right_strings.contains(stringFounded)) mAdkManager.writeSerial(RIGHT_SENDSTRING);
    	        else if (left_strings.contains(stringFounded)) mAdkManager.writeSerial(LEFT_SENDSTRING);
    	        else if (moonwalk_strings.contains(stringFounded)) moonWalkCase();
    	        else if (pizza_strings.contains(stringFounded))
					tts.speak("Si mi piace la pizza, ma non posso mangiarla", TextToSpeech.QUEUE_FLUSH, null);
    	        else if (goodbye_strings.contains(stringFounded)) sendGoodby();
				else if (goodbye_strings.contains(stringFounded)) tts.speak("Campus Parti", TextToSpeech.QUEUE_FLUSH, null);
				else if (bari_strings.contains(stringFounded)) tts.speak("Le braciuole, con le orecchiette, con il ragù. Li friccio tutti. Noi sim anziaaaaaaaaaa", TextToSpeech.QUEUE_FLUSH, null);

				break;
			}
		}
		return found;
    }
    
    private void moonWalkCase() {
    	Log.i(TAG, "moonwalk case");
    	moonwalk.play();
    	setNewFace(R.drawable.happy);   	
		mAdkManager.writeSerial(MOONWALK_SENDSTRING);
		returnToNormalState(6000);
    }
    
    private void sendHello() {
    	Log.i(TAG, "hello case");  
    	setNewFace(R.drawable.happy);
    	tts.speak("Ciao Campus parti", TextToSpeech.QUEUE_FLUSH, null);
		mAdkManager.writeSerial(HELLO_SENDSTRING);
		returnToNormalState(5000);
    }

    private void sendGoodby() {
    	Log.i(TAG, "hello case");  
    	setNewFace(R.drawable.happy);
    	//tts.speak("Goodbye and thank you", TextToSpeech.QUEUE_FLUSH, null);
		tts.speak("Ciao e grazie", TextToSpeech.QUEUE_FLUSH, null);
		mAdkManager.writeSerial(HELLO_SENDSTRING);
		returnToNormalState(5000);
    }
    
    private void sendGoodCase() {
		Log.i(TAG, "good case");
		setNewFace(R.drawable.happy);
		goodEffect.play();
		mAdkManager.writeSerial(GOODBOY_SENDSTRING);
		returnToNormalState(5000);
	}

    private void sendBadCase() {
    	Log.i(TAG, "bad case");  
    	setNewFace(R.drawable.sad);
		badEffect.play();
		mAdkManager.writeSerial(BADBOY_SENDSTRING);	
		returnToNormalState(5000);
    }
    
    private void sendCuteCase() {
    	Log.i(TAG, "cute case");  		
		setNewFace(R.drawable.cute);
		cuteEffect.play();
		mAdkManager.writeSerial(CUTEBOY_SENDSTRING);		
		returnToNormalState(5000);
    }

	private void speakCase(String message) {
		Log.i(TAG, "speak case");
		setNewFace(R.drawable.happy);
		tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		returnToNormalState(5000);
	}
    
    private void setNewFace(int resourceID){
    	faceImage.startAnimation(animationFadeOut);
		faceImage.setVisibility(View.INVISIBLE);
		faceImage.setImageResource(resourceID);
		faceImage.startAnimation(animationFadeIn);
		faceImage.setVisibility(View.VISIBLE);
    }
    
    private void returnToNormalState(int millis) {
    	new CountDownTimer(millis, 1000) {

            public void onTick(long millisUntilFinished) {
                //do nothing, just let it tick
            }

            public void onFinish() {
            	Log.i(TAG, "returnToNormalState");
            	setNewFace(R.drawable.normal);
            }
         }.start();
    }
      
    @SuppressWarnings("unused")
	private void showToastMessage(String message){
    	  Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
	/**
     * Fire an intent to start the voice recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
    	
        mSpeechRecognizer.startListening(intent);        
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                //do nothing, just let it tick
            }

            public void onFinish() {
            	Log.i(TAG, "stop countdown");
            	mSpeechRecognizer.stopListening();
            	voiceButton.setBackgroundResource(R.drawable.nose_up);
            }
         }.start();
    }
	
	class MyRecognitionListener implements RecognitionListener {

        @Override
        public void onBeginningOfSpeech() {
                Log.d("Speech", "onBeginningOfSpeech");
        }

		@Override
        public void onBufferReceived(byte[] buffer) {
                Log.d("Speech", "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
                Log.d("Speech", "onEndOfSpeech");
        }

        @Override
        public void onError(int error) {
                Log.d("Speech", "onError: " + error);
//                if (error == 7 ){
//                	tts.speak("Sorry, but I didn't understand", TextToSpeech.QUEUE_FLUSH, null);
//                }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
                Log.d("Speech", "onEvent");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
                Log.d("Speech", "onPartialResults");
        }

        @Override
        public void onReadyForSpeech(Bundle params) {
                Log.d("Speech", "onReadyForSpeech");
                voiceButton.setBackgroundResource(R.drawable.nose_down);
        }
        

        @Override
        public void onResults(Bundle results) {
                Log.d("Speech", "onResults");
                ArrayList<String> resultsArray = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		        debug_tv.setText("Received text: " + resultsArray.get(0));
		        
		        if (!searchCommands(resultsArray.get(0))) {
//					tts.speak("Sorry, but I didn't understand", TextToSpeech.QUEUE_FLUSH, null);
					tts.speak("Scusa, ma non ho capito.", TextToSpeech.QUEUE_FLUSH, null);
		        	//showToastMessage("Sentence is not recognized");
		        }
		        
                for (int i = 0; i < resultsArray.size();i++ ) {
                        Log.d("Speech", "result=" + resultsArray.get(i));           
                }
        }

        @Override
        public void onRmsChanged(float rmsdB) {
//               Log.d("Speech", "onRmsChanged");
        }

	}

}
