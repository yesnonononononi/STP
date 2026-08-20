package com.summit.stp.message.infrastructure.websocket;


import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.summit.stp.message.infrastructure.websocket.listener.DisconnectListener;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
@Slf4j
@RequiredArgsConstructor
@Component
public class Connector  {
    public static final String UID = "UID";
    public static  final String ROOM = "USER_ROOM";
    private final SocketIOServer socketIOServer;
    private final ConnectListener connectListener;
    private final DisconnectListener disconnectListener;

    @PostConstruct
    public void init(){
        socketIOServer.addConnectListener(connectListener);
        socketIOServer.addDisconnectListener(disconnectListener);
    }

    @PreDestroy
    public void stop(){
        socketIOServer.stop();
    }



}
