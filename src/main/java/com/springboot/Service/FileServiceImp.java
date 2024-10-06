package com.springboot.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.Model.Product;
import com.springboot.Repository.ProductRepository;

@Service
public class FileServiceImp implements FileService{

	@Value("${file.upload.path}")
	private String uploadPath;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Override
	public Boolean uploadFile(MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		String fileName = file.getOriginalFilename();
		File saveFile = new File(uploadPath);
		if(!saveFile.exists()) {
			saveFile.mkdir();
		}
		String storedPath = uploadPath.concat(fileName);
		long upload = Files.copy(file.getInputStream(), Paths.get(storedPath));
		if(upload != 0) {
			return true;
		}
		return false;
	}

	@Override
	public byte[] downloadFile(String file) throws Exception {
		// TODO Auto-generated method stub
		String fullPath = uploadPath.concat(file);
		try {
			InputStream stream = new FileInputStream(fullPath);
			return StreamUtils.copyToByteArray(stream);
			
		}catch(FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		}catch(IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@Override
	public List<byte[]> getAllImages() throws IOException {
	    File folder = new File(uploadPath);
	    File[] files = folder.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpeg") || name.endsWith(".jpg"));

	    List<byte[]> imageFiles = new ArrayList<>();
	    if (files != null) {
	        for (File file : files) {
	            try (InputStream stream = new FileInputStream(file)) {
	                imageFiles.add(StreamUtils.copyToByteArray(stream));
	            }
	        }
	    }
	    return imageFiles;
	}

	@Override
	public Boolean saveProduct(Product product) {
		// TODO Auto-generated method stub
		Product save = productRepository.save(product);
		if(!ObjectUtils.isEmpty(save)) {
			return true;
		}
		return false;
	}

	
	@Override
	public String uploadFileWithData(MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		String fileName = file.getOriginalFilename();
		File saveFile = new File(uploadPath);
		
		//Create a unique name of file name
		String randomString = UUID.randomUUID().toString();
		String removeExtension = FilenameUtils.removeExtension(fileName);
		fileName = removeExtension+"_"+randomString+"."+FilenameUtils.getExtension(fileName);
		System.out.println("Filename: "+ fileName);
		
		if(!saveFile.exists()) {
			saveFile.mkdir();
		}
		String storedPath = uploadPath.concat(fileName);
		System.out.println("StoredPath: "+ storedPath);
		long upload = Files.copy(file.getInputStream(), Paths.get(storedPath));
		if(upload != 0) {
			return storedPath;
		}
		return null;
	}
}
