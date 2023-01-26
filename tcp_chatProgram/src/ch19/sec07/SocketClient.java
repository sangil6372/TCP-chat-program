package ch19.sec07;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.json.JSONObject;

public class SocketClient {

	ChatServer chatServer;
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String clientIp;
	String chatName;

	public SocketClient(ChatServer chatServer, Socket socket) {
		try {
		this.chatServer = chatServer;
		this.socket = socket;
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
		
		// socket에 연결된 client의 속성을 얻기 위함
		InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
		this.clientIp = isa.getHostString();
		receive();
		
		}catch (IOException e) {
			// TODO: handle exception
		}
		}

	// 메소드 : JSON 받기 
	private void receive() {
		chatServer.threadPool.execute(()->{
			try {
				while(true) {
					String receiveJson = dis.readUTF();
					JSONObject jsonObject = new JSONObject(receiveJson);
					
					// jsonOjbec = {
//					"command" : "incoming",
//					"data" : "chatName"
//					
//					
//				}
					String command = jsonObject.getString("command");
					
					switch (command) {
					case "incoming": {
						this.chatName = jsonObject.getString("data");
						chatServer.sendToAll(this, "들어오셨습니다.");
						chatServer.addSocketClient(this);
						break;
					}
					case "message" : {
						String message = jsonObject.getString("data");
						chatServer.sendToAll(this, message);
						break;
					}
						
					}
					
				}
			} catch (Exception e) {
				chatServer.sendToAll(this, "나가셨습니다.");
				chatServer.removeSocketClient(this);
			}
			
			
			
		});
	}
	
	
	// 소켓에서 write 하는 함수
	public void send(String json) {
		try {
			dos.writeUTF(json);
			dos.flush();
						
		} catch (IOException e) {
		}
		
		
	}

	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}
