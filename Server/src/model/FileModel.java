package model;

import java.io.Serializable;

public class FileModel implements Serializable{
	String fileName;
	byte[] data;
	
	public FileModel(String filename, byte[] data) {
		this.fileName = filename;
		this.data = data;
	}
	public String getFilename() {
		return fileName;
	}
	public void setFilename(String filename) {
		this.fileName = filename;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
}
