package com.springboot.Service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

	public Boolean uploadFile(MultipartFile file) throws IOException;
	public byte[] downloadFile(String file) throws Exception;
	public List<byte[]> getAllImages() throws IOException;
}
