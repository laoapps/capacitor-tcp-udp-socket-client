import { WebPlugin } from '@capacitor/core';
import {TcpUdpPlugin} from './definitions';
export class tcpudpsocketclientWeb
  extends WebPlugin
  implements TcpUdpPlugin
{
  connectTCP(_options: { host: string; port: number; cert?: string; key?: string }): Promise<void>{
    throw 'not implement for web';
  };
  sendTCP(_options: { data: string | Uint8Array }): Promise<void>{
    throw 'not implement for web';
  };
  disconnectTCP(): Promise<void>{
    throw 'not implement for web';
  };
  // Removed onTCPData as we'll use events instead

  connectUDP(_options: { host: string; port: number }): Promise<void>{
    throw 'not implement for web';
  };
  sendUDP(_options: { data: string | Uint8Array }): Promise<void>{
    throw 'not implement for web';
  };
  disconnectUDP(): Promise<void>{
    throw 'not implement for web';
  };
  // Removed onUDPData as we'll use events instead


  removeAllListeners(): Promise<void>{
    throw 'not implement for web';
  };
}
