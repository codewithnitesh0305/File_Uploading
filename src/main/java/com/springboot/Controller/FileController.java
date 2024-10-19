package com.springboot.Controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.Model.Product;
import com.springboot.Repository.ProductRepository;
import com.springboot.Response.ImageResponse;
import com.springboot.Service.FileService;

import ch.qos.logback.core.util.StringUtil;
import io.micrometer.common.util.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class FileController {

	@Autowired
	private FileService fileService;
	
	@Value("${file.upload.path}")
	private String uploadPath;
	
	@PostMapping("/UploadFile")
	public ResponseEntity<?> uploadFile(@RequestParam MultipartFile file){
		try {
			Boolean uploadFile = fileService.uploadFile(file);
			if(uploadFile) {
				return new ResponseEntity<>("File Upload Successfully...", HttpStatus.CREATED);
			}else {
				return new ResponseEntity<>("File Upload Failed...",HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/GetImages")
	public ResponseEntity<?> downloadFile(@RequestParam String file){
		try {
			byte[] downloadFile = fileService.downloadFile(file);
			String contentType = getContentType(file);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(contentType));
			//headers.setContentLength(file.length());
			headers.setContentDispositionFormData("attachememt", file);
			return ResponseEntity.ok().headers(headers).body(downloadFile);
		}catch (FileNotFoundException e) {
			return new ResponseEntity<>("Fiel not found...",HttpStatus.INTERNAL_SERVER_ERROR);
		}catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public String getContentType(String filename) {
		String extension = FilenameUtils.getExtension(filename);
		switch(extension) {
		case "pdf":
			return "application/pdf";
		case "xlsx":
			return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		case "txt":
			return "text/plan";
		case "png":
			return "image/png";
		case "jpeg":
			return "image/jpeg";
		case "jpg":
			return "image/jpg";
		default:
			return "application/octet-stream";
				
		}
		
	}
	
	@GetMapping("/images")
    public ResponseEntity<byte[]> getImagesAsZip() {
        try {
            List<byte[]> images = fileService.getAllImages();
            if (images.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            for (int i = 0; i < images.size(); i++) {
                byte[] image = images.get(i);
                ZipEntry entry = new ZipEntry("image" + i + ".jpeg"); // Adjust extension as needed
                zos.putNextEntry(entry);
                zos.write(image);
                zos.closeEntry();
            }
            zos.finish();
            zos.close();
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=images.zip");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity.ok().headers(headers).body(baos.toByteArray());
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/UploadDataWithImage")
	public ResponseEntity<?> uploadFileWithData(@RequestParam String product, @RequestParam MultipartFile file){
    	System.out.println("Product: "+ product);
    	System.out.println("File: "+ file);
		try {
			 String filename = fileService.uploadFileWithData(file);
			if(filename != null) {
				
				ObjectMapper objectMapper = new ObjectMapper();
				
				Product readValue = objectMapper.readValue(product, Product.class);
				//String filename = file.getOriginalFilename();
				readValue.setImageName(filename);
				Boolean saveProduct = fileService.saveProduct(readValue);
				if(saveProduct) {
					return new ResponseEntity<>("Data and File Upload Successfully...", HttpStatus.CREATED);
				}else {
					return new ResponseEntity<>("Data and File Upload Failed...", HttpStatus.CREATED);
				}
			}else {
				return new ResponseEntity<>("Data and File Upload Failed...",HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}catch(Exception e) {
			return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

    @GetMapping("/files/{imageName}")
    public ResponseEntity<Resource> serveImage(@PathVariable String imageName) throws MalformedURLException {
        Path imagePath = Paths.get(uploadPath).resolve(imageName).normalize();
        Resource resource = new UrlResource(imagePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Adjust if you support other types like PNG
                .body(resource);
    }
    
    
    @GetMapping("/ByteArrayImages")
    public ResponseEntity<?> getAllImageUrls1() {
        try {
            List<Object> imageUrls = fileService.getImagesAsByteArray();
            return ResponseEntity.ok(imageUrls);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/imageWithData")
    public ResponseEntity<?> getimageWithData() {
        try {
            List<ImageResponse> imageUrls = fileService.getImageWihtData();
            return ResponseEntity.ok(imageUrls);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/OnlyUrl")
    public ResponseEntity<?> getOnlyUrl() {
        try {
            List<String> imageUrls = fileService.getOnlyUrl();
            return ResponseEntity.ok(imageUrls);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
