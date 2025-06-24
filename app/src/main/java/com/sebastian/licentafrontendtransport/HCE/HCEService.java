package com.sebastian.licentafrontendtransport.HCE;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

import android.content.Intent;

import com.sebastian.licentafrontendtransport.HCE.TokenManager;
import com.sebastian.licentafrontendtransport.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.zip.Inflater;

public class HCEService extends HostApduService {
    private static final String TAG = "HCEService";
    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Intent apduIntent = new Intent("com.sebastian.HCE_APDU_RECEIVED");
        apduIntent.setPackage(getPackageName());
        sendBroadcast(apduIntent);
        if (!TokenManager.NFC_ENABLED) {
            return Utils.hexStringToByteArray(HostCardEmulatorConstants.STATUS_FAILED);
        }
        if (commandApdu.length == 0) {
            return Utils.hexStringToByteArray(HostCardEmulatorConstants.STATUS_FAILED);
        }
        String apduStr = Utils.byteArrayToHex(commandApdu);
        if (apduStr.equalsIgnoreCase(HostCardEmulatorConstants.SELECT_AID)) {
            Log.i(TAG, "AID selectat corect");
            return Utils.hexStringToByteArray(HostCardEmulatorConstants.STATUS_SUCCESS);
        }
        if (apduStr.equalsIgnoreCase(HostCardEmulatorConstants.READ_NDEF_MESSAGE)) {
            String payload = TokenManager.NFC_PAYLOAD;
            byte[] message = payload.getBytes(StandardCharsets.UTF_8);
            byte[] response = new byte[message.length + 2];
            System.arraycopy(message, 0, response, 0, message.length);
            response[response.length - 2] = (byte) 0x90;
            response[response.length - 1] = (byte) 0x00;
            return response;
        }
        return Utils.hexStringToByteArray(HostCardEmulatorConstants.STATUS_FAILED);
    }
    @Override
    public void onDeactivated(int reason) {
        Log.i(TAG, "Conexiune NFC întreruptă: " + reason);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "HCE Service distrus");
    }
}

final class HostCardEmulatorConstants {
    public static  String SELECT_AID = "00A4040007A0000002471001";
    public static  String READ_NDEF_MESSAGE = "00B000000F";
    public static String STATUS_SUCCESS = "9000";
    public static String STATUS_FAILED = "6F00";

}

