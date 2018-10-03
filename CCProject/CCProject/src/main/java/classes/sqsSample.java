package classes;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class sqsSample{
	private static final String QUEUE_NAME ="Fifoqueuetrial1.fifo";
	private static String myQueueUrl;
	private static List<String> urls =new ArrayList<String>();
	public static void createQueue()
	{
		final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

        try {
            // Create a FIFO queue
            System.out.println("Creating a new Amazon SQS FIFO queue called " +
                    "MyFifoQueue.fifo.\n");
            final Map<String, String> attributes = new HashMap<String, String>();
            attributes.put("FifoQueue", "true");
            //attributes.put("ContentBasedDeduplication", "true");
           //attributes.put("MessageDeduplicationId", "true");

          
            final CreateQueueRequest createQueueRequest =
                    new CreateQueueRequest(QUEUE_NAME)
                            .withAttributes(attributes);
            // Comment /Remove this while submitting from here
          myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
           
            System.out.println("Listing all queues in your account.\n");
            for (final String queueUrl : sqs.listQueues().getQueueUrls()) {
                System.out.println("  QueueUrl: " + queueUrl);
            }
            
            
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
	public static void createbatches(String inputs) throws InterruptedException
	{
		final AmazonSQSAsync sqsAsync = new AmazonSQSAsyncClient();
		final AmazonSQSAsync bufferedSqs = new AmazonSQSBufferedAsyncClient(sqsAsync);
		final CreateQueueRequest createRequest = new CreateQueueRequest().withQueueName("Buffers1");
		final CreateQueueResult res = bufferedSqs.createQueue(createRequest);
		final SendMessageRequest request = new SendMessageRequest();
		final String body = inputs;
		System.out.println(inputs);
		request.setMessageBody( body );
		request.setQueueUrl(res.getQueueUrl());
		//System.out.println(createRequest.getQueueName());
		 System.out.println(urls.size());
		bufferedSqs.sendMessageAsync(request);
		final ReceiveMessageRequest receiveMessageRequest =
                new ReceiveMessageRequest().withQueueUrl(res.getQueueUrl());
		receiveMessageRequest.setVisibilityTimeout(30);
		receiveMessageRequest.setWaitTimeSeconds(3);
		final List<Message> messages = sqsAsync.receiveMessage(receiveMessageRequest)
                .getMessages();
		
       for (final Message message : messages) {
          
           System.out.println("  Body: " + message.getBody());
             urls.add(message.getBody());
             if(urls.size()==10) {
            	 System.out.println(urls);
            	 sendmsg(urls);
            	 urls.clear();
             }
            else if(receiveMessageRequest.getWaitTimeSeconds()==3)
             {
            	 sendmsg(urls);
            	 urls.clear();
             }
       }
       System.out.println("Deleting the message.\n");
      // final String messageReceiptHandle = messages.get(0).getReceiptHandle();
       //sqsAsync.deleteMessage(new DeleteMessageRequest(sqsAsync.getQueueUrl("BuffersQueues").getQueueUrl(),messageReceiptHandle));
		
	}
	public static void sendmsg(List<String> inputs) throws InterruptedException
	{
		final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		final AmazonSQSAsync sqsAsync = new AmazonSQSAsyncClient();
		System.out.println(sqs.getQueueUrl(QUEUE_NAME));
		String queueUrl=sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
		System.out.println(inputs);
		List<SendMessageBatchRequestEntry> entries=new ArrayList<SendMessageBatchRequestEntry>();
		Integer i=0;
		SendMessageBatchRequestEntry other = new SendMessageBatchRequestEntry();
		
		while(i<inputs.size())
		{  
			Long ID=System.currentTimeMillis();
		    other.setId(ID.toString());
			other.setMessageBody(inputs.get(i));
			String messagededuplicationId=ID.toString()+i;
			String groupid="messageGroup"+ID.toString();
			other.setMessageGroupId(groupid);
			other.setMessageDeduplicationId(i.toString());
	
			entries.add(new SendMessageBatchRequestEntry(ID.toString(), inputs.get(i)).withMessageGroupId(groupid).withMessageDeduplicationId(messagededuplicationId));
			i=i+1;
			send_batch(sqsAsync,entries);
		}
	}
	public static void send_batch(AmazonSQSAsync sqsAsync,List<SendMessageBatchRequestEntry> entries)
	{
		final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		SendMessageBatchRequest send_msg = new SendMessageBatchRequest(sqs.getQueueUrl(QUEUE_NAME).getQueueUrl());
        send_msg.setEntries(entries);
        //((ReceiveMessageRequest) send_msg).withWaitTimeSeconds(20);
        // long timeBeforePost = System.currentTimeMillis();
        
		SendMessageBatchResult smbResult = sqsAsync.sendMessageBatch(send_msg);
	
		System.out.println(smbResult.getSuccessful());
	}
	public static void receive_msg()
	{
		final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		final ReceiveMessageRequest receiveMessageRequest =
                new ReceiveMessageRequest().withQueueUrl(sqs.getQueueUrl(QUEUE_NAME).getQueueUrl());
		receiveMessageRequest.setWaitTimeSeconds(10);
		receiveMessageRequest.setMaxNumberOfMessages(10);
		receiveMessageRequest.setReceiveRequestAttemptId("1");
		String queueUrl=sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		//System.out.println(messages.size());
		
       for (final Message message : messages) {
    	   System.out.println(message.getBody());
          
       }
	}
		
	public static void createinstance() {
		final AmazonEC2 ec2= AmazonEC2ClientBuilder.defaultClient();
		System.out .println("create an instance");
		
	//Choose which operating system we want to choose
		String imageId="ami-8f78c2f7"; //Choosing image id of the EC2 instance
		int minInstanceCount=1; //create only one instance
		int maxinstanceCount=1;
		
		RunInstancesRequest rir=new RunInstancesRequest(imageId,minInstanceCount,maxinstanceCount);
		rir.setInstanceType("t2.micro");
	
		
		//Region usWest2=Region.getRegion(Regions.US_WEST_2);
		//ec2.setRegion(usWest2);
		
		RunInstancesResult result= ec2.runInstances(rir);
		
		List<Instance> resultInstance= result.getReservation().getInstances();
		
		for(Instance ins: resultInstance) {
			System.out.println("New instance has been created:" + ins.getInstanceId()); //print the instance ID
		}
		
		}
	public static void createSnapShots()
	{
		
		final AmazonEC2 ec2= AmazonEC2ClientBuilder.defaultClient();
		String instanceId="i-0810c69f5c0058cdf";
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest().withInstanceIds(instanceId);
		DescribeInstancesResult describeInstancesResult = ec2.describeInstances(describeInstancesRequest);
		Instance instances = describeInstancesResult.getReservations().get(0).getInstances().get(0);

		// Then get the mappings
		String volumeId = null;
		List<InstanceBlockDeviceMapping> mappingList = instances.getBlockDeviceMappings();
		for(InstanceBlockDeviceMapping mapping: mappingList) {
		   volumeId=mapping.getEbs().getVolumeId().toString();
		   System.out.println(volumeId);
		}
		CreateSnapshotRequest cr=new CreateSnapshotRequest().withVolumeId(volumeId);
		CreateSnapshotResult result=ec2.createSnapshot(cr);
		System.out.println(result.getSnapshot().getSnapshotId().toString());
		
		//creating volume
		CreateVolumeRequest req = new CreateVolumeRequest()
				.withAvailabilityZone("us-west-2a")
	           .withSnapshotId("snap-00be57cbb74240535")
	            .withSize(1);
	    CreateVolumeResult res = ec2.createVolume(req);
	    
		
	}
	public static void getInstanceDetails()
	{
		final AmazonEC2 ec2= AmazonEC2ClientBuilder.defaultClient();
		HashMap<String,String> map=new HashMap<String,String>();
		List<String> Snapshots=new ArrayList<String>();
		//getting all instances
		List<String> Instance_ID=new ArrayList<String>();
		List<String> status=new ArrayList<String>();
		boolean done = false;

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		while(!done) {
		    DescribeInstancesResult response = ec2.describeInstances(request);

		    for(Reservation reservation : response.getReservations()) {
		        for(Instance instance : reservation.getInstances()) {
		            System.out.printf(
		                "Found instance with id %s, " +
		                "AMI %s, " +
		                "type %s, " +
		                "state %s " +
		                "IP %s"+
		                "and monitoring state %s",
		                "Volume ID %s",
		                instance.getInstanceId(),
		                instance.getImageId(),
		                instance.getInstanceType(),
		                instance.getState().getName(),
		                instance.getPublicIpAddress(),
		                instance.getMonitoring().getState());
		                map.put(instance.getInstanceId().toString(),instance.getState().toString());
		                
		        }
		        
		    }
		    request.setNextToken(response.getNextToken());

		    if(response.getNextToken() == null) {
		        done = true;
		}
		System.out.println(status);
		System.out.println(Instance_ID);
		
		}
		  
		    }
		
	   
	/*private static Boolean waitForSnapshotAvailable(String snapshotId) throws InterruptedException {
        Boolean snapshotAvailable = false;
        System.out.println("Wating for snapshot to become available.");
        while (!snapshotAvailable) {
            DescribeClusterSnapshotsResult result = client.describeClusterSnapshots(new DescribeClusterSnapshotsR equest()
                .withSnapshotIdentifier(snapshotId));
            String status = (result.getSnapshots()).get(0).getStatus();
            if (status.equalsIgnoreCase("available")) {
                snapshotAvailable = true;
            }
            else {
                System.out.print(".");
                Thread.sleep(sleepTime*1000);
            }
        }
        return snapshotAvailable;
    }

}*/
//	public static void main(String args[]) throws InterruptedException
//	{
//		createQueue();
////		String[] inputs= {"111","112","113","114"," 115"," 116"," 117"," 118"," 119"," 111"," 111"," 151"," 161"," 171"};
////		
////		//List<String> li=new ArrayList<String>();
////		//createSnapShots();
////		for(int i=0;i<inputs.length;i++)
////		createbatches(inputs[i]);
////		//receive_msg();
//		
//		
//	}

}
