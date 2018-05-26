package im.conversations.compliance.web;

import im.conversations.compliance.pojo.ServerResponse;
import im.conversations.compliance.xmpp.OneOffTestRunner;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;

import static im.conversations.compliance.utils.JsonReader.gson;

@WebSocket
public class TestLiveWebsocket {

    @OnWebSocketConnect
    public void connected(Session session) {
        String domain = session.getUpgradeRequest().getParameterMap().get("domain").get(0);
        boolean status = OneOffTestRunner.addResultListener(domain, (success, msg) -> {
            try {
                ServerResponse liveResultResponse = new ServerResponse(
                        success,
                        msg,
                        "/result/" + domain);
                message(session, gson.toJson(liveResultResponse));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if (!status) {
            ServerResponse liveResultResponse = new ServerResponse(
                    false,
                    "No live tests running for " + domain + ". Try refreshing this page",
                    "/result/" + domain);
            try {
                message(session, gson.toJson(liveResultResponse));
            } catch (IOException e) {
                e.printStackTrace();
            }
            session.close(400, "Invalid request");
        }
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        System.out.println("Sent: " + message);
        session.getRemote().sendString(message);
    }
}