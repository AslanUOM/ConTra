package com.aslan.contracep.function;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.aslan.contracep.function.NaiveBayesClassifier.NaiveBayesClassifier;

/**
 * This extension identify whether the user is in a meeting or not
 */
public class MeetingIdentificationStreamFunctionExtension extends
		StreamFunctionProcessor {

	private static final Logger LOGGER = Logger
			.getLogger(TripPlanningStreamFunctionExtension.class);
	private static final String USERNAME_PASSWORD = "neo4j:root";
	public static final String USER_MODEL_URL = "http://localhost:7474/contra/person";

	boolean isUserInMeeting = false;

	/**
	 * @param data
	 *            the data values for the function parameters
	 * @return the data for additional output attributes introduced by the
	 *         function
	 */
	@Override
	protected Object[] process(Object[] data) {

		// get sensor data values
		double Light = Double.parseDouble(String.valueOf(data[0]));
		double SoundLevel = Double.parseDouble(String.valueOf(data[3]));
		double LinearAcceleration = Double.parseDouble(String.valueOf(data[4]));
		int uID = Integer.parseInt(String.valueOf(data[5]));

		// convert proximity boolean value to double value
		String proximity_string = String.valueOf(data[1]);
		Double proximity = 1.0;
		if (proximity_string.equals("true")) {
			proximity = 1000.0;
		} else {
			proximity = 1.0;
		}

		// convert internet boolean value to double value
		String internet_string = String.valueOf(data[2]);
		Double internet = 0.0;

		if (internet_string.equals("true")) {
			internet = 1.0;
		} else {
			internet = 0.0;
		}

		// boolean values
		boolean Light_M = false;
		boolean SoundLevel_M = false;
		boolean LinearAcceleration_M = false;

		// sensor value at meeting occasions
		if (100 < Light && Light < 300)
			Light_M = true;
		if (40 < SoundLevel && SoundLevel < 70)
			SoundLevel_M = true;
		if (0.0 <= LinearAcceleration && LinearAcceleration < 1)
			LinearAcceleration_M = true;

		// NaiveBayesClassifier object
		NaiveBayesClassifier obj = new NaiveBayesClassifier();

		// locations for training and test data set
		String trainingCsvFile = "C:/Users/User/Documents/Raveen/Softwares/fyp_new/MeetingIdentificationUsingNB/JavaApplication-server and client/BayesMeeting/resources/datasets/training/trainingData.csv";

		// test data set only takes 4 attribute values
		double[] dataValueArrayForTest = new double[4];
		dataValueArrayForTest[0] = Light;
		dataValueArrayForTest[1] = proximity;
		dataValueArrayForTest[2] = internet;
		dataValueArrayForTest[3] = SoundLevel;

		ArrayList<double[]> testDataset = new ArrayList<double[]>();
		testDataset.add(dataValueArrayForTest);

		ArrayList<double[]> trainingDataset = obj.loadCsv(trainingCsvFile);

		// prepare model
		Map<Double, ArrayList<double[]>> summaries = obj
				.summarizeByClass(trainingDataset);

		// test model
		ArrayList<String> predictions = obj.getPredictions(summaries,
				testDataset);

		String x = predictions.get(0);

		// boolean output value from the naiveBayes classifier
		int isInMeetingNB = Integer.parseInt(x);
		boolean isMeetingNB = false;

		if (isInMeetingNB == 1)
			isMeetingNB = true;

		// (combined naivebayes and logic process)
		if ((Light_M && SoundLevel_M && LinearAcceleration_M)
				|| (Light_M && SoundLevel_M && !LinearAcceleration_M && isMeetingNB)
				|| (Light_M && !SoundLevel_M && LinearAcceleration_M && isMeetingNB)
				|| (!Light_M && SoundLevel_M && LinearAcceleration_M && isMeetingNB))
			isUserInMeeting = true;

		LOGGER.info("Is user in a meeting ? : " + isUserInMeeting);

		return new Object[] { isUserInMeeting };

	}

	/**
	 * @param data
	 * 
	 *            null if the function parameter count is zero or runtime data
	 *            value of the function parameter
	 * @return the data for additional output attribute introduced by the
	 *         function
	 */
	@Override
	protected Object[] process(Object data) {
		// return new Object[] { listOfFriends , countOfFriends };
		return new Object[] { isUserInMeeting };
	}

	/**
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
			ExpressionExecutor[] attributeExpressionExecutors,
			ExecutionPlanContext executionPlanContext) {
		if (attributeExpressionExecutors.length != 6) {
			throw new ExecutionPlanValidationException(
					"MeetingIdentification must have three double parameters , 2 boolean parameters, and one integer parameter");
		}

		if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.DOUBLE) {
			throw new ExecutionPlanValidationException(
					"First parameter: expected " + Attribute.Type.DOUBLE
							+ " found "
							+ attributeExpressionExecutors[0].getReturnType());
		}

		if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.BOOL) {
			throw new ExecutionPlanValidationException(
					"Second parameter: expected " + Attribute.Type.BOOL
							+ " found "
							+ attributeExpressionExecutors[1].getReturnType());
		}
		if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.BOOL) {
			throw new ExecutionPlanValidationException(
					"Third parameter: expected " + Attribute.Type.BOOL
							+ " found "
							+ attributeExpressionExecutors[2].getReturnType());
		}

		if (attributeExpressionExecutors[3].getReturnType() != Attribute.Type.DOUBLE) {
			throw new ExecutionPlanValidationException(
					"Fourth parameter: expected " + Attribute.Type.DOUBLE
							+ " found "
							+ attributeExpressionExecutors[3].getReturnType());
		}
		if (attributeExpressionExecutors[4].getReturnType() != Attribute.Type.DOUBLE) {
			throw new ExecutionPlanValidationException(
					"Fifth parameter: expected " + Attribute.Type.DOUBLE
							+ " found "
							+ attributeExpressionExecutors[4].getReturnType());
		}
		if (attributeExpressionExecutors[5].getReturnType() != Attribute.Type.INT) {
			throw new ExecutionPlanValidationException(
					"Fifth parameter: expected " + Attribute.Type.INT
							+ " found "
							+ attributeExpressionExecutors[5].getReturnType());
		}

		ArrayList<Attribute> attributes = new ArrayList<Attribute>(5);
		attributes.add(new Attribute("isUserInMeeting", Attribute.Type.BOOL));

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