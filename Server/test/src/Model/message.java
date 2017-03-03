package Model;

import java.io.Serializable;

public class message implements Serializable{
	int msgtype;//0£ºchatmessage  1£ºfile  2:heartpackage  3:register  4:login
	Object msg;
	String username;
	String password;
	
	public message(int msgtype, Object msg, String username, String password) {
		super();
		this.msgtype = msgtype;
		this.msg = msg;
		this.username = username;
		this.password = password;
	}
	public int getMsgtype() {
		return msgtype;
	}
	public void setMsgtype(int msgtype) {
		this.msgtype = msgtype;
	}
	public Object getMsg() {
		return msg;
	}
	public void setMsg(Object msg) {
		this.msg = msg;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
