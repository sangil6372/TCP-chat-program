package ch19.sec07;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;

public class ChatServer {

	ServerSocket serverSocket;
	ExecutorService threadPool = Executors.newFixedThreadPool(100); // 100개 클라이언트 동시 채팅
	Map<String, SocketClient> chatRoom = Collections.synchronizedMap(new HashMap<>());

	public void start() throws IOException {
		serverSocket = new ServerSocket(50001);
		System.out.println("[서버] 시작됨");
		
		Thread thread = new Thread(()->{
			try {
				while(true) {
					Socket socket = serverSocket.accept();
					SocketClient sc = new SocketClient(this, socket); //ChatServer 객체, ChatSever에서 생성한 소켓으로 SocketClient 객체 생성
				}
			} catch (IOException e) {
			}
		});
		
		thread.start();
		
	}

	public void addSocketClient(SocketClient socketClient) {
		String key = socketClient.chatName + "@" + socketClient.clientIp;
		chatRoom.put(key, socketClient);
		System.out.println("입장 : " + key);
		System.out.println("현재 채팅자 수 : " + chatRoom.size() + "\n");
	}
	
	public void removeSocketClient(SocketClient socketClient) {
		String key = socketClient.chatName + "@" + socketClient.clientIp;
		chatRoom.remove(key);
		System.out.println("퇴장 : " + key);
		System.out.println("현재 채팅자 수 : " + chatRoom.size() + "\n");
	}
	
	public void sendToAll(SocketClient sender, String message) {
		
		JSONObject root = new JSONObject();
		root.put("clientIp", sender.clientIp);
		root.put("chatName", sender.chatName);
		root.put("message", message);
		
		String json = root.toString();
		
		// chatRoom 의 value 로만 collection생성
		Collection<SocketClient> socketClient = chatRoom.values();
		
		for (SocketClient sc : socketClient) {
			if (sc == sender) continue;
			sc.send(json);       //	sc.dos.writeUTF(json);
		}
		
		
	}
	
	public void stop() {
		try {
			serverSocket.close();
			threadPool.shutdownNow();
			chatRoom.values().stream().forEach(sc->sc.close());
			System.out.println("[서버] 종료 됨");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public static void main(String[] args) {
		try {
			
		ChatServer chatServer = new ChatServer();
		chatServer.start();

		System.out.println("--------------------------------------------------------------------");
		System.out.println("서버를 종료하려면 q 또는 Q를 입력하고 Enter 키를 입력하세요");
		System.out.println("--------------------------------------------------------------------");

		Scanner sc = new Scanner(System.in);

		while (true) {
			String key = sc.next().toLowerCase();
			if (key.equals("q"))
				break;
		}

		sc.close();
		chatServer.stop();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}

