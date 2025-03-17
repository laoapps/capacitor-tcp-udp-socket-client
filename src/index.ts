import { registerPlugin } from '@capacitor/core';
import { TcpUdpPlugin } from './definitions';

const TcpUdp = registerPlugin<TcpUdpPlugin>('TcpUdp', {
  web: () => import('./web').then(m => new m.tcpudpsocketclientWeb()),
});

export * from './definitions';
export { TcpUdp };