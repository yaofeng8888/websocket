package com.yf.websocket;

import net.sf.json.JSONObject;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

/**
 * servlet是单例对象，但是websocket是多例对象。因为他需要实施连接。需要知道每个链接是谁
 */
@ServerEndpoint("/websocket/{name}")
public class Websocket {
    private String name;//用户记录当前websocket是谁
    private Session session;//链接用于记录当前链接

    private static Map<String, Websocket> websocketMap = new HashMap<>();

    /**
     * 建立链接的时候调用此方法
     *
     * @param name    地址参数中的name 用于区分链接是谁
     * @param session 当前建立的链接
     */
    @OnOpen
    public void onOpen(@PathParam("name") String name, Session session) {
        this.name = name;
        this.session = session;
        websocketMap.put(name, this);
        System.out.println(name+"用户建立链接了");
    }

    /**
     * 用于接受客户端发来的消息，这个地方应该根据自己实际业务需求来写，
     * 比如聊天 应该是收到消息后查看发送给谁，然后转发给他，
     * 如何判断另外一个接收者是谁
     *
     * @param session
     * @param message
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        //解析发送过来的内容，找到目标接收者
        JSONObject jsonObject = JSONObject.fromObject(message);//将字符串转为json对象
        String to = jsonObject.getString("toUser");//找到接收者
        String toMessage = jsonObject.getString("toMessage");//获取到发送的内容
        //根据目标接收者，找到他的session链接
        Websocket websocket = websocketMap.get(to);//获取目标的接收者websocket
        //通过session发消息
        if (websocket != null) {
            Session tosession = websocket.getSession();//获取到服务器和目标接收者的链接，可能会抛出空指针异常
            if (tosession.isOpen()){//如果链接是打开状态
                tosession.getAsyncRemote().sendText(toMessage);//找到链接的另外一段，然后发送消息
            }
        }else {
            //对方不在线 缓存发送的消息，返回对方不在线给发送者
            session.getAsyncRemote().sendText("对方不在线");
        }
    }

    /**
     * 出现异常的时候触发
     *
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {

    }

    /**
     * 链接关闭的时候触发
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public static Map<String, Websocket> getWebsocketMap() {
        return websocketMap;
    }

    public static void setWebsocketMap(Map<String, Websocket> websocketMap) {
        Websocket.websocketMap = websocketMap;
    }
}
