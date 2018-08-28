package henry.websocket.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String wsurl = "ws://172.16.50.126:8080/websocketServer";
    private static final String TAG = "MainActivity";
    private WebSocketConnection mConnect = new WebSocketConnection();
    private EditText mContent;
    private Button mSend;
    private TextView mText;
    private EditText mUserName;
    private EditText mToSb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindObject();
        connect();
    }

    /**
     * 绑定控件
     */
    private void bindObject() {
        mContent = (EditText) findViewById(R.id.et_content);
        mSend = (Button) findViewById(R.id.btn_send);
        mText = (TextView) findViewById(R.id.tv_test);
        mUserName = (EditText) findViewById(R.id.et_username);
        mToSb = (EditText) findViewById(R.id.et_to);
        mSend.setOnClickListener(this);
    }

    /**
     * websocket连接，接收服务器消息
     */
    private void connect() {
        Log.i(TAG, "ws connect....");
        try {
            mConnect.connect(wsurl, new WebSocketHandler() {
                @Override
                public void onOpen() {
                    Log.i(TAG, "Status:Connect to " + wsurl);
                    sendUsername();
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.i(TAG, payload);
                    mText.setText(payload != null ? payload : "");
//                    mConnect.sendTextMessage("I am android client");
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.i(TAG, "Connection lost..");
                }
            });
        } catch (WebSocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送用户名给服务器
     */
    private void sendUsername() {
        String user = mUserName.getText().toString();
        if (user != null && user.length() != 0)
            mConnect.sendTextMessage(user);
        else
            Toast.makeText(getApplicationContext(), "不能为空", Toast.LENGTH_SHORT).show();
    }

    /**
     * 发送消息
     *
     * @param msg
     */
    private void sendMessage(String msg) {
        if (mConnect.isConnected()) {
            mConnect.sendTextMessage(msg);
        } else {
            Log.i(TAG, "no connection!!");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mConnect.disconnect();
    }

    @Override
    public void onClick(View view) {
        if (view == mSend) {
            String content = mToSb.getText().toString() + "@" + mContent.getText().toString();
            if (content != null && content.length() != 0)
                sendMessage(content);
            else
                Toast.makeText(getApplicationContext(), "不能为空", Toast.LENGTH_SHORT).show();
        }
    }
}
