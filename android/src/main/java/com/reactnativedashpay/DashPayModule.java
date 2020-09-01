package com.reactnativedashpay;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.List;

public class DashPayModule extends ReactContextBaseJavaModule {
  private static ReactApplicationContext reactContext;
  private static final String PAYMENT_URI = "com.ar.dashpaypos";
  private static final int REQUEST_CODE = 1;
  public static int tsn=1;
  public static int lastSentTsn=0;
  private TelephonyManager tm;

  @ReactMethod
  public void multiply(int a, int b , Promise promise){
      promise.resolve(a*b);
  }

  public String getResponseCode() {
    return responseCode;
  }

  public void setResponseCode(String responseCode) {
    this.responseCode = responseCode;
  }

  private String responseCode;

  public String getDisplayTest() {
    return displayTest;
  }

  public void setDisplayTest(String displayTest) {
    this.displayTest = displayTest;
  }

  private String displayTest;

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  private String result;
  DashPayModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
    tm = (TelephonyManager) reactContext.getSystemService(Context.TELEPHONY_SERVICE);
  }

  @SuppressLint("MissingPermission")
  @ReactMethod
  public void getImei(Promise promise) {
    if (!hasPermission()) {
      promise.reject(new RuntimeException("Missing permission " + Manifest.permission.READ_PHONE_STATE));
    } else {
      if (Build.VERSION.SDK_INT >= 23) {
        int count = tm.getPhoneCount();
        String[] imei = new String[count];
        for (int i = 0; i < count; i++) {
          if (Build.VERSION.SDK_INT >= 26) {
            imei[i] = tm.getImei(i);
          } else {
            imei[i] = tm.getDeviceId(i);
          }
        }
        promise.resolve(Arguments.fromJavaArgs(imei));
      } else {
        promise.resolve(Arguments.fromJavaArgs(new String[]{tm.getDeviceId()}));
      }
    }
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return reactContext.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    } else return true;
  }

  @ReactMethod
  public void getTransactionResults (Promise promise){
    MobileResults results = new MobileResults(getDisplayTest(),getResponseCode(),getResult());
    String res = String.format("{'result':'%s','displayTest':'%s','responseCode':'%s'}",results.result,results.displayTest,results.responseCode);
    promise.resolve(res);
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
              setDisplayTest(displayTest);
              setResult(result);
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
    List<ResolveInfo> resInfo = reactContext.getPackageManager().queryIntentActivities(share, 0);
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

        break;
      }
    }
    if (!found){
      promise.resolve("no dashpay pos");
      return;
    }
    reactContext.getCurrentActivity().startActivityForResult(Intent.createChooser(share,"Select"),REQUEST_CODE);
  }

  public class MobileResults {
    public String displayTest;
    public String responseCode;
    public String result;
    public MobileResults(String displayTest, String responseCode, String result) {
      this.displayTest = displayTest;
      this.responseCode = responseCode;
      this.result = result;
    }

  }
}
