# capacitor-tcp-udp-socket-client

tcp/udp socket client for capacitor

## Install

```bash
npm install capacitor-tcp-udp-socket-client
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`createTCPClient(...)`](#createtcpclient)
* [`closeTCPClient()`](#closetcpclient)
* [`listenTCP(...)`](#listentcp)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### createTCPClient(...)

```typescript
createTCPClient(options: { serverUrl: string; port: string; }) => Promise<void>
```

| Param         | Type                                              |
| ------------- | ------------------------------------------------- |
| **`options`** | <code>{ serverUrl: string; port: string; }</code> |

--------------------


### closeTCPClient()

```typescript
closeTCPClient() => Promise<void>
```

--------------------


### listenTCP(...)

```typescript
listenTCP(options: { name: enumListenName; callBack: (params: any) => void; }) => Promise<void>
```

| Param         | Type                                                                                                     |
| ------------- | -------------------------------------------------------------------------------------------------------- |
| **`options`** | <code>{ name: <a href="#enumlistenname">enumListenName</a>; callBack: (params: any) =&gt; void; }</code> |

--------------------


### Enums


#### enumListenName

| Members                    | Value                               |
| -------------------------- | ----------------------------------- |
| **`onCommingTCPMessage`**  | <code>'onCommingTCPMessage'</code>  |
| **`onCloseTCPClient`**     | <code>'onCloseTCPClient'</code>     |
| **`onErrorTCPClient`**     | <code>'onErrorTCPClient'</code>     |
| **`onSendingTCPClient`**   | <code>'onSendingTCPClient'</code>   |
| **`onSentTCPClient`**      | <code>'onSentTCPClient'</code>      |
| **`onRemoveTCPCListener`** | <code>'onRemoveTCPCListener'</code> |

</docgen-api>
import * as tls from 'tls';
import * as dgram from 'dgram';
import * as fs from 'fs';
import * as path from 'path';
import { TcpClient, UdpMessage } from './types';

const TCP_PORT = 3000;
const UDP_PORT = 3001;

// TCP Server (with TLS)
const tcpOptions: tls.TlsOptions = {
  key: fs.readFileSync(path.join(__dirname, '../server.key')),
  cert: fs.readFileSync(path.join(__dirname, '../server.crt')),
};

const tcpServer = tls.createServer(tcpOptions, (socket: tls.TLSSocket) => {
  const client: TcpClient = {
    socket,
    address: socket.remoteAddress || 'unknown',
    port: socket.remotePort || 0,
  };
  console.log(`TCP Client connected: ${client.address}:${client.port}`);

  socket.on('data', (data: Buffer) => {
    console.log(`TCP Received from ${client.address}:${client.port}: ${data.toString()}`);
    socket.write(data); // Echo back
  });

  socket.on('end', () => {
    console.log(`TCP Client disconnected: ${client.address}:${client.port}`);
  });

  socket.on('error', (err) => {
    console.error(`TCP Error from ${client.address}:${client.port}: ${err.message}`);
  });
});

tcpServer.listen(TCP_PORT, () => {
  console.log(`TCP Server listening on port ${TCP_PORT}`);
});

tcpServer.on('error', (err) => {
  console.error(`TCP Server error: ${err.message}`);
});

// UDP Server
const udpServer = dgram.createSocket('udp4');

udpServer.on('message', (msg: Buffer, rinfo: dgram.RemoteInfo) => {
  const message: UdpMessage = { msg, rinfo };
  console.log(`UDP Received from ${message.rinfo.address}:${message.rinfo.port}: ${message.msg.toString()}`);
  udpServer.send(message.msg, message.rinfo.port, message.rinfo.address, (err) => {
    if (err) {
      console.error(`UDP Send error to ${message.rinfo.address}:${message.rinfo.port}: ${err.message}`);
    }
  });
});

udpServer.on('listening', () => {
  const address = udpServer.address();
  console.log(`UDP Server listening on ${address.address}:${address.port}`);
});

udpServer.on('error', (err) => {
  console.error(`UDP Server error: ${err.message}`);
});

udpServer.bind(UDP_PORT);

----

export interface TcpClient {
  socket: import('tls').TLSSocket;
  address: string;
  port: number;
}

export interface UdpMessage {
  msg: Buffer;
  rinfo: import('dgram').RemoteInfo;
}

----
FROM node:18
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install
COPY tsconfig.json ./
COPY src/ ./src/
COPY server.key server.crt ./
RUN npm run build
EXPOSE 3000 3001/udp
CMD ["npm", "start"]

---

import { registerPlugin } from '@capacitor/core';
import { TcpUdpPlugin } from './definitions';

const TcpUdp = registerPlugin<TcpUdpPlugin>('tcpudpsocketclient', {
  web: () => import('./web').then(m => new m.TcpUdpPluginWeb()),
});

export * from './definitions';
export { TcpUdp };
----

import { Component } from '@angular/core';
import { TcpUdp, TcpUdpEvent, TcpUdpPlugin } from 'capacitor-tcp-udp-socket-client';

@Component({
  selector: 'app-root',
  template: `
    <button (click)="testTcp()">Test TCP</button>
    <button (click)="testUdp()">Test UDP</button>
  `,
})
export class AppComponent {
  private tcpUdp: TcpUdpPlugin = TcpUdp;

  async testTcp(): Promise<void> {
    try {
      // Register listeners
      await this.tcpUdp.addListener(TcpUdpEvent.TcpConnected, () => console.log('TCP Connected'));
      await this.tcpUdp.addListener(TcpUdpEvent.TcpDataSent, () => console.log('TCP Data Sent'));
      await this.tcpUdp.addListener(TcpUdpEvent.TcpDisconnected, () => console.log('TCP Disconnected'));
      await this.tcpUdp.addListener(TcpUdpEvent.TcpDataReceived, (event) => {
        const data = new Uint8Array(event.data);
        console.log('TCP Data Received:', new TextDecoder().decode(data));
      });
      await this.tcpUdp.addListener(TcpUdpEvent.TcpError, (event) => console.error('TCP Error:', event.message));

      // Connect to the server (use your machine's IP if testing on a device)
      await this.tcpUdp.connectTCP({ host: 'localhost', port: 3000 });
      console.log('Connecting to TCP server...');

      // Send string data
      await this.tcpUdp.sendTCP({ data: 'Hello TCP Server' });

      // Send binary data (e.g., a file buffer)
      const binaryData = new Uint8Array([1, 2, 3, 4, 5]);
      await this.tcpUdp.sendTCP({ data: binaryData });

      // Disconnect after 5 seconds
      setTimeout(async () => {
        await this.tcpUdp.disconnectTCP();
        console.log('TCP Disconnected');
      }, 5000);
    } catch (error) {
      console.error('TCP Test Failed:', error);
    }
  }

  async testUdp(): Promise<void> {
    try {
      // Register listeners
      await this.tcpUdp.addListener(TcpUdpEvent.UdpConnected, () => console.log('UDP Connected'));
      await this.tcpUdp.addListener(TcpUdpEvent.UdpDataSent, () => console.log('UDP Data Sent'));
      await this.tcpUdp.addListener(TcpUdpEvent.UdpDisconnected, () => console.log('UDP Disconnected'));
      await this.tcpUdp.addListener(TcpUdpEvent.UdpDataReceived, (event) => {
        const data = new Uint8Array(event.data);
        console.log('UDP Data Received:', new TextDecoder().decode(data));
      });
      await this.tcpUdp.addListener(TcpUdpEvent.UdpError, (event) => console.error('UDP Error:', event.message));

      // Connect to the server (use your machine's IP if testing on a device)
      await this.tcpUdp.connectUDP({ host: 'localhost', port: 3001 });
      console.log('Connecting to UDP server...');

      // Send string data
      await this.tcpUdp.sendUDP({ data: 'Hello UDP Server' });

      // Send binary data
      const binaryData = new Uint8Array([6, 7, 8, 9, 10]);
      await this.tcpUdp.sendUDP({ data: binaryData });

      // Disconnect after 5 seconds
      setTimeout(async () => {
        await this.tcpUdp.disconnectUDP();
        console.log('UDP Disconnected');
      }, 5000);
    } catch (error) {
      console.error('UDP Test Failed:', error);
    }
  }
}
