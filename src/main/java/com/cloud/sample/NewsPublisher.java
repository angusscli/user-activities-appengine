package com.cloud.sample;

import java.util.logging.Logger;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

public class NewsPublisher {
	private final static String PROJECT_ID = "techfest-hackathon-1";
	private final static String TOPIC_ID = "news-topic";

	private final static Logger log = Logger.getLogger(NewsPublisher.class.getName());

	public static void publish(String message) throws Exception {
		ProjectTopicName topicName = ProjectTopicName.of(PROJECT_ID, TOPIC_ID);

		Publisher publisher = null;
		try {
			// Create a publisher instance with default settings bound to the topic
			publisher = Publisher.newBuilder(topicName).build();

			// convert message to bytes
			ByteString data = ByteString.copyFromUtf8(message);
			PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

			// schedule a message to be published, messages are automatically batched
			ApiFuture<String> future = publisher.publish(pubsubMessage);

			// add an asynchronous callback to handle success / failure
			ApiFutures.addCallback(future, new ApiFutureCallback<String>() {

				@Override
				public void onFailure(Throwable throwable) {
					if (throwable instanceof ApiException) {
						ApiException apiException = ((ApiException) throwable);
						// details on the API exception
						log.severe(apiException.getStatusCode().getCode().name());
						//System.out.println(apiException.isRetryable());
					}
					log.severe("Error publishing message : " + message);
				}

				@Override
				public void onSuccess(String messageId) {
					// Once published, returns server-assigned message ids (unique within the topic)
					log.info(messageId);
				}
			});
		} finally {
			if (publisher != null) {
				// When finished with the publisher, shutdown to free up resources.
				publisher.shutdown();
			}
		}

	}

}
