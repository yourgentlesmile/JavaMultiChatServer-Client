package Model;

import java.io.Serializable;

public class FileModel implements Serializable{
	String filename;
	byte[] data;
	
	public FileModel(String filename, byte[] data) {
		this.filename = filename;
		this.data = data;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
}
