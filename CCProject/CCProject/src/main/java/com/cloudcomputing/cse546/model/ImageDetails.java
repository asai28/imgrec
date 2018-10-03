package com.cloudcomputing.cse546.model;

import java.util.concurrent.atomic.AtomicInteger;

public class ImageDetails {

	public String imageId;
	public String imageUrl;
	private static AtomicInteger ID_GENERATOR = new AtomicInteger(1000);

	
	public ImageDetails() {
	}
	
	public ImageDetails(String url) {
		this.setImageId("Image"+ID_GENERATOR.getAndIncrement());
		setImageUrl(url);
	}


	public String getImageUrl() {
		return imageUrl;
	}


	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}


	public String getImageId() {
		return imageId;
	}


	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

}
