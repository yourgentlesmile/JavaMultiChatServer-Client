package Client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

import Model.Datatype;

public class exec {

	volatile static boolean sta=false;
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Scanner scanner=new Scanner(System.in);
		System.out.println("---------welcome to use simple chat room---------");
		boolean canexit=false;
		while (!canexit) {
			System.out.println("1¡¢login\n2¡¢register\n0¡¢exit");
			int choice=scanner.nextInt();
			scanner.nextLine();
			postdata postdata;
			switch (choice) {
				case 1:
					postdata=new postdata(Datatype.Login, scanner);
					postdata.run();
					break;
	
				case 2:
					postdata=new postdata(Datatype.Register, scanner);
					postdata.run();
					break;
				case 0:
					System.out.println("good bye");
					canexit=true;
					break;
			}
		}
	}
	
}
