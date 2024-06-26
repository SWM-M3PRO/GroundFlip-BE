package com.m3pro.groundflip.config;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class GeometryConverter {
	private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

	public static org.locationtech.jts.geom.Point convertGeomToJts(Object geolattePoint) {
		Point<G2D> point = (Point<G2D>)geolattePoint;
		if (geolattePoint == null) {
			return null;
		}

		G2D position = point.getPosition();
		Coordinate coordinate = new Coordinate(position.getLon(), position.getLat());
		return geometryFactory.createPoint(coordinate);
	}
}
