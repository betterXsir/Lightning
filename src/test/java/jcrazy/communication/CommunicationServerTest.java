package jcrazy.communication;

import jcrazy.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CommunicationServerTest extends BaseTest{
    @Autowired
    private CommunicationServer communicationServer;

    @Test
    public void initServer() {
        try {
            communicationServer.startUpServer();
        } catch (Exception var) {
            var.printStackTrace();
        }
    }
}
