package Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JOptionPane;

import Model.Datatype;
import Model.Heartpkgcallback;
import Model.message;

public class postdata implements Heartpkgcallback{
	
	int mode;
	Scanner scanner;
	Socket socket=null;
	volatile OutputStream outs=null;
	volatile InputStream inputs=null;
	volatile ObjectOutputStream oos=null;
	volatile ObjectInputStream ois=null;
	volatile boolean status_heartpkg=true;
	volatile boolean status_watchdog=true;
	Thread watchdg=new Thread(new watchdog());
	public postdata(int mode,Scanner sc) {
		this.mode=mode;
		scanner=sc;
	}
	Timer timer=new Timer();
	private void getconn() {
		try {
			socket=new Socket("127.0.0.1", 8369);
			outs=socket.getOutputStream();
			oos=new ObjectOutputStream(outs);
			inputs=socket.getInputStream();
		} catch (Exception e) {
			System.out.println("Can not connect Server");
			System.exit(0);
		}

	}
	class task extends TimerTask{
		Heartpkgcallback callback;
		
		public task(Heartpkgcallback callback) {
			this.callback = callback;
		}
		@Override
		public void run() {
			called();
		}
		public void called() {
			callback.execute();
		}
		
	}
	public boolean login(message msg){
		if("0".equals((String)msg.getMsg())||"-1".equals((String)msg.getMsg())){
			System.out.println("username or password error please try again.");
			return false;
		}
		return true;
	}
	public void run() {
		status_heartpkg=true;
		status_watchdog=true;
		int trynum=3;
		try {
			switch (mode) {
				case Datatype.Login:
					message temps=login_or_register(Datatype.Login);
					if(socket==null){
						getconn();
					}
					synchronized (oos) {
						oos.writeObject(temps);
						if(ois==null) {
							ois=new ObjectInputStream(inputs);
						}
						watchdg.start();
						timer.schedule(new task(this), 1000,2000);
						oos.wait();
						chat();
					}
					break;
				case Datatype.Register:
					message tempss=login_or_register(Datatype.Register);
					if(socket==null) getconn();
					oos.writeObject(tempss);
					ois=new ObjectInputStream(inputs);
					watchdg.start();
					timer.schedule(new task(this), 1000,2000);
					chat();
					break;
			}
		} catch (IOException e) {
			System.out.println("Server error");
		} catch (InterruptedException e) {
			System.out.println("Internal Client Error");
		}
	}
	public message login_or_register(int mode){
		String username="";
		String password="";
		username=JOptionPane.showInputDialog("用户名");
		password=JOptionPane.showInputDialog("密码");
		return new message(mode, null, username, password);
	}
	public void chat() throws IOException, InterruptedException{
		while (true) {
			String words=scanner.nextLine();
			String source=words.trim();
			if(source.charAt(0)=='/'){
				int st=command(source.substring(1));
				if((st==1)||(st==2)) continue;
				return;
			}
			message msg=new message(Datatype.Msg,words, null,null);
			oos.writeObject(msg);
		}
	}
	public int command(String cmd) throws IOException, InterruptedException{
		switch (cmd) {
		case "help":
		case "?":
			System.out.println("/help /? 显示帮助信息");
			System.out.println("/exit 退出聊天");
			return 1;
		case "exit":
			oos.writeObject(new message(Datatype.Exit,null, null,null));
			socket.close();
			inputs.close();
			ois.close();
			oos.close();
			outs.close();
			timer.cancel();
			watchdg.stop();
			status_heartpkg=false;
			status_watchdog=false;
			return 0;
		case "eme":
			timer.schedule(new task(this), 1000,2000);
			watchdg.stop();
			inputs.close();
			ois.close();
			oos.close();
			outs.close();
			socket.close();
			status_heartpkg=false;
			status_watchdog=false;
			return 0;
		default:
			System.out.println("无效命令");
			return 2;
		}
	}
	@Override
	public void execute() {
		try {
			oos.writeObject(new message(Datatype.HeartPKG, null, null, null));
		} catch (Exception e) {
			System.out.println("Connection report: server have been closed");
			if(status_watchdog!=false){
				try {
					command("eme");
					System.exit(0);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}
//	class heartpackage implements Runnable{
//		@Override
//		public void run() {
//			try {
//				while (status_heartpkg) {
//				oos.writeObject(new message(Datatype.HeartPKG, null, null, null));
//				Thread.sleep(2000);
//				}
//			} catch (Exception e) {
//				System.out.println("Connection report: server have been closed");
//				if(status_heartpkg!=false||status_watchdog!=false){
//					try {
//						command("eme");
//					} catch (Exception e1) {
//						e1.printStackTrace();
//					}
//				}
//			}
//		}
//	}
	class watchdog implements Runnable{
		@Override
		public void run() {
			int loginchecktimes=3;
			try {
				while (status_watchdog) {
					if(inputs.available()>0){
						message msg=(message)ois.readObject();
						switch (msg.getMsgtype()) {
							case Datatype.Msg:
								System.out.println((String)msg.getMsg());
								break;
							case Datatype.HeartPKG:
								;
								break;
							case Datatype.Register:
								if("-1".equals((String)msg.getMsg())){
									System.out.println("username already exist");
									message tempss=login_or_register(Datatype.Register);
									oos.writeObject(tempss);
								}
								break;
							case Datatype.Login:
								if (loginchecktimes==1) {
									throw new Exception();
								}
								synchronized (oos) {
									if(login(msg)) oos.notify();
									else{
										loginchecktimes--;
										message tempss=login_or_register(Datatype.Login);
										oos.writeObject(tempss);
									}
								}
								break;
							case Datatype.Exit:
								;
								break;
						}
					}
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				System.out.println("Socket report: server have been close this connection");
				if(status_heartpkg!=false||status_watchdog!=false){
					try {
						command("eme");
					} catch (IOException | InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
}
