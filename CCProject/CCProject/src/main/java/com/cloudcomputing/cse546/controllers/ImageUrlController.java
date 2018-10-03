package com.cloudcomputing.cse546.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.cloudcomputing.cse546.model.ImageDetails;

import classes.RequestQueueImpl;
import classes.ResponseQueueImpl;
import classes.sqsSample;

@Controller
public class ImageUrlController{

	
	@GetMapping("/cloudapp")
	public String getDetails(Model model) {
		model.addAttribute("imageDetail", new ImageDetails());
		return "ImageVerifier";
	}
	
	@PostMapping("/cloudresponse")
	public String getImageRequest(@ModelAttribute ImageDetails imageDetail) throws InterruptedException {
		sqsSample.createbatches(imageDetail.getImageUrl());
		return "Response";
	}
	
	@RequestMapping(value="/cloudimagerecognition",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Map<String,String>> getImageDetails(@RequestParam("input") String imageURL) {
		boolean isArrived=false;
		String value = "Timedout";
		int timeoutPeriod=0;
		
		long startTime = System.currentTimeMillis()/1000;
		RequestQueueImpl requestQueue = new RequestQueueImpl();
		requestQueue.sendMessagesToQueue(imageURL);
		ResponseQueueImpl responseQueue = new ResponseQueueImpl();
		BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIAIAODANXHYHUEQVXQ",
				"xEyugc4+Y3DCxNXDxDrYf3CA2PD/rCwxImtWjGpt");
		final AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion("us-west-1")
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();
		String queryUrl = sqs.getQueueUrl("responseQueue").getQueueUrl();
		ReceiveMessageRequest receiveMessageRequest = responseQueue.createMessageRequest(sqs);
		while(!isArrived && timeoutPeriod<30) {
			timeoutPeriod = (int) (System.currentTimeMillis()/1000 - startTime);
			List<Message> messages = responseQueue.getMessagesFromQueue(sqs, receiveMessageRequest);
			for (final Message message : messages) {
				String[] a = (message.getBody()).split(",");
				String key=a[0].substring(1,a[0].length());
				if(key.equalsIgnoreCase(imageURL.substring(33,imageURL.length()))) {
					isArrived=true;
					value = message.getBody().substring(a[0].length()+1, message.getBody().length()-1);
					sqs.deleteMessage(queryUrl, message.getReceiptHandle());
				}
				System.out.println(timeoutPeriod);
			}
		}
		Map<String,String> response = new HashMap<String, String>();
		response.put(imageURL, value);
		return ResponseEntity.accepted().body(response);
	}
	
}