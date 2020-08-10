package com.reactnativedashpay;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class DashPayModule extends ReactContextBaseJavaModule {
  private static ReactApplicationContext reactContext;
  private static final String PAYMENT_URI = "com.ar.pos";
  private static final int REQUEST_CODE = 1;
  public static int tsn=1;
  public static int lastSentTsn=0;


  @ReactMethod
  public void multiply(int a, int b , Promise promise){
      promise.resolve(a*b);
  }

  @ReactMethod
  public String getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(String responseCode) {
    this.responseCode = responseCode;
  }

  private String responseCode;

  DashPayModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
  }

  @NonNull
  @Override
  public String getName() {
    return "DashPay";
  }


  @ReactMethod
  public void pay(String REFERENCE_NUMBER, String TRANSACTION_ID,String OPERATOR_ID, String ADDITIONAL_AMOUNT,String AMOUNT,String TRANSACTION_TYPE,String EXTRA_ORIGINATING_URI, Promise promise) {

    reactContext.addActivityEventListener(new ActivityEventListener() {
      @Override
      public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CODE) {
          if(resultCode == Activity.RESULT_OK){
            String tid = intent.getStringExtra("TRANSACTION_ID");
            if (tid != null && Integer.parseInt(tid) == lastSentTsn) {
              String result = intent.getStringExtra("RESULT");
              String displayTest = intent.getStringExtra("DISPLAY_TEXT");
              if (result.equals("APPROVED")) {
                String responseCode = intent.getStringExtra("RESPONSE_CODE");
                String authCode = intent.getStringExtra("AUTH_CODE");
                setResponseCode(responseCode);
                promise.resolve(requestCode);
              } else if (result.equals("DECLINED")) {
                String responseCode = intent.getStringExtra("RESPONSE_CODE");
                setResponseCode(responseCode);
                promise.resolve(requestCode);
              } else {
              }

              new CountDownTimer(2000, 1000) { // 5000 = 5 sec

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                }
              }.start();
            }
          } else if (resultCode == Activity.RESULT_CANCELED) {
            //Write your code if there's no result
          }
        }
      }

      @Override
      public void onNewIntent(Intent intent) {

      }
    });

    Intent share = new Intent(android.content.Intent.ACTION_SEND);
    boolean found = false;
    share.setType("text/plain");
    Iterable<? extends ResolveInfo> resInfo = null;
    for (ResolveInfo info : resInfo) {
      if (info.activityInfo.packageName.toLowerCase().contains(PAYMENT_URI) ||
        info.activityInfo.name.toLowerCase().contains(PAYMENT_URI) ) {
        share.putExtra(Intent.EXTRA_ORIGINATING_URI, EXTRA_ORIGINATING_URI);
        share.putExtra("TRANSACTION_TYPE", TRANSACTION_TYPE);
        //share.putExtra("TRANSACTION_TYPE","REVERSE LAST");
        share.putExtra("AMOUNT", AMOUNT); // 15.00
        share.putExtra("ADDITIONAL_AMOUNT", ADDITIONAL_AMOUNT);
        share.putExtra("OPERATOR_ID", OPERATOR_ID);
        share.putExtra("REFERENCE_NUMBER", REFERENCE_NUMBER);
        share.putExtra("TRANSACTION_ID", TRANSACTION_ID);
        lastSentTsn = tsn;
        tsn++;
        share.setPackage(info.activityInfo.packageName);
        found = true;
        reactContext.startActivity(share);
        break;
      }
    }
    if (!found)
      return;
  }

}
