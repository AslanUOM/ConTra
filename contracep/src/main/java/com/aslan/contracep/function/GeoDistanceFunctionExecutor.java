package com.aslan.contracep.function;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/**
 * @author gobinath
 */
public class GeoDistanceFunctionExecutor extends FunctionExecutor {
    Attribute.Type returnType = Attribute.Type.DOUBLE;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {

    }

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 4) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to " +
                    "geo:distance() function, " +
                    "requires 4, but found " + attributeExpressionExecutors.length);
        }
    }

    @Override
    protected Object execute(Object[] data) {
        if (data[0] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:distance() " +
                    "function. First argument should be double");
        }
        if (data[1] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:distance() " +
                    "function. Second argument should be double");
        }
        if (data[2] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:distance() " +
                    "function. Third argument should be double");
        }
        if (data[3] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:distance() " +
                    "function. Fourth argument should be double");
        }

        double latitude = (Double) data[0];
        double longitude = (Double) data[1];
        double prevLatitude = (Double) data[2];
        double prevLongitude = (Double) data[3];

        int R = 6371000; // Radius of the earth in m
        latitude = latitude * (Math.PI / 180);
        prevLatitude = prevLatitude * (Math.PI / 180);
        longitude = longitude * (Math.PI / 180);
        prevLongitude = prevLongitude * (Math.PI / 180);
        double dlon = prevLongitude - longitude;
        double dlat = prevLatitude - latitude;
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(latitude) * Math.cos(prevLatitude) *
                Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    protected Object execute(Object data) {
        return null;
    }

    @Override
    public Attribute.Type getReturnType() {
        return returnType;
    }

}