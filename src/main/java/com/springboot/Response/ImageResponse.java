package com.springboot.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ImageResponse {

	
	private int id; // ID of the image
    private String description; // Description of the image
    private String imageName; // Name of the image
    private String url; 
    
  
}
