package com.driver.utils;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.driver.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Apipas on 6/4/15.
 */
public class SimUtils {

    public static boolean sendSMS(Context ctx, int simID, String toNum, String centerNum, String smsText, PendingIntent sentIntent, PendingIntent deliveryIntent) {
        String name;

        try {
            if (simID == 0) {
                name = "isms";
                // for model : "Philips T939" name = "isms0"
            } else if (simID == 1) {
                name = "isms2";
            } else {
                throw new Exception("can not get service which for sim '" + simID + "', only 0,1 accepted as values");
            }
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            Object param = method.invoke(null, name);

            method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            if (Build.VERSION.SDK_INT < 18) {
                method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                method.invoke(stubObj, toNum, centerNum, smsText, sentIntent, deliveryIntent);
            } else {
                method = stubObj.getClass().getMethod("sendText", String.class, String.class, String.class, String.class, PendingIntent.class, PendingIntent.class);
                method.invoke(stubObj, ctx.getPackageName(), toNum, centerNum, smsText, sentIntent, deliveryIntent);
            }

            return true;
        } catch (ClassNotFoundException e) {
            Log.e("apipas1", "ClassNotFoundException:" + e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e("apipas2", "NoSuchMethodException:" + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e("apipas3", "InvocationTargetException:" + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e("apipas4", "IllegalAccessException:" + e.getMessage());
        } catch (Exception e) {
            Log.e("apipas5", "Exception:" + e.getMessage());
        }
        return false;
    }


    public static boolean sendMultipartTextSMS(Context ctx, int simID, String toNum, String centerNum, ArrayList<String> smsTextlist, ArrayList<PendingIntent> sentIntentList, ArrayList<PendingIntent> deliveryIntentList) {
        String name;
        try {
            if (simID == 0) {
                name = "isms";
                // for model : "Philips T939" name = "isms0"
            } else if (simID == 1) {
                name = "isms2";
            } else {
                throw new Exception("can not get service which for sim '" + simID + "', only 0,1 accepted as values");
            }
            Method method = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            method.setAccessible(true);
            Object param = method.invoke(null, name);

            method = Class.forName("com.android.internal.telephony.ISms$Stub").getDeclaredMethod("asInterface", IBinder.class);
            method.setAccessible(true);
            Object stubObj = method.invoke(null, param);
            if (Build.VERSION.SDK_INT < 18) {
                method = stubObj.getClass().getMethod("sendMultipartText", String.class, String.class, List.class, List.class, List.class);
                method.invoke(stubObj, toNum, centerNum, smsTextlist, sentIntentList, deliveryIntentList);
            } else {
                method = stubObj.getClass().getMethod("sendMultipartText", String.class, String.class, String.class, List.class, List.class, List.class);
                method.invoke(stubObj, ctx.getPackageName(), toNum, centerNum, smsTextlist, sentIntentList, deliveryIntentList);
            }
            return true;
        } catch (ClassNotFoundException e) {
            Log.e("apipas", "ClassNotFoundException:" + e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e("apipas", "NoSuchMethodException:" + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e("apipas", "InvocationTargetException:" + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e("apipas", "IllegalAccessException:" + e.getMessage());
        } catch (Exception e) {
            Log.e("apipas", "Exception:" + e.getMessage());
        }
        return false;
    }

    public static void sendDirectSMS(Context ctx) {
        String SENT = "SMS_SENT", DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0, new Intent(
                SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0,
                new Intent(DELIVERED), 0);

        // SEND BroadcastReceiver
        BroadcastReceiver sendSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
//                        showSnackBar(getString(R.string.sms_sent));
//                        Analytics.track(AnalyticsEvents.SEND_REMINDER_SMS_APP_SUCCESS);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//                        showSnackBar(getString(R.string.sms_send_failed_try_again));
//                        Analytics.track(AnalyticsEvents.SEND_REMINDER_SMS_APP_FAILED);
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
//                        showSnackBar(getString(R.string.no_service_sms_failed));
//                        Analytics.track(AnalyticsEvents.SEND_REMINDER_SMS_APP_FAILED);
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
//                        showSnackBar(getString(R.string.no_service_sms_failed));
//                        Analytics.track(AnalyticsEvents.SEND_REMINDER_SMS_APP_FAILED);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
//                        showSnackBar(getString(R.string.no_service_sms_failed));
//                        Analytics.track(AnalyticsEvents.SEND_REMINDER_SMS_APP_FAILED);
                        break;
                }
            }
        };

        // DELIVERY BroadcastReceiver
        BroadcastReceiver deliverSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
//                        Toast.makeText(getBaseContext(), R.string.sms_delivered,
//                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
//                        Toast.makeText(ctx, R.string.sms_not_delivered,
//                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        ctx.registerReceiver(sendSMS, new IntentFilter(SENT));
        ctx.registerReceiver(deliverSMS, new IntentFilter(DELIVERED));
        String smsText = "hiiii";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager localSubscriptionManager = SubscriptionManager.from(ctx);
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                List localList = localSubscriptionManager.getActiveSubscriptionInfoList();

                SubscriptionInfo simInfo1 = localSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(0);
                SubscriptionInfo simInfo2 = localSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(1);

                //SendSMS From SIM One
                SmsManager.getSmsManagerForSubscriptionId(simInfo1.getSubscriptionId()).sendTextMessage("8447992236", null, smsText, sentPI, deliveredPI);

                //SendSMS From SIM Two
                SmsManager.getSmsManagerForSubscriptionId(simInfo2.getSubscriptionId()).sendTextMessage("8447992236", null, smsText, sentPI, deliveredPI);
            }
        } else {
            SmsManager.getDefault().sendTextMessage("8447992236", null, smsText, sentPI, deliveredPI);
            Toast.makeText(ctx, "Success", Toast.LENGTH_SHORT).show();
        }
        ctx.unregisterReceiver(sendSMS);
        ctx.unregisterReceiver(deliverSMS);
    }


    public static void sendDualSimSMSOption(final String ToaddrmobileNo, final String message, Context ctx) {

// detect os is below 22 or not
        if (Build.VERSION.SDK_INT >= 22) {

            final PendingIntent PendingIntent1 = PendingIntent.getBroadcast(ctx, 0, new Intent("SENT"), 0);
            final PendingIntent PendingIntent2 = PendingIntent.getBroadcast(ctx, 0, new Intent("DELIVERED"), 0);

            SubscriptionManager localSubscripManager = SubscriptionManager.from(ctx);

            ArrayList<Integer> saveSubscripID = new ArrayList<Integer>();
            ArrayList<String> saveSimOperatorNames = new ArrayList<String>();
            ArrayList<String> saveSimSerialNumbers = new ArrayList<String>();
            final int[] subscripInformationArray = new int[2];

            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            if (localSubscripManager.getActiveSubscriptionInfoCount() > 1) {
                List localList = localSubscripManager.getActiveSubscriptionInfoList();
                final String[] arrayOfStringData = new String[localList.size()];
                int i = 0;
                Iterator localIterator = localList.iterator();

                while (localIterator.hasNext()) {
                    SubscriptionInfo localSubscriptionInfo = (SubscriptionInfo) localIterator.next();
                    localSubscriptionInfo.getSubscriptionId();
                    Log.v("SIM", "22 api level..got dual sim: ");
                    int j = i + 1;
                    String SimCarrierName = (localSubscriptionInfo.getCarrierName().toString().trim());
                    String SimDisplayName = (localSubscriptionInfo.getDisplayName().toString().trim());
                    String SimSerialNo = (localSubscriptionInfo.getIccId().toString().trim());
                    if (SimCarrierName != null && SimCarrierName.length() > 0) {
                        arrayOfStringData[i] = SimCarrierName;
                        Log.v("SIM", "SIM.getCarrierName:"+arrayOfStringData[i]);
                    } else {
                        arrayOfStringData[i] = SimDisplayName;
                        Log.v("SIM", "SIM.SimDisplayName:"+arrayOfStringData[i]);
                    }
                    i = j;
                }

                SubscriptionManager subscriptionManager = SubscriptionManager.from(ctx);
                List<SubscriptionInfo> subscripInformationList = subscriptionManager.getActiveSubscriptionInfoList();
                for (SubscriptionInfo subscriptionInfo : subscripInformationList) {
                    int subscriptionId = subscriptionInfo.getSubscriptionId();
                    Log.v("SIM", "SIM.subscriptionId:" + subscriptionId);
                }

                for (i = 0; i < subscripInformationList.size(); i++) {
                    Log.v("SIM", "DUal SIm:subscripInformationList:" + subscripInformationList.get(i));
                    Log.v("SIM", "DUal SIm:getSubscriptionId:" + subscripInformationList.get(i).getSubscriptionId());
                    subscripInformationArray[i] = subscripInformationList.get(i).getSubscriptionId();
                    saveSubscripID.add(subscripInformationList.get(i).getSubscriptionId());
                    saveSimSerialNumbers.add(subscripInformationList.get(i).getIccId());
                }

                for (i = 0; i < arrayOfStringData.length; i++) {
                    Log.v("SIM", "DUal SIm:arrayOfStringData:" + arrayOfStringData[i]);
                    Log.v("SIM", "DUal SIm:subscripInformationArray:" + subscripInformationArray[i]);
                    if (arrayOfStringData[i] != null && arrayOfStringData[i].length() > 0) {
                        saveSimOperatorNames.add(arrayOfStringData[i]);
                    } else {
                        saveSimOperatorNames.add("Sim " + (i + 1));
                    }
                }
// this is the method to show sim selection dialog with above sim serials info.
//                SimSelection_ShowDialogBox(context, ToAddrMobNo, message, saveSubscripId, saveSimOperaNames, saveSimSerialNumbers);
            }
        } else {
// else part. default sim taking to send sms
        }
    }

}