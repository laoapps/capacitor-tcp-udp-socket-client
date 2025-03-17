import Capacitor
import Network

@objc(TcpUdpPlugin)
public class TcpUdpPlugin: CAPPlugin {
    private var tcpConnection: NWConnection?
    private var udpConnection: NWConnection?
    private let queue = DispatchQueue(label: "com.laoapps.tcpudp")

    // TCP Methods
    @objc func connectTCP(_ call: CAPPluginCall) {
        guard let host = call.getString("host"),
              let port = call.getInt("port") else {
            call.reject("Invalid host or port")
            return
        }
        let cert = call.getString("cert")
        let key = call.getString("key")
        let tcpHost = NWEndpoint.Host(host)
        let tcpPort = NWEndpoint.Port(rawValue: UInt16(port))!

        let parameters: NWParameters
        if cert != nil && key != nil {
            // Use TLS with custom configuration
            let tlsOptions = NWProtocolTLS.Options()
            // Optionally configure TLS with cert and key (see note below)
            parameters = NWParameters(tls: tlsOptions)
        } else {
            // Plain TCP
            parameters = NWParameters.tcp
        }

        tcpConnection = NWConnection(host: tcpHost, port: tcpPort, using: parameters)
        tcpConnection?.stateUpdateHandler = { state in
            switch state {
            case .ready:
                self.startTcpListener()
                self.notifyListeners("tcpConnected", data: [:]) // Success notification
                call.resolve()
            case .failed(let error):
                self.notifyListeners("tcpError", data: ["message": error.localizedDescription])
                call.reject("TCP Connection Failed: \(error)")
            default:
                break
            }
        }
        tcpConnection?.start(queue: queue)
    }

    @objc func sendTCP(_ call: CAPPluginCall) {
        guard let connection = tcpConnection else {
            call.reject("Not connected")
            return
        }
        let data = call.getAny("data")
        let sendData: Data
        if let stringData = data as? String {
            sendData = stringData.data(using: .utf8)!
        } else if let binaryData = data as? [UInt8] {
            sendData = Data(binaryData)
        } else {
            call.reject("Invalid data type")
            return
        }
        connection.send(content: sendData, completion: .contentProcessed { error in
            if let error = error {
                self.notifyListeners("tcpError", data: ["message": error.localizedDescription])
                call.reject("Send Failed: \(error)")
            } else {
                self.notifyListeners("tcpDataSent", data: [:]) // Success notification
                call.resolve()
            }
        })
    }

    @objc func disconnectTCP(_ call: CAPPluginCall) {
        tcpConnection?.cancel()
        tcpConnection = nil
        notifyListeners("tcpDisconnected", data: [:]) // Success notification
        call.resolve()
    }

    private func startTcpListener() {
        tcpConnection?.receive(minimumIncompleteLength: 1, maximumLength: 1024) { data, _, isComplete, error in
            if let data = data, !data.isEmpty {
                self.notifyListeners("tcpDataReceived", data: ["data": [UInt8](data)])
            }
            if let error = error {
                self.notifyListeners("tcpError", data: ["message": error.localizedDescription])
            }
            if !isComplete {
                self.startTcpListener()
            }
        }
    }

    // UDP Methods
    @objc func connectUDP(_ call: CAPPluginCall) {
        guard let host = call.getString("host"),
              let port = call.getInt("port") else {
            call.reject("Invalid host or port")
            return
        }
        let udpHost = NWEndpoint.Host(host)
        let udpPort = NWEndpoint.Port(rawValue: UInt16(port))!
        udpConnection = NWConnection(host: udpHost, port: udpPort, using: .udp)
        udpConnection?.start(queue: queue)
        startUdpListener()
        notifyListeners("udpConnected", data: [:]) // Success notification
        call.resolve()
    }

    @objc func sendUDP(_ call: CAPPluginCall) {
        guard let connection = udpConnection else {
            call.reject("Not connected")
            return
        }
        let data = call.getAny("data")
        let sendData: Data
        if let stringData = data as? String {
            sendData = stringData.data(using: .utf8)!
        } else if let binaryData = data as? [UInt8] {
            sendData = Data(binaryData)
        } else {
            call.reject("Invalid data type")
            return
        }
        connection.send(content: sendData, completion: .contentProcessed { error in
            if let error = error {
                self.notifyListeners("udpError", data: ["message": error.localizedDescription])
                call.reject("Send Failed: \(error)")
            } else {
                self.notifyListeners("udpDataSent", data: [:]) // Success notification
                call.resolve()
            }
        })
    }

    @objc func disconnectUDP(_ call: CAPPluginCall) {
        udpConnection?.cancel()
        udpConnection = nil
        notifyListeners("udpDisconnected", data: [:]) // Success notification
        call.resolve()
    }

    private func startUdpListener() {
        udpConnection?.receiveMessage { data, _, isComplete, error in
            if let data = data, !data.isEmpty {
                self.notifyListeners("udpDataReceived", data: ["data": [UInt8](data)])
            }
            if let error = error {
                self.notifyListeners("udpError", data: ["message": error.localizedDescription])
            }
            if !isComplete {
                self.startUdpListener()
            }
        }
    }
}
