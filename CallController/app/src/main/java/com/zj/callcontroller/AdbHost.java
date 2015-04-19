package com.zj.callcontroller;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by jack on 4/18/2015.
 */
public class AdbHost {
    private static String TAG = AdbHost.class.getName();

    private Context context;
    public static final String ADB_PUB_FILE = "adbkey.pub";
    public static final String ADB_PRIV_FILE = "adbkey";
    private Socket socket;
    private AdbConnection adbConnection=null;
    private AdbStream adbStream = null;

    public AdbHost(Context context){
        this.context = context;
    };

    private AdbCrypto getAdbCrypto() throws NoSuchAlgorithmException, IOException {
        File pub = new File("/sdcard/",ADB_PUB_FILE);
        File priv = new File("/sdcard/",ADB_PRIV_FILE);

        AdbCrypto c = null;

        AdbBase64 adbBase64 = new AdbBase64() {
            @Override
            public String encodeToString(byte[] bytes) {
                return Base64.encodeToString(bytes, Base64.NO_PADDING);
            }
        };

        // Try to load a key pair from the files
        if (pub.exists() && priv.exists())
        {
            try {
                c = AdbCrypto.loadAdbKeyPair(adbBase64, priv, pub);
            } catch (IOException e) {
                // Failed to read from file
                c = null;
            } catch (InvalidKeySpecException e) {
                // Key spec was invalid
                c = null;
            } catch (NoSuchAlgorithmException e) {
                // RSA algorithm was unsupported with the crypo packages available
                c = null;
            }
        }

        if (c == null)
        {
            // We couldn't load a key, so let's generate a new one
            c = AdbCrypto.generateAdbKeyPair(adbBase64);

            // Save it
            c.saveAdbKeyPair(priv, pub);

            c.saveAdbKeyPairBase64(new File("/sdcard/",ADB_PRIV_FILE+".b64"),new File("/sdcard/",ADB_PUB_FILE+".b64"));
            Log.d(TAG, "Generated new keypair");
        }
        else
        {
            Log.d(TAG, "Loaded existing keypair");
        }

        return c;

    }


    public AdbStream getAdbStream() throws IOException, InterruptedException, NoSuchAlgorithmException {
        if (null==adbStream){
            socket = new Socket("127.0.0.1", 5555);
            adbConnection = AdbConnection.create(socket, getAdbCrypto());
            adbConnection.connect();
            adbStream = adbConnection.open("shell:");

        }
        return adbStream;
    }

    public void close(){
        try{
            adbStream.close();
            adbConnection.close();
            socket.close();
        }catch(Exception e){}
    }
}
