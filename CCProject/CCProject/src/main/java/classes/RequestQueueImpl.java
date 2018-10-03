package classes;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import interfaces.RequestQueueServices;

public class RequestQueueImpl implements RequestQueueServices{
	
	private static final String QUEUE_NAME ="requestQueue";
	
	public RequestQueueImpl() {
	}

	@Override
	public void getQueueSize() {
	}

	@Override
	public void createQueue() {
		
		final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		try {
			final Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("FifoQueue", "true");
			attributes.put("ContentBasedDeduplication", "true");
			attributes.put("MessageDeduplicationId", "true");

			final CreateQueueRequest createQueueRequest =
					new CreateQueueRequest(QUEUE_NAME)
					.withAttributes(attributes);
			// Comment /Remove this while submitting from here
			//myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

		}catch (final AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means " +
					"your request made it to Amazon SQS, but was " +
					"rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (final AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means " +
					"the client encountered a serious internal problem while " +
					"trying to communicate with Amazon SQS, such as not " +
					"being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}

	@Override
	public void deleteQueue() {
		// TODO Auto-generated method stub
	}

	@Override
	public void sendMessagesToQueue(String input) {
		final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		String queueurl=sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
		SendMessageRequest send_msg_request = new SendMessageRequest()
		        .withQueueUrl(queueurl)
		        .withMessageBody(input)
		        .withDelaySeconds(5);
		sqs.sendMessage(send_msg_request);
	}

}
