package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import model.Datatype;
import model.Message;

public class Servers implements Runnable{
	public int port;
	public static Map<String, Object> map=new ConcurrentHashMap<>();
	public Map<String,String> userMap=new ConcurrentHashMap<>();;
	long recvTimeDelay=5000;
	class ThreadContainer{
		Socket socket;
		InputStream inputs;
		OutputStream os;
		ObjectInputStream ois;
		ObjectOutputStream oos;
		public ThreadContainer(Socket socket, InputStream inputs, OutputStream os, ObjectInputStream ois
			,ObjectOutputStream oos) {
			super();
			this.socket = socket;
			this.inputs = inputs;
			this.os = os;
			this.ois = ois;
			this.oos = oos;
		}
		public Socket getSocket() {
			return socket;
		}
		public InputStream getInputs() {
			return inputs;
		}
		public OutputStream getOs() {
			return os;
		}
		public ObjectInputStream getOis() {
			return ois;
		}
		public ObjectOutputStream getOos() {
			return oos;
		}
		
	}
	public Servers(int port) {
		this.port = port;
		File file=new File("data.bin");
		if(file.exists()){
			ObjectInputStream ois;//filestream
			try {
				ois = new ObjectInputStream(new FileInputStream(file));
				userMap=(Map<String,String>)ois.readObject();//用户名密码反序列化
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		ServerSocket serversocket;
		try {
			serversocket = new ServerSocket(port,5);
			while (true) {
				Socket socket=serversocket.accept();
				//线程创建
				new Thread(new ServerToUser(socket)).start();//准备使用线程池
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	class ServerToUser implements Runnable{
		Message msg;
		String userName;
		Socket socket;
		OutputStream os;
		ObjectInputStream ois;
		ObjectOutputStream oos;
		InputStream inputs;
		boolean markWithMap=true;
		boolean status=true;
		boolean markWithOis=true;
		long lastReceiveTime=System.currentTimeMillis();
		public ServerToUser(Socket s) throws IOException {
			socket=s;
			os=s.getOutputStream();
			oos=new ObjectOutputStream(os);
		}
		@Override
		public void run() {
			while (status) {
				long test=System.currentTimeMillis();
				if(test-lastReceiveTime>recvTimeDelay){
					long ppp=test-lastReceiveTime;
					killSession();
				}else{
					try {
						inputs=socket.getInputStream();
						if(inputs.available()>0){
							if(ois==null) ois=new ObjectInputStream(inputs);
							markWithOis=false;
							msg=(Message)ois.readObject();
							lastReceiveTime=System.currentTimeMillis();
							//0：chatmessage  2:heartpackage  3:register  4:login  5:exit
							msgAction(msg.getMsgtype());
						}else {
							Thread.sleep(10);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		private void msgAction(int actiontype) throws IOException{
			switch (actiontype) {
				case Datatype.MSG:
					boardCastMsg(userName+" said:"+(String)msg.getMsg());
					break;
				case Datatype.HEARTPKG:
					break;
				case Datatype.REGISTER:
					if(!userMap.containsKey(msg.getUsername())){
						userMap.put(msg.getUsername(),msg.getPassword());
						userName=msg.getUsername();
						boardCastMsg(userName+" join the chat room");
						serverMsg("Now You can send Message ");
						if(markWithMap) {
							map.put(msg.getUsername(), new ThreadContainer(socket, inputs, os, ois, oos));
							markWithMap=false;
						}
					}else oos.writeObject(new Message(Datatype.REGISTER, "-1", null, null));
					break;
				case Datatype.LOGIN:
					if(userMap.containsKey(msg.getUsername())){
						if(userMap.get(msg.getUsername()).equals(msg.getPassword())){
							userName=msg.getUsername();
							if(markWithMap) {
								map.put(msg.getUsername(), new ThreadContainer(socket, inputs, os, ois, oos));
								markWithMap=false;
							}
							oos.writeObject(new Message(Datatype.LOGIN, "Login success", null, null));
							boardCastMsg(userName+" join the chat room");
							serverMsg("Now You can send Message ");
						}else oos.writeObject(new Message(Datatype.LOGIN, "0", null, null));//0 ：密码错误
					}else oos.writeObject(new Message(Datatype.LOGIN, "-1", null, null));   //-1:用户不存在
					break;
				case Datatype.EXIT:
					boardCastMsg(userName+" leave the chat room");
					map.remove(userName);
					socket.close();
					status=false;
					break;
			}
		}
		
		private void boardCastMsg(String sendmsg) throws IOException{
			for(Map.Entry<String, Object> entry:map.entrySet()){
				if(entry.getKey().equals(userName)) 
					continue;
				ThreadContainer s=(ThreadContainer)entry.getValue();
				ObjectOutputStream temp_oos=s.getOos();
				temp_oos.writeObject(new Message(Datatype.MSG, sendmsg, null, null));
			}
		}
		private void serverMsg(String s) throws IOException{
			oos.writeObject(new Message(Datatype.MSG, s, null, null));
		}
		private void killSession(){
			if(status) status=false;
			if(socket!=null&&map.containsKey(userName)){
				try {
					boardCastMsg(userName+" leave the chat room");
					map.remove(userName);
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
