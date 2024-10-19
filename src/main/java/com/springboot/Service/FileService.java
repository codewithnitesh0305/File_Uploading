package com.springboot.Service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.springboot.Model.Product;
import com.springboot.Response.ImageResponse;

public interface FileService {

	Boolean uploadFile(MultipartFile file) throws IOException;

	byte[] downloadFile(String file) throws Exception;

	List<byte[]> getAllImages() throws IOException;

	Boolean saveProduct(Product product);

	String uploadFileWithData(MultipartFile file) throws IOException;

	List<Object> getImagesAsByteArray() throws Exception;

	List<ImageResponse> getImageWihtData() throws Exception;

	List<String> getOnlyUrl() throws Exception;


}
