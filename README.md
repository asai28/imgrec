# Image Recognition Using AWS

![Architecture of Image Recognition App](/Architecture.PNG)

### Architecture of the application

1. Web Tier:

  * The web tier is deployed on an EC2 instance as a JAVA Spring based WEB application with REST endpoints, for the user to provide URLs for Image Recognition.
  * The URL is sent to Amazon Simple Queuing Service (SQS) denoted as a requestQueue for reliable pickup of requests. 
  * The requests are processed by the app-tier instances running the Image Recognition model, and the results are stored in another Amazon Simple Queuing Service (SQS) denoted as a responseQueue.
  * To ensure that the output is mapped correctly to input URL, the input provided by the user is compared with the messages in the responseQueue and returned to the user.
  
2. Request Queue:

  * The requests are picked up from the web-tier where the “requestQueue” polls for 10 seconds for new messages containing the URLs.

3. Load Balancer:

  * The requests are picked up from the requestQueue and creates instances based on the number of requests such that no more than 20 instances are created.
  * The created instances pull in more requests from the requestQueue. If there are no more requests, the instance is terminated.
  * If again there is any new request, new instances are created.

4. App Tier:

  * The requests are picked up from the requestQueue by the App-instances for image recognition.
  * Each App-instance is launched from an Image Recognition AMI, which also has a jar and shell script deployed for processing.
  * The shell script is invoked on start of the App-Instance, and in turn this script calls the jar which consists of the image recognition logic.
  * The jar file on running, will fetch requests from the requestQueue, and call another script for using the image recognition model. The output from the image recognition model is fetched and inserted to the reponseQueue.
  * The app tier picks up if any more request are present, else it terminates all instances.
  
5. Response Queue:

* The responses are picked from the instances and stored in S3 bucket name "image-result-bucket" after comparing the input URL sent in web-tier with the URL in "responseQueue".

6. S3:

* The responses are picked from the output of tensorflow model and fed as (key, pair) values in S3 bucket named "image-result-bucket".
* The (key,pair) is passed to the web-tier and displayed as result for the corresponding client.

### Autoscaling

Autoscaling is implemented in web tier. Requests are polled from request queue and based on the number of requests, instances are created. We support upto 18 instances. Once instances are created they poll messages from request queue and once queue is empty, each instance terminates itself. For concurrent requests upto 18 instances are spawned, after which instances poll requests from the queue.
Once there are no more requests left in the requestQueue and if the instance is idle, the instance is terminated.

### Code

The code consists of two jar files WebTier.jar, ImageRecognition.jar and loadBalancer.jar and two bash scripts startapp.sh and imagerec.sh.

#### startapp.sh:

(i) This script invokes the ImageRecognition.jar which runs the tensorflow model. This file is invoked from the WebTier instance, placed in a location inside the instance such that it is executed during the boot of the instance.
(ii) startapp.sh is placed in /home/ubuntu/var/lib/cloud/scripts/per-boot folder and given full permission (777).

To run startapp.sh in an ec2 instance:
sudo chmod 777 startapp.sh
cd /home/ubuntu/var/lib/cloud/scripts/per-boot/startapp.sh

ImageRecognition.jar:
(i) This code polls for messages from Request queue and runs the tensorflow model. Output of tensorflow models are written to a file which is then pushed to the response Queue and S3 for storing.
(ii) ImageRecognition.jar is stored in /home/ubuntu and is given full permission (777). It contains the code for requestQueue to poll requests; executes imagerec.sh (tensorflow code); polls outputs of tensorflow model to responseQueue and pushes the key, value pairs to S3 bucket “image-result-bucket-unique”.

To run the jar in the ec2 instance:
sudo chmod 777 ImageRecognition.jar
java -jar ImageRecognition.jar

#### imagerec.sh:

(i) This involves the code to pass the request URL to the tensorflow model. It is invoked by ImageRecognition.jar every time there is a new web request. It takes the web URL as input and generates the top 1 prediction with a score. The result is the list of top 1 predictions without the score. This result is stored in output.txt.

(ii) To run the script in ec2 instance:
./imagerec.sh <URL name>

#### WebTier.jar :

This takes an input from the user and sends it to Request Queue loadBalancer.jar :

This jar contains code for load Balancing ,which will continuously poll for messages from the request
queue and based on the Number of messages in the queue ,it will create those many number of
instances for concurrent requests ,but for streaming data ,instances that are idle get terminated while
the rest continue to poll based on availability.This jar is placed in load Balancer instance which will
always be running.
