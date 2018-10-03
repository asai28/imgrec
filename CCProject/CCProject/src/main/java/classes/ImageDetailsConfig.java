package classes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudcomputing.cse546.model.ImageDetails;
import com.cloudcomputing.cse546.model.ResponseMessage;

@Configuration
public class ImageDetailsConfig {

	@Bean
	public ImageDetails imageDetails() {
		return new ImageDetails();
	}

	@Bean 
	public ResponseMessage responseMessage() {
		return new ResponseMessage();
	}
}
