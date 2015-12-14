package com.aslan.contracep.function;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.function.FunctionExecutor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

/**
 * Created by Vishnuvathsasarma on 10-Dec-15.
 */
public class GeoFenceFunctionExtension extends FunctionExecutor {

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515; //in miles
        dist = dist * 1.609344; //in kilometers
//            dist = dist * 0.8684; //in Nautical Miles

        return (dist);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts decimal degrees to radians						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::	This function converts radians to decimal degrees						 :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

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
        Double lat1 = Double.valueOf(String.valueOf(data[0]));
        Double lon1 = Double.valueOf(String.valueOf(data[1]));
        Double lat2 = Double.valueOf(String.valueOf(data[2]));
        Double lon2 = Double.valueOf(String.valueOf(data[3]));
        Double radius = Double.valueOf(String.valueOf(data[4]));

        return radius >= distance(lat1, lon1, lat2, lon2);
    }

    @Override
    protected Object execute(Object data) {
        // Since the split function takes 3 parameters, this method does not
        // get called. Hence, not implemented.
        return null;
    }

    @Override
    protected void init(ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors.length != 5) {
            throw new ExecutionPlanValidationException(
                    "LatLng of 2 location and radius in km must be provided");
        }

        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.DOUBLE) {
            throw new ExecutionPlanValidationException("First parameter: expected " + Attribute.Type.DOUBLE + " found "
                    + attributeExpressionExecutors[0].getReturnType());
        }

        if (attributeExpressionExecutors[1].getReturnType() != Attribute.Type.DOUBLE) {
            throw new ExecutionPlanValidationException("Second parameter: expected " + Attribute.Type.DOUBLE + " found "
                    + attributeExpressionExecutors[1].getReturnType());
        }

        if (attributeExpressionExecutors[2].getReturnType() != Attribute.Type.DOUBLE) {
            throw new ExecutionPlanValidationException("Third parameter: expected " + Attribute.Type.DOUBLE + " found "
                    + attributeExpressionExecutors[2].getReturnType());
        }

        if (attributeExpressionExecutors[3].getReturnType() != Attribute.Type.DOUBLE) {
            throw new ExecutionPlanValidationException("Fourth parameter: expected " + Attribute.Type.DOUBLE + " found "
                    + attributeExpressionExecutors[2].getReturnType());
        }

        if (attributeExpressionExecutors[4].getReturnType() != Attribute.Type.DOUBLE) {
            throw new ExecutionPlanValidationException("Fifth parameter: expected " + Attribute.Type.DOUBLE + " found "
                    + attributeExpressionExecutors[2].getReturnType());
        }
    }

    @Override
    public Attribute.Type getReturnType() {
        return Attribute.Type.BOOL;
    }
}

/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
/*::                                                                         :*/
/*::  This routine calculates the distance between two points (given the     :*/
/*::  latitude/longitude of those points). It is being used to calculate     :*/
/*::  the distance between two locations using GeoDataSource (TM) prodducts  :*/
/*::                                                                         :*/
/*::  Definitions:                                                           :*/
/*::    South latitudes are negative, east longitudes are positive           :*/
/*::                                                                         :*/
/*::  Passed to function:                                                    :*/
/*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
/*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
/*::    unit = the unit you desire for results                               :*/
/*::           where: 'M' is statute miles (default)                         :*/
/*::                  'K' is kilometers                                      :*/
/*::                  'N' is nautical miles                                  :*/
/*::  Worldwide cities and other features databases with latitude longitude  :*/
/*::  are available at http://www.geodatasource.com                          :*/
/*::                                                                         :*/
/*::  For enquiries, please contact sales@geodatasource.com                  :*/
/*::                                                                         :*/
/*::  Official Web site: http://www.geodatasource.com                        :*/
/*::                                                                         :*/
/*::           GeoDataSource.com (C) All Rights Reserved 2015                :*/
/*::                                                                         :*/
/*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/