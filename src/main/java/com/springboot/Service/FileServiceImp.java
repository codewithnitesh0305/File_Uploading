package com.springboot.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.Model.Product;
import com.springboot.Repository.ProductRepository;
import com.springboot.Response.ImageResponse;

import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

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
		System.out.println("File Path: "+ fullPath);
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
			return fileName;
		}
		return null;
	}
	

    
//    @Override //Working
//    public List<String> getImageUrls1() throws Exception {
//        File directory = new File(uploadPath);
//        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || 
//                                                             name.toLowerCase().endsWith(".jpeg") ); // Add other image formats as needed
//
//        List<String> imageUrls = new ArrayList<>();
//        if (files != null) {
//            for (File file : files) {
//                String fileUrl = "/api/download?file=" + URLEncoder.encode(file.getName(), StandardCharsets.UTF_8);
//                imageUrls.add(fileUrl);
//            }
//        }
//        return imageUrls;
//    }
    
    @Override //Working
    public List<String> getOnlyUrl() throws Exception {
        List<String> imageNames = productRepository.findAllImageNames(); // Fetching image names from DB
        String baseUrl = "http://localhost:8080/api/download?file="; // Set the base URL

        return imageNames.stream()
                .map(name -> baseUrl + URLEncoder.encode(name, StandardCharsets.UTF_8))
                .collect(Collectors.toList());
    }
	
	@Override
	public List<Object> getImagesAsByteArray() throws Exception {
	    List<String> imageNames = productRepository.findAllImageNames(); // Fetching image names from DB
	    String baseUrl = "http://localhost:8080/api/download?file="; // Set the base URL
	    List<Object> result = new ArrayList<>();

	    for (String name : imageNames) {
	        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
	        String imageUrl = baseUrl + encodedName;

	        // Check if the image can be fetched as byte array
	        byte[] imageBytes = fetchImageAsByteArray(name); // Implement this method to get byte array
	        if (imageBytes != null && imageBytes.length > 0) {
	            result.add(imageBytes); // Add byte array to results if it exists
	        } else {
	            result.add(imageUrl); // Otherwise, add the URL
	        }
	    }

	    return result;
	}

	// Example method to fetch image as byte array
	private byte[] fetchImageAsByteArray(String fileName) {
	    try {
	        // Implement logic to fetch image bytes from storage or database
	        // For example:
	        InputStream stream = new FileInputStream(uploadPath + fileName);
	        return StreamUtils.copyToByteArray(stream);
	    } catch (IOException e) {
	        return null; // Return null if there's an error fetching the image
	    }
	}
	
	@Override
	public List<ImageResponse> getImageWihtData() throws Exception {
	    List<Product> imageEntities = productRepository.findAll(); // Fetching all images from DB
	    String baseUrl = "http://localhost:8080/api/download?file="; // Set the base URL for images
	    List<ImageResponse> result = new ArrayList<>();

	    for (Product entity : imageEntities) {
	        String encodedName = URLEncoder.encode(entity.getImageName(), StandardCharsets.UTF_8);
	        String imageUrl = baseUrl + encodedName; // Construct the URL for each image
	        
	        // Create an ImageResponse object with metadata and URL
	        ImageResponse imageResponse = new ImageResponse(entity.getId(), entity.getDescription(), entity.getImageName(), imageUrl);
	        result.add(imageResponse); // Add to results
	    }

	    return result; // Return a list of ImageResponse objects
	}
}
