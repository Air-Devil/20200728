package bluetooth.maiziclass_3.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import bluetooth.maiziclass_3.MainActivity;

/**
 * 监听连接申请的线程
 */
public class AcceptThread extends Thread {
    private static final String NAME = "BlueToothClass";
    private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTTION_UUID);

    private final BluetoothServerSocket mmServerSocket;     // 服务器socket
    private final BluetoothAdapter mBluetoothAdapter;       // 蓝牙适配器
    private final Handler mHandler;
    //    private ConnectedThread mConnectedThread;
    private ArrayList<ConnectedThread> mConnectedThreadList = new ArrayList<>();     // 线程队列

    public AcceptThread(BluetoothAdapter adapter, Handler handler) {
        // 使用一个临时对象，该对象稍后被分配给mmServerSocket，因为mmServerSocket是最终的
        mBluetoothAdapter = adapter;
        mHandler = handler;
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID是应用程序的UUID，客户端代码使用相同的UUID
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket;

        //持续监听，直到出现异常或返回socket
        while (true) {
            try {
                mHandler.sendEmptyMessage(Constant.MSG_START_LISTENING);
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
                break;
            }
            // 如果一个连接被接受
            if (socket != null) {
                // 在单独的线程中完成管理连接的工作
                manageConnectedSocket(socket);
                Log.e("test", "0000000000");
//                try {
//                    mmServerSocket.close();
//                    mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                break;
            }
        }
    }

//    private void manageConnectedSocket(BluetoothSocket socket) {
//        //只支持同时处理一个连接
//        if( mConnectedThread != null) {
//            mConnectedThread.cancel();
//        }
//        mHandler.sendEmptyMessage(Constant.MSG_GOT_A_CLINET);
//        mConnectedThread = new ConnectedThread(socket, mHandler);
//        mConnectedThread.start();
//    }


    private void manageConnectedSocket(BluetoothSocket socket) {
        Log.e("inmanage1", "111111111");
        ConnectedThread connectedThread = new ConnectedThread(socket, mHandler);
        mHandler.sendEmptyMessage(Constant.MSG_GOT_A_CLINET);
        connectedThread.start();
        mConnectedThreadList.add(connectedThread);
        Log.e("inmanage2", "2222222");
    }

    /**
     * 取消监听socket，使此线程关闭
     */
    public void cancel() {
        try {
            mmServerSocket.close();
            mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
        } catch (IOException e) {
        }
    }

    public void sendData(byte[] data) {
        for (ConnectedThread connectedThread : mConnectedThreadList){
            connectedThread.write(data);
        }
//            if (mConnectedThread != null) {
//                mConnectedThread.write(data);
//            }
    }
}