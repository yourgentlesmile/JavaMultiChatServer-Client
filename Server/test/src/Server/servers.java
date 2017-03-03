package Server;

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

import Model.Datatype;
import Model.message;

public class servers implements Runnable{
	public int port;
	public static Map<String, Object> map=new ConcurrentHashMap<>();
	public Map<String,String> usermap=new ConcurrentHashMap<>();;
	long recvTimeDelay=5000;
	class ThreadContainer{
		Socket socket;
		InputStream inputs;
		OutputStream os;
		ObjectInputStream ois;
		ObjectOutputStream oos;
		public ThreadContainer(Socket socket, InputStream inputs, OutputStream os, ObjectInputStream ois,
				ObjectOutputStream oos) {
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
	public servers(int port) {
		this.port = port;
		File file=new File("data.bin");
		if(file.exists()){
			ObjectInputStream ois;//filestream
			try {
				ois = new ObjectInputStream(new FileInputStream(file));
				usermap=(Map<String,String>)ois.readObject();//用户名密码反序列化
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
				new Thread(new server2User(socket)).start();//准备使用线程池
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	class server2User implements Runnable{
		message msg;
		String username;
		Socket socket;
		OutputStream os;
		ObjectInputStream ois;
		ObjectOutputStream oos;
		InputStream inputs;
		boolean markwithmap=true;
		boolean status=true;
		boolean markwithois=true;
		long lastrecvtime=System.currentTimeMillis();
		public server2User(Socket s) throws IOException {
			socket=s;
			os=s.getOutputStream();
			oos=new ObjectOutputStream(os);
		}
		@Override
		public void run() {
			while (status) {
				long test=System.currentTimeMillis();
				if(test-lastrecvtime>recvTimeDelay){
					long ppp=test-lastrecvtime;
					killsession();
				}else{
					try {
						inputs=socket.getInputStream();
						if(inputs.available()>0){
							if(ois==null) ois=new ObjectInputStream(inputs);
							markwithois=false;
							msg=(message)ois.readObject();
							lastrecvtime=System.currentTimeMillis();
							//0：chatmessage  2:heartpackage  3:register  4:login  5:exit
							MsgAction(msg.getMsgtype());
						}else {
							Thread.sleep(10);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		private void MsgAction(int actiontype) throws IOException{
			switch (actiontype) {
				case Datatype.Msg:
					BoardcastMsg(username+" said:"+(String)msg.getMsg());
					break;
				case Datatype.HeartPKG:
					break;
				case Datatype.Register:
					if(!usermap.containsKey(msg.getUsername())){
						usermap.put(msg.getUsername(),msg.getPassword());
						username=msg.getUsername();
						BoardcastMsg(username+" join the chat room");
						ServerMsg("Now You can send Message ");
						if(markwithmap) {
							map.put(msg.getUsername(), new ThreadContainer(socket, inputs, os, ois, oos));
							markwithmap=false;
						}
					}else oos.writeObject(new message(Datatype.Register, "-1", null, null));
					break;
				case Datatype.Login:
					if(usermap.containsKey(msg.getUsername())){
						if(usermap.get(msg.getUsername()).equals(msg.getPassword())){
							username=msg.getUsername();
							if(markwithmap) {
								map.put(msg.getUsername(), new ThreadContainer(socket, inputs, os, ois, oos));
								markwithmap=false;
							}
							oos.writeObject(new message(Datatype.Login, "Login success", null, null));
							BoardcastMsg(username+" join the chat room");
							ServerMsg("Now You can send Message ");
						}else oos.writeObject(new message(Datatype.Login, "0", null, null));//0 ：密码错误
					}else oos.writeObject(new message(Datatype.Login, "-1", null, null));   //-1:用户不存在
					break;
				case Datatype.Exit:
					BoardcastMsg(username+" leave the chat room");
					map.remove(username);
					socket.close();
					status=false;
					break;
			}
		}
		
		private void BoardcastMsg(String sendmsg) throws IOException{
			for(Map.Entry<String, Object> entry:map.entrySet()){
				if(entry.getKey().equals(username)) 
					continue;
				ThreadContainer s=(ThreadContainer)entry.getValue();
				ObjectOutputStream temp_oos=s.getOos();
				temp_oos.writeObject(new message(Datatype.Msg, sendmsg, null, null));
			}
		}
		private void ServerMsg(String s) throws IOException{
			oos.writeObject(new message(Datatype.Msg, s, null, null));
		}
		private void killsession(){
			if(status) status=false;
			if(socket!=null&&map.containsKey(username)){
				try {
					BoardcastMsg(username+" leave the chat room");
					map.remove(username);
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
