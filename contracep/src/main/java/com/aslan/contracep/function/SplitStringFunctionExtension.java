package com.aslan.contracep.function;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.Attribute.Type;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

public class SplitStringFunctionExtension extends FunctionExecutor {

	@Override
	public void start() {
		// Nothing to start
	}

	@Override
	public void stop() {
		// Nothing to stop
	}

	@Override
	public Object[] currentState() {
		// This function is stateless
		return null;
	}

	@Override
	public void restoreState(Object[] state) {
		// This function is stateless
	}

	@Override
	protected Object execute(Object[] data) {
		String source = String.valueOf(data[0]);
		String regex = String.valueOf(data[1]);
		int index = Integer.valueOf(String.valueOf(data[2]));

		String[] array = source.split(regex);
		if (index < 0 || index >= array.length) {
			throw new ArrayIndexOutOfBoundsException("Requested for " + index);
		}

		return array[index];
	}

	@Override
	protected Object execute(Object data) {
		// Since the split function takes 3 parameters, this method does not
		// get called. Hence, not implemented.
		return null;
	}

	@Override
	protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
		if (attributeExpressionExecutors.length != 3) {
			throw new ExecutionPlanValidationException(
					"Split must have exactly two string parameters and an integer parameter");
		}

		if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
			throw new ExecutionPlanValidationException("First parameter: expected " + Attribute.Type.STRING + " found "
					+ attributeExpressionExecutors[0].getReturnType());
		}

		if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.STRING) {
			throw new ExecutionPlanValidationException("Second parameter: expected " + Attribute.Type.STRING + " found "
					+ attributeExpressionExecutors[1].getReturnType());
		}

		if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.INT) {
			throw new ExecutionPlanValidationException("Third parameter: expected " + Attribute.Type.INT + " found "
					+ attributeExpressionExecutors[2].getReturnType());
		}
	}

	@Override
	public Type getReturnType() {
		return Attribute.Type.STRING;
	}

}
