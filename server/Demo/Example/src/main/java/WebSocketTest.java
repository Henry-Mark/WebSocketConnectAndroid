import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;

/**
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
@ServerEndpoint("/websocketServer")
public class WebSocketTest {

    //connect key为session的ID，value为此对象this
    private static final HashMap<String, Object> connect = new HashMap<String, Object>();
    //userMap key为session的ID，value为用户名
    private static final HashMap<String, String> userMap = new HashMap<String, String>();
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //判断是否是第一次接收的消息
    private boolean isFirst = true;

    private String userName;

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        connect.put(session.getId(), this);//获取Session，存入Hashmap中
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {

        String usr = userMap.get(session.getId());
        userMap.remove(session.getId());
        connect.remove(session.getId());


        System.out.println(usr + "退出！当前在线人数为" + connect.size());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {

        if (isFirst) {
            this.userName = message;
            System.out.println("用户" + userName + "上线,在线人数：" + connect.size());
            userMap.put(session.getId(), userName);
            isFirst = false;
        } else {
            String[] msg = message.split("@", 2);//以@为分隔符把字符串分为xxx和xxxxx两部分,msg[0]表示发送至的用户名，all则表示发给所有人
            if (msg[0].equals("all")) {
                sendToAll(msg[1], session);
            } else {
                sendToUser(msg[0], msg[1], session);
            }
        }
    }

    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    /**
     * 给所有人发送消息
     *
     * @param msg     发送的消息
     * @param session
     */
    private void sendToAll(String msg, Session session) {
        String who = "";
        //群发消息
        for (String key : connect.keySet()) {
            WebSocketTest client = (WebSocketTest) connect.get(key);
            if (key.equalsIgnoreCase(userMap.get(key))) {
                who = "自己对大家说 : ";
            } else {
                who = userMap.get(session.getId()) + "对大家说 :";
            }
            synchronized (client) {
                try {
                    client.session.getBasicRemote().sendText(who + msg);
                } catch (IOException e) {
                    connect.remove(client);
                    e.printStackTrace();
                    try {
                        client.session.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        }
    }

    /**
     * 发送给指定用户
     *
     * @param user    用户名
     * @param msg     发送的消息
     * @param session
     */
    private void sendToUser(String user, String msg, Session session) {
        boolean you = false;//标记是否找到发送的用户
        for (String key : userMap.keySet()) {
            if (user.equalsIgnoreCase(userMap.get(key))) {
                WebSocketTest client = (WebSocketTest) connect.get(key);
                synchronized (client) {
                    try {
                        client.session.getBasicRemote().sendText(userMap.get(session.getId()) + "对你说：" + msg);
                    } catch (IOException e) {
                        connect.remove(client);
                        try {
                            client.session.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                you = true;//找到指定用户标记为true
                break;
            }

        }
        //you为true则在自己页面显示自己对xxx说xxxxx,否则显示系统：无此用户
        if (you) {
            try {
                session.getBasicRemote().sendText("自己对" + user + "说:" + msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                session.getBasicRemote().sendText("系统：无此用户");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
