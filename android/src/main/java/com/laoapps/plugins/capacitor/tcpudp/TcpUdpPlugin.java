package com.laoapps.tcpudp;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

@CapacitorPlugin(name = "TcpUdp")
public class TcpUdpPlugin extends Plugin {
    private SSLSocket tcpSocket;
    private OutputStream tcpOutputStream;
    private InputStream tcpInputStream;
    private DatagramSocket udpSocket;
    private Thread tcpListenerThread;
    private Thread udpListenerThread;
    private String udpHost;
    private int udpPort;

    // TCP Methods
    @PluginMethod
    public void connectTCP(PluginCall call) {
        String host = call.getString("host");
        int port = call.getInt("port");
        String cert = call.getString("cert");
        String key = call.getString("key");

        try {
            if (cert != null && key != null) {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, null, null); // Simplified; add cert/key logic here
                SSLSocketFactory factory = sslContext.getSocketFactory();
                tcpSocket = (SSLSocket) factory.createSocket(host, port);
            } else {
                tcpSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
            }
            tcpOutputStream = tcpSocket.getOutputStream();
            tcpInputStream = tcpSocket.getInputStream();
            startTcpListener();
            notifyListeners("tcpConnected", new JSObject()); // Success notification
            call.resolve();
        } catch (Exception e) {
            JSObject error = new JSObject();
            error.put("message", e.getMessage());
            notifyListeners("tcpError", error);
            call.reject("TCP Connection Failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void sendTCP(PluginCall call) {
        Object data = call.getData().opt("data");
        try {
            byte[] buffer;
            if (data instanceof String) {
                buffer = ((String) data).getBytes(StandardCharsets.UTF_8);
            } else if (data instanceof byte[]) {
                buffer = (byte[]) data;
            } else {
                call.reject("Invalid data type");
                return;
            }
            tcpOutputStream.write(buffer);
            tcpOutputStream.flush();
            notifyListeners("tcpDataSent", new JSObject()); // Success notification
            call.resolve();
        } catch (Exception e) {
            JSObject error = new JSObject();
            error.put("message", e.getMessage());
            notifyListeners("tcpError", error);
            call.reject("Send Failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void disconnectTCP(PluginCall call) {
        try {
            if (tcpSocket != null) tcpSocket.close();
            if (tcpListenerThread != null) tcpListenerThread.interrupt();
            tcpSocket = null;
            notifyListeners("tcpDisconnected", new JSObject()); // Success notification
            call.resolve();
        } catch (Exception e) {
            JSObject error = new JSObject();
            error.put("message", e.getMessage());
            notifyListeners("tcpError", error);
            call.reject("Disconnect Failed: " + e.getMessage());
        }
    }

    private void startTcpListener() {
        tcpListenerThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    int bytesRead = tcpInputStream.read(buffer);
                    if (bytesRead > 0) {
                        byte[] data = new byte[bytesRead];
                        System.arraycopy(buffer, 0, data, 0, bytesRead);
                        JSObject result = new JSObject();
                        result.put("data", data);
                        notifyListeners("tcpDataReceived", result);
                    }
                }
            } catch (Exception e) {
                JSObject error = new JSObject();
                error.put("message", e.getMessage());
                notifyListeners("tcpError", error);
            }
        });
        tcpListenerThread.start();
    }

    // UDP Methods
    @PluginMethod
    public void connectUDP(PluginCall call) {
        udpHost = call.getString("host");
        udpPort = call.getInt("port");
        try {
            udpSocket = new DatagramSocket();
            startUdpListener();
            notifyListeners("udpConnected", new JSObject()); // Success notification
            call.resolve();
        } catch (Exception e) {
            JSObject error = new JSObject();
            error.put("message", e.getMessage());
            notifyListeners("udpError", error);
            call.reject("UDP Connection Failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void sendUDP(PluginCall call) {
        Object data = call.getData().opt("data");
        String host = call.getString("host", udpHost);
        int port = call.getInt("port", udpPort);
        try {
            InetAddress address = InetAddress.getByName(host);
            byte[] buffer;
            if (data instanceof String) {
                buffer = ((String) data).getBytes(StandardCharsets.UTF_8);
            } else if (data instanceof byte[]) {
                buffer = (byte[]) data;
            } else {
                call.reject("Invalid data type");
                return;
            }
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            udpSocket.send(packet);
            notifyListeners("udpDataSent", new JSObject()); // Success notification
            call.resolve();
        } catch (Exception e) {
            JSObject error = new JSObject();
            error.put("message", e.getMessage());
            notifyListeners("udpError", error);
            call.reject("Send Failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void disconnectUDP(PluginCall call) {
        try {
            if (udpSocket != null) udpSocket.close();
            if (udpListenerThread != null) udpListenerThread.interrupt();
            udpSocket = null;
            notifyListeners("udpDisconnected", new JSObject()); // Success notification
            call.resolve();
        } catch (Exception e) {
            JSObject error = new JSObject();
            error.put("message", e.getMessage());
            notifyListeners("udpError", error);
            call.reject("Disconnect Failed: " + e.getMessage());
        }
    }

    private void startUdpListener() {
        udpListenerThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    udpSocket.receive(packet);
                    byte[] data = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());
                    JSObject result = new JSObject();
                    result.put("data", data);
                    notifyListeners("udpDataReceived", result);
                }
            } catch (Exception e) {
                JSObject error = new JSObject();
                error.put("message", e.getMessage());
                notifyListeners("udpError", error);
            }
        });
        udpListenerThread.start();
    }
}