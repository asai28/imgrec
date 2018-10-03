package interfaces;

import java.util.List;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public interface ResponseQueueServices extends QueueServices{

	List<Message> getMessagesFromQueue(AmazonSQS sqs, ReceiveMessageRequest receiveMessageRequest);
	
}
