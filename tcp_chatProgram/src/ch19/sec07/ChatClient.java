package ch19.sec07;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.json.JSONObject;

public class ChatClient {
	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;
	String chatName;

	public void connect() throws UnknownHostException, IOException {
		socket = new Socket("localhost", 50001);
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		System.out.println("[서버]에 연결됨");
	}

	public void receive() {
		Thread thread = new Thread(() -> {

			try {
				String json = dis.readUTF();

				JSONObject root = new JSONObject(json);
				String clientIp = root.getString("clientIp");
				String chatName = root.getString("chatName");
				String message = root.getString("message");
				System.out.println("(" + chatName + "@" + clientIp + "> " + message);

			} catch (IOException e) {
				System.out.println("[클라이언트] 서버 연결 끊김");
				System.exit(0);
			}

		});
		thread.start();

	}

	public void send(String json) throws IOException {
		dos.writeUTF(json);
		dos.flush();
	}

	public void unconnect() throws IOException {
		socket.close();
	}

	public static void main(String[] args) {
		try {
			ChatClient chatClient = new ChatClient();
			chatClient.connect(); // 서버랑 연결

			Scanner sc = new Scanner(System.in);
			
			System.out.println("대화명 입력: ");
			chatClient.chatName = sc.nextLine();
			
			//JSON 데이터 만들기 
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("command", "incoming");
			jsonObject.put("data", chatClient.chatName);
			
			String json = jsonObject.toString();
			chatClient.send(json);
			
			// 보냈으면 서버에서 데이터 받기 
			chatClient.receive();
			
			System.out.println("--------------------------------------------------------------------");
			System.out.println("보낼 메세지를 입력하고 Enter키를 누르세요.");
			System.out.println("채팅을 종료하려면 q를 입력하고 Enter키를 누르세요.");
			System.out.println("--------------------------------------------------------------------");
			
			while (true) {
				String message = sc.nextLine();
				if (message.toLowerCase().equals("q"))
					break;
				else {
					//JSON 데이터 만들기 
					jsonObject = new JSONObject();
					jsonObject.put("command", "incoming");
					jsonObject.put("data", chatClient.chatName);
					
					json = jsonObject.toString();
					chatClient.send(json);
					
				}
			}
			sc.close();
			chatClient.unconnect();
		
			
		} catch (Exception e) {
			System.out.println("[클라이언트] 서버 연결 안됨");
		}

	}

}
