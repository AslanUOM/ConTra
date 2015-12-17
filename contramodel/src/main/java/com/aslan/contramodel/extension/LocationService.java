package com.aslan.contramodel.extension;

import com.aslan.contramodel.entity.Country;
import com.aslan.contramodel.entity.Location;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.neo4j.helpers.collection.MapUtil.map;
import static com.aslan.contramodel.util.Utility.isNullOrEmpty;


/**
 * Created by gobinath on 12/16/15.
 */
public class LocationService extends Service {
    private final Logger LOGGER = LoggerFactory.getLogger(LocationService.class);

    public LocationService(GraphDatabaseService databaseService) {
        super(databaseService);
    }

    public void createCountry(Country country) {
        if (country == null) {
            return;
        }
        Long id = executeAndReturnID("MATCH (n:Country {code: {code}}) RETURN ID(n) as id", "code", country.getCode());
        if (id == null) {
            id = executeAndReturnID("CREATE (n:Country { code: {code}, name: {name}}) RETURN ID(n) as id", "name", country.getName(), "code", country.getCode());
            LOGGER.info("Country is created: " + id);
        }
    }

    public void createRegion(Location region) {
        if (region == null) {
            return;
        }
        Long id = executeAndReturnID("MATCH (c:Country {code: {country_code}})<-[:COUNTRY]-(r:Location {code: {region_code}}) RETURN ID(r) as id", "country_code", region.getRegionalCode(), "region_code", region.getCode());
        if (id == null) {
            id = executeAndReturnID("CREATE (n:Location { code: {code}, name: {name}, latitude: {latitude}, longitude: {longitude}}) RETURN ID(n) as id"
                    , "code", region.getCode(), "name", region.getName(), "latitude", region.getLatitude(), "longitude", region.getLongitude());
            databaseService.execute("MATCH (c:Country), (r:Location) WHERE c.code = {country_code} AND ID(r) = {region_id} CREATE (r)-[:COUNTRY]->(c)", map("country_code", region.getRegionalCode(), "region_id", id));
            LOGGER.info("Region is created: " + id);
        }
    }

    public void createLocation(Location location) {
        if (location == null) {
            return;
        }
        Long id = executeAndReturnID("MATCH (x:Location {code: {location_code}})-[:SUBURB]->(y:Location {code: {region_code}}) RETURN ID(x) as id", "region_code", location.getRegionalCode(), "location_code", location.getCode());
        if (id == null) {
            id = executeAndReturnID("CREATE (n:Location { code: {code}, name: {name}, latitude: {latitude}, longitude: {longitude}}) WITH n " +
                    "MATCH (u:Location {code: {region_code}}) CREATE (n)-[:SUBURB]->(u) RETURN ID(n) as id", "region_code", location.getRegionalCode(), "code", location.getCode(), "name", location.getName(), "latitude", location.getLatitude(), "longitude", location.getLongitude());
            LOGGER.info("Location is created: " + id);
        }
    }

}
