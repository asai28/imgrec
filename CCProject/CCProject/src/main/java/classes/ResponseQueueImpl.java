package classes;

import java.util.List;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import interfaces.ResponseQueueServices;

public class ResponseQueueImpl implements ResponseQueueServices{

	public ResponseQueueImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void getQueueSize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void createQueue() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteQueue() {
		// TODO Auto-generated method stub

	}


	public ReceiveMessageRequest createMessageRequest(AmazonSQS sqs) {
		final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
				.withQueueUrl(sqs.getQueueUrl("responseQueue").getQueueUrl());
		receiveMessageRequest.setVisibilityTimeout(10);
		receiveMessageRequest.setWaitTimeSeconds(3);
		receiveMessageRequest.setMaxNumberOfMessages(10);
		return receiveMessageRequest;
	}

	@Override
	public List<Message> getMessagesFromQueue(AmazonSQS sqs, ReceiveMessageRequest receiveMessageRequest) {
		return sqs.receiveMessage(receiveMessageRequest).getMessages();
	}

}
