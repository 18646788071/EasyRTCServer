package com.lyl.WebSocketServer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.json.JsonObject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by david on 2017/10/11.
 */

@ServerEndpoint(value = "/websocket")
@Component
public class WebSocketHandler {
    /**当前在线人数*/
    private static int onlineCount = 0;

    /**当前所有的WebSocketHandler集合*/
    private static CopyOnWriteArraySet<WebSocketHandler> webSocketHandlerSet =
            new CopyOnWriteArraySet<WebSocketHandler>();

    /**当前handler对应的session，用于与客户端通信*/
    private Session session;

    /**当前连接用户的userId*/
    private Long userId;

    /**
     * 连接成功
     * */
    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        webSocketHandlerSet.add(this);
        addOnlineCount();
        System.out.println("有新的客户端连接 session: "+session+"  当前在线人数为："+getOnlineCount()+"人");
    }

    /**
     * 连接关闭
     * */
    @OnClose
    public void onClose(){
        webSocketHandlerSet.remove(this);
        minusOnlineCount();
        System.out.println("有客户端断开连接 session: "+session+"  当前在线人数为："+getOnlineCount()+"人");
    }

    /**
     * 收到字符串消息
     * */
    @OnMessage
    public void didReceiveMessage(String message, Session session){
        System.out.println("有来自客户端的消息:\n"+message+"\nsession: "+session);
    }

    /**
     * 收到二进制数据
     * */
    @OnMessage
    public void didReceiveData(ByteBuffer byteBuffer, Session session){
        String message = getString(byteBuffer);
        System.out.println("有来自客户端的二进制数据:\n"+message+"\nsession: "+session);
        JSONObject obj = JSON.parseObject(message);
        String eventName = (String) obj.get("eventName");
        JSONObject data = (JSONObject)obj.get("data");

        if(eventName.equals("__send_userId")){
            this.userId = ((Integer)data.get("userId")).longValue();
            System.out.println("userId:"+this.userId);
        }
        else {
            Long toUserId = ((Integer)data.get("toUserId")).longValue();
            Iterator<WebSocketHandler> iterator = webSocketHandlerSet.iterator();
            while(iterator.hasNext()){
                WebSocketHandler handler = iterator.next();
                if(toUserId.equals(handler.userId)){
                    try {
                        sendDate(handler.session, byteBuffer);
                        System.out.println("转发成功！");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

    }

    /**
     * 发生错误
     * */
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("发生错误 session: "+session);
        error.printStackTrace();
    }

    /**
     * 向客户端发送字符串消息
     * */
    public void sendMessage(String message)throws IOException{
        this.session.getBasicRemote().sendText(message);
        System.out.println("向客户端发送消息："+message);
    }

    /**
     * 向客户端发送二进制数据
     * */
    public void sendDate(Session session, ByteBuffer byteBuffer)throws IOException{
        session.getBasicRemote().sendBinary(byteBuffer);
        String message = getString(byteBuffer);
        System.out.println("向客户端发送二进制数据:"+message);
    }

    public int getOnlineCount(){
        return onlineCount;
    }

    private void addOnlineCount(){
        onlineCount++;
    }

    private void minusOnlineCount(){
        onlineCount--;
    }

    private String getString(ByteBuffer buffer){
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try{
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        }catch(Exception e){
            e.printStackTrace();
            return "error";
        }
    }

}
