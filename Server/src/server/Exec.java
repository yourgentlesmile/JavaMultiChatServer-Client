package server;

import java.util.Scanner;

import javax.swing.JOptionPane;

public class Exec {  
	public static int port=8369;
	public static String version="v1.0.0";
	public static boolean serverStatus=false;
    public static void main(String[] args) { 
    	serverAction();
    }
    public static void menu(){
    	String title="----------welcome use Simple multiplayer chat server----------\n";
    	String menuitem1="   menu:   /s           display all the server information\n";
    	String menuitem2="           /o           change listen port\n";
    	String menuitem3="           /start       start server\n";
    	String menuitem4="           /h or /?     display help\n";
    	String menuitem5="           /e           exit\n";
    	System.out.println(title+menuitem1+menuitem2+menuitem3+menuitem4+menuitem5);
    }
    public static void serverAction() {
		menu();
		boolean isrunning=true;
		Scanner scanner=new Scanner(System.in);
		Thread serverrun =null;
		while (isrunning) {
			String cmd=((scanner.nextLine()).trim()).substring(1);
			switch (cmd) {
			case "s":
				System.out.println("version: "+version);
				System.out.println("designer: XC");
				System.out.println("using port: "+port);
				System.out.println("server status: "+(serverStatus?"Running":"Stopped"));
				if(serverStatus) System.out.println("online user count: "+Servers.map.size());
				else System.out.println("online user count: Server stopped");
				break;
			case "o":
				while (true) {
					int temp=0;;
					temp=Integer.parseInt(JOptionPane.showInputDialog("端口号"));
					if(temp<8000) {
						System.out.println("请输入大于8000的端口号");
					}else {
						port=temp;
						break;
					}
				}
				break;
			case "start":
				System.out.println("server running....");
				serverStatus=true;
				serverrun=new Thread(new Servers(port));
				serverrun.start();
				break;
			case "h":
			case "?":
				menu();
				break;
			case "e":
				System.out.println("Good bye");
				if(serverrun!= null) serverrun.stop();
				System.exit(0);
				return;
			default:
				System.out.println("无效命令");
				break;
			}
		}
	}
}