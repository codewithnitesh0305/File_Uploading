package com.springboot.Controller;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.Service.FileService;

@RestController
@RequestMapping("/api")
public class FileController {

	@Autowired
	private FileService fileService;
	
	@PostMapping("/upload")
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
	
	@GetMapping("/download")
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
		default:
			return "application/octet-stream";
				
		}
		
	}
}
