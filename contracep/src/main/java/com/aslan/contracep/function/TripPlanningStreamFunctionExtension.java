package com.aslan.contracep.function;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import com.aslan.contra.dto.common.*;
import com.aslan.contra.dto.ws.NearbyKnownPeople;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

/**
 * This extension calculates the number of friends who visited for a particular
 * place in a particular period. address
 */
public class TripPlanningStreamFunctionExtension extends StreamFunctionProcessor {

	private static final Logger LOGGER = Logger.getLogger(TripPlanningStreamFunctionExtension.class);
	private static final String USERNAME_PASSWORD = "neo4j:admin";
	public static final String USER_MODEL_URL = "http://localhost:7474/contra/person";
	double longitude = 0.0, latitude = 0.0;
	ArrayList<String> listOfFriends;int countOfFriends;

	/**
	 * The process method of the TripPlanningStreamFunctionExtension, used when
	 * more than one function parameters are provided
	 *
	 * @param data
	 *            the data values for the function parameters
	 * @return the data for additional output attributes introduced by the
	 *         function
	 */
	@Override
	protected Object[] process(Object[] data) {

		String bufferOutput;
		 listOfFriends = new ArrayList<String>();

		Calendar startPeriod = Calendar.getInstance();
		Calendar endPeriod = Calendar.getInstance();

		String userID = String.valueOf(data[0]);
		longitude = Double.valueOf(data[2].toString());
		latitude = Double.valueOf(data[3].toString());
		double distance = Double.valueOf(data[4].toString());
		long start_time = Long.valueOf(data[5].toString());
		long end_time = Long.valueOf(data[6].toString());

		startPeriod.setTimeInMillis(start_time);
		endPeriod.setTimeInMillis(end_time);

		int startYear = startPeriod.get(Calendar.YEAR);
		int endYear = endPeriod.get(Calendar.YEAR);

		for (int i = startYear - 1, j = endYear - 1; i >= startYear - 5 & j >= endYear - 5; i--, j--) {

			startPeriod.set(Calendar.YEAR, i);
			endPeriod.set(Calendar.YEAR, i);

			Time startTime = Time.valueOf(startPeriod.getTimeInMillis());
			Time endTime = Time.valueOf(endPeriod.getTimeInMillis());

			Interval interval = new Interval();
			interval.setStartTime(startTime);
			interval.setEndTime(endTime);

			NearbyKnownPeople nearbyKnownPeople = new NearbyKnownPeople();
			nearbyKnownPeople.setUserID(userID);
			nearbyKnownPeople.setLongitude(longitude);
			nearbyKnownPeople.setLatitude(latitude);
			nearbyKnownPeople.setDistance(distance);
			nearbyKnownPeople.setInterval(interval);

			HttpClient client = HttpClientBuilder.create().build();

			String encoding = Base64.getEncoder().encodeToString(USERNAME_PASSWORD.getBytes());

			// Create a POST method using the receiver URL.
			HttpPost method = new HttpPost(USER_MODEL_URL + "/nearby");

			method.addHeader("Authorization", "Basic " + encoding);
			method.addHeader("Content-Type", "application/json");
			method.addHeader("Accept", "application/json");

			LOGGER.info("executing request " + method.getRequestLine());

			// Create an entity and add it to the method.
			Gson gson = new Gson();
			StringEntity entity;
			try {
				entity = new StringEntity(gson.toJson(nearbyKnownPeople));
			} catch (UnsupportedEncodingException e) {
				throw new ExecutionPlanRuntimeException("Error in encoding", e);
			}
			method.setEntity(entity);

			HttpResponse response = null;
			try {
				// Execute the method and retrieve the response.
				response = client.execute(method);
			} catch (ClientProtocolException e) {
				throw new ExecutionPlanRuntimeException("Error in executing client protocol", e);
			} catch (IOException e) {
				throw new ExecutionPlanRuntimeException("Error in input of excute method", e);
			}
			BufferedReader br;
			String jsonResponse = "";

			try {
				br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
				while ((bufferOutput = br.readLine()) != null) {
					jsonResponse += bufferOutput;
				}
			} catch (IllegalStateException e) {
				throw new ExecutionPlanRuntimeException("Illegal state exception", e);

			} catch (IOException e) {
				throw new ExecutionPlanRuntimeException("Error in buffer reading.", e);
			}
			
			
			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObj = (JsonObject) jsonParser.parse(jsonResponse);

			for (int k = 0; k < jsonObj.getAsJsonArray("entity").size(); k++) {
				String friendID = jsonObj.getAsJsonArray("entity").get(k).toString();
				if (!listOfFriends.contains(friendID))
					listOfFriends.add(jsonObj.getAsJsonArray("entity").get(k).toString());
			}

		}
		 countOfFriends = listOfFriends.size();
		 
		LOGGER.info("UserID of Friends: " + listOfFriends.toString());
		LOGGER.info("Number of friends visited to " + "(" + longitude + ", " + latitude + ") :" + countOfFriends);
		// process(countOfFriends);
		
		return new Object[] { listOfFriends , countOfFriends};

	}

	/**
	 * The process method of TripPlanningStreamFunctionExtension, used when zero
	 * or one function parameter is provided
	 *
	 * @param data
	 * 
	 *            null if the function parameter count is zero or runtime data
	 *            value of the function parameter
	 * @return the data for additional output attribute introduced by the
	 *         function
	 */
	@Override
	protected Object[] process(Object data) {
		return new Object[] { listOfFriends , countOfFriends };
	}

	/**
	 * The init method of the GeocodeStreamFunctionProcessor, this method will
	 * be called before other methods
	 *
	 * @param inputDefinition
	 *            the incoming stream definition
	 * @param attributeExpressionExecutors
	 *            the executors of each function parameters
	 * @param executionPlanContext
	 *            the context of the execution plan
	 * @return the additional output attributes introduced by the function
	 */
	@Override
	protected List<Attribute> init(AbstractDefinition inputDefinition,
			ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
		if (attributeExpressionExecutors.length != 7) {
			throw new ExecutionPlanValidationException(
					"TripPlanningFunction must have exactly one string parameters, three double parameters and two long parameter");
		}

		if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
			throw new ExecutionPlanValidationException("First parameter: expected " + Attribute.Type.STRING + " found "
					+ attributeExpressionExecutors[0].getReturnType());
		}

		if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
			throw new ExecutionPlanValidationException("Second parameter: expected " + Attribute.Type.STRING + " found "
					+ attributeExpressionExecutors[1].getReturnType());
		}
		if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.DOUBLE) {
			throw new ExecutionPlanValidationException("Third parameter: expected " + Attribute.Type.DOUBLE + " found "
					+ attributeExpressionExecutors[2].getReturnType());
		}

		if (attributeExpressionExecutors[3].getReturnType() != Attribute.Type.DOUBLE) {
			throw new ExecutionPlanValidationException("Fourth parameter: expected " + Attribute.Type.DOUBLE + " found "
					+ attributeExpressionExecutors[3].getReturnType());
		}
		if (attributeExpressionExecutors[4].getReturnType() != Attribute.Type.DOUBLE) {
			throw new ExecutionPlanValidationException("Fifth parameter: expected " + Attribute.Type.DOUBLE + " found "
					+ attributeExpressionExecutors[4].getReturnType());
		}
		if (attributeExpressionExecutors[5].getReturnType() != Attribute.Type.LONG) {
			throw new ExecutionPlanValidationException("Fifth parameter: expected " + Attribute.Type.LONG + " found "
					+ attributeExpressionExecutors[5].getReturnType());
		}
		if (attributeExpressionExecutors[6].getReturnType() != Attribute.Type.LONG) {
			throw new ExecutionPlanValidationException("Sixth parameter: expected " + Attribute.Type.LONG + " found "
					+ attributeExpressionExecutors[6].getReturnType());
		}
		ArrayList<Attribute> attributes = new ArrayList<Attribute>(3);
		attributes.add(new Attribute("listOfFriends", Attribute.Type.STRING));
		attributes.add(new Attribute("countOfFriends", Attribute.Type.INT));

		return attributes;
	}

	@Override
	public void start() {
		// nothing to start
	}

	@Override
	public void stop() {
		// nothing to stop
	}

	/**
	 * Used to collect the serializable state of the processing element, that
	 * need to be persisted for the reconstructing the element to the same state
	 * on a different point of time
	 *
	 * @return stateful objects of the processing element as an array
	 */
	@Override
	public Object[] currentState() {
		return new Object[0];
	}

	/**
	 * Used to restore serialized state of the processing element, for
	 * reconstructing the element to the same state as if was on a previous
	 * point of time.
	 *
	 * @param state
	 *            the stateful objects of the element as an array on the same
	 *            order provided by currentState().
	 */
	@Override
	public void restoreState(Object[] state) {
		// Implement restore state logic.
	}
}