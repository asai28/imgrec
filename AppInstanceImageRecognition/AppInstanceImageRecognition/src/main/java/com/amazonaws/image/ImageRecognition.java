package com.amazonaws.image;



import static java.nio.file.Files.deleteIfExists;

import java.io.BufferedReader;

import java.io.File;

import java.io.FileReader;

import java.io.IOException;

import java.nio.file.Paths;

import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;

import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;

import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

import com.amazonaws.services.s3.AmazonS3;

import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import com.amazonaws.services.sqs.AmazonSQS;

import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

import com.amazonaws.services.sqs.model.Message;

import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import com.amazonaws.services.sqs.model.SendMessageRequest;

import com.amazonaws.util.EC2MetadataUtils;



public class ImageRecognition {



	private static final String REQUEST_QUEUE_NAME = "requestQueue";

	private static final String RESPONSE_QUEUE_NAME = "responseQueue";

	private static final String BUCKET_NAME = "image-result-bucket-unique";



	public static void main(String args[]) throws IOException {

		receiveFromRequestQ();

	}



	public static void receiveFromRequestQ() throws IOException {



		BasicAWSCredentials awsCreds = new BasicAWSCredentials(credentials); //credentials found in key file



		final AmazonSQS sqs = AmazonSQSClientBuilder.standard().withRegion("us-west-1")

				.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

		String queryUrl = sqs.getQueueUrl(REQUEST_QUEUE_NAME).getQueueUrl();

		final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()

				.withQueueUrl(sqs.getQueueUrl(REQUEST_QUEUE_NAME).getQueueUrl());

		receiveMessageRequest.setVisibilityTimeout(10);

		receiveMessageRequest.setWaitTimeSeconds(10);

		receiveMessageRequest.setMaxNumberOfMessages(10);



		while (true) {

			final List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();



			if (messages.isEmpty()) {

				String instanceId = EC2MetadataUtils.getInstanceId();

				terminateInstance(instanceId);

			} else {

				for (final Message message : messages) {

					executeBash(message.getBody());

					sqs.deleteMessage(queryUrl, message.getReceiptHandle());

					deleteIfExists(Paths.get("/home/ubuntu/output.txt"));

				}

			}

		}

	}



	public static void terminateInstance(String instanceId) {


		AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard()

				.withCredentials(new AWSStaticCredentialsProvider(

						new BasicAWSCredentials(credentials.accesskey, credentials.secret_key)))

				.build();

		//final AmazonEC2 ec2 = AmazonEC2ClientBuilder.standard().withCredentials(credentialsProvider);

		TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instanceId);

		ec2.terminateInstances(request);

	}



	public static void executeBash(String requestUrl) throws IOException {



		try {

			ProcessBuilder pb = new ProcessBuilder("/home/ubuntu/imagerec.sh", requestUrl);

			Process p = pb.start();

		} catch (IOException e) {

			e.printStackTrace();

		}

		pushToResponseQ();

	}



	public static void pushToResponseQ() throws IOException {



		String outputFilePath = "/home/ubuntu/output.txt";

		File f = new File(outputFilePath);



		while (!f.exists()) {

		}



		FileReader fileReader = new FileReader(outputFilePath);

		BufferedReader bufferReader = new BufferedReader(fileReader);

		String line = bufferReader.readLine();

		sendMessage(line);

		String[] a = line.split(",");

		putObjectToS3(BUCKET_NAME, a[0].substring(1, a[0].length()),

				line.substring(a[0].length() + 1, line.length() - 1));

		bufferReader.close();

	}



	public static void sendMessage(String input) {



		//final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		AmazonSQS sqs = AmazonSQSClientBuilder.standard()

				.withCredentials(new AWSStaticCredentialsProvider(

						new BasicAWSCredentials(credentials.access_key, credentials.secret_key)))

				.build();
		String queueurl = sqs.getQueueUrl(RESPONSE_QUEUE_NAME).getQueueUrl();

		SendMessageRequest send_msg_request = new SendMessageRequest().withQueueUrl(queueurl).withMessageBody(input)

				.withDelaySeconds(5);

		sqs.sendMessage(send_msg_request);

	}



	public static void putObjectToS3(String bucketName, String key, String webpage) {



		AmazonS3 s3client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_WEST_1)

				.withCredentials(new AWSStaticCredentialsProvider(

						new BasicAWSCredentials(credentials.access_key, credentials.secret_key)))

				.build();

		s3client.putObject(bucketName, key, webpage);

	}

}

