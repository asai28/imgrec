package com.amazonaws.load;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class loadBalancerInstance {

	private static final String REQUEST_QUEUE_NAME = "requestQueue";
	private static int i=1;

	public static void main(String args[]) throws IOException {

		checkQForMessage();
		//System.out.println("Successfully completed.");
	}

	public static void checkQForMessage() {

		int instanceCount = 0;
		final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		String queryUrl = sqs.getQueueUrl(REQUEST_QUEUE_NAME).getQueueUrl();
		final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
				.withQueueUrl(sqs.getQueueUrl(REQUEST_QUEUE_NAME).getQueueUrl());
		receiveMessageRequest.setVisibilityTimeout(10);
		receiveMessageRequest.setWaitTimeSeconds(10);
		receiveMessageRequest.setMaxNumberOfMessages(10);

		while (true) {
			final List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();

			for (final Message message : messages) {
				instanceCount = getInstanceCount();
				System.out.println("instanceCount: "+instanceCount);
				if (instanceCount < 18) {
					createAppTierInstance();
					System.out.println(message.getBody());
					//sqs.deleteMessage(queryUrl, message.getReceiptHandle());
				}
			}
		}
	}

	public static int getInstanceCount() {

		boolean done = false;

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-west-1").build();
		int instanceCount = 0;

		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for (Reservation reservation : response.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					if (instance.getState().getName().equalsIgnoreCase("running") || instance.getState().getName().equalsIgnoreCase("pending")) {
						instanceCount++;
					}
				}
			}
			request.setNextToken(response.getNextToken());

			if (response.getNextToken() == null) {
				done = true;
			}
		}
		return instanceCount;
	}

	public static void createAppTierInstance() {
	     i=i+1;
	   String appname="App-Instance "+i;
		Collection<TagSpecification> tagSpecifications = new ArrayList<TagSpecification>();
		TagSpecification tagSpecification = new TagSpecification();
		Collection<Tag> tags = new ArrayList<Tag>();
		Tag t = new Tag();
		t.setKey("Name");
		t.setValue(appname);
		tags.add(t);
		tagSpecification.setResourceType("instance");
		tagSpecification.setTags(tags);
		tagSpecifications.add(tagSpecification);
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withRegion("us-west-1").build();
		
		RunInstancesRequest req = new RunInstancesRequest();
		req.withImageId("ami-c56071a5").withInstanceType("t2.micro").withMaxCount(1).withMinCount(1).withKeyName("keypair")
				.withSecurityGroupIds("sg-e6cb9a9f");

		RunInstancesResult res = ec2.runInstances(req);
	}
}
