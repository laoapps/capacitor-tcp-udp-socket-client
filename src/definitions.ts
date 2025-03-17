// Define event names as an enum to ensure type safety
export enum TcpUdpEvent {
  TcpConnected = 'tcpConnected',
  TcpDataSent = 'tcpDataSent',
  TcpDisconnected = 'tcpDisconnected',
  TcpDataReceived = 'tcpDataReceived',
  TcpError = 'tcpError',
  UdpConnected = 'udpConnected',
  UdpDataSent = 'udpDataSent',
  UdpDisconnected = 'udpDisconnected',
  UdpDataReceived = 'udpDataReceived',
  UdpError = 'udpError',
}

export interface TcpUdpPlugin {
  connectTCP(options: { host: string; port: number; cert?: string; key?: string }): Promise<void>;
  sendTCP(options: { data: string | Uint8Array }): Promise<void>;
  disconnectTCP(): Promise<void>;

  connectUDP(options: { host: string; port: number }): Promise<void>;
  sendUDP(options: { data: string | Uint8Array }): Promise<void>;
  disconnectUDP(): Promise<void>;

  // Generic addListener with event-specific signatures
  // addListener(eventName: TcpUdpEvent, listenerFunc: () => void): Promise<void>;
  // addListener(eventName: TcpUdpEvent.TcpDataSent, listenerFunc: () => void): Promise<void>;
  // addListener(eventName: TcpUdpEvent.TcpDisconnected, listenerFunc: () => void): Promise<void>;
  // addListener(eventName: TcpUdpEvent.TcpDataReceived, listenerFunc: (event: { data: Uint8Array }) => void): Promise<void>;
  // addListener(eventName: TcpUdpEvent.TcpError, listenerFunc: (event: { message: string }) => void): Promise<void>;

  // addListener(eventName: TcpUdpEvent.UdpConnected, listenerFunc: () => void): Promise<void>;
  // addListener(eventName: TcpUdpEvent.UdpDataSent, listenerFunc: () => void): Promise<void>;
  // addListener(eventName: TcpUdpEvent.UdpDisconnected, listenerFunc: () => void): Promise<void>;
  // addListener(eventName: TcpUdpEvent.UdpDataReceived, listenerFunc: (event: { data: Uint8Array }) => void): Promise<void>;
  // addListener(eventName: TcpUdpEvent.UdpError, listenerFunc: (event: { message: string }) => void): Promise<void>;

  removeAllListeners(): Promise<void>;
}