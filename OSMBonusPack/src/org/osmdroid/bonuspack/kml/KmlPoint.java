package org.osmdroid.bonuspack.kml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * KML and/or GeoJSON Point
 * @author M.Kergall
 */
public class KmlPoint extends KmlGeometry implements Parcelable, Cloneable {

	public KmlPoint(){
		super();
	}
	
	public KmlPoint(GeoPoint position){
		this();
		setPosition(position);
	}
	
	/** GeoJSON constructor */
	public KmlPoint(JSONObject json){
		this();
		JSONArray coordinates = json.optJSONArray("coordinates");
		if (coordinates != null){
			setPosition(KmlGeometry.parseGeoJSONPosition(coordinates));
		}
	}
	
	public void setPosition(GeoPoint position){
		if (mCoordinates == null){
			mCoordinates = new ArrayList<GeoPoint>(1);
			mCoordinates.add(position);
		} else
			mCoordinates.set(0, position);
	}
	
	public GeoPoint getPosition(){
		return mCoordinates.get(0);
	}
	
	/** default listener for dragging a Marker built from a KML Point */
	public class OnKMLMarkerDragListener implements OnMarkerDragListener {
		@Override public void onMarkerDrag(Marker marker) {}
		@Override public void onMarkerDragEnd(Marker marker) {
			Object object = marker.getRelatedObject();
			if (object != null && object instanceof KmlPoint){
				KmlPoint point = (KmlPoint)object;
				point.setPosition(marker.getPosition());
			}
		}
		@Override public void onMarkerDragStart(Marker marker) {}		
	}
	
	/** Build the corresponding Marker overlay */	
	@Override public Overlay buildOverlay(MapView map, Style defaultStyle, KmlPlacemark kmlPlacemark, 
			KmlDocument kmlDocument, boolean supportVisibility){
		Context context = map.getContext();
		Marker marker = new Marker(map);
		marker.setTitle(kmlPlacemark.mName);
		marker.setSnippet(kmlPlacemark.mDescription);
		marker.setPosition(getPosition());
		Style style = kmlDocument.getStyle(kmlPlacemark.mStyle);
		if (style != null && style.mIconStyle != null){
			style.mIconStyle.styleMarker(marker, context);
		} else if (defaultStyle!=null && defaultStyle.mIconStyle!=null){
			defaultStyle.mIconStyle.styleMarker(marker, context);
		}
		//keep the link from the marker to the KML feature:
		marker.setRelatedObject(this);
		//allow marker drag, acting on KML Point:
		marker.setDraggable(true);
		marker.setOnMarkerDragListener(new OnKMLMarkerDragListener());
		if (supportVisibility && !kmlPlacemark.mVisibility)
			marker.setEnabled(kmlPlacemark.mVisibility);
		return marker;
	}
	
	@Override public void saveAsKML(Writer writer){
		try {
			writer.write("<Point>\n");
			writeKMLCoordinates(writer, mCoordinates);
			writer.write("</Point>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override public JSONObject asGeoJSON(){
		try {
			JSONObject json = new JSONObject();
			json.put("type", "Point");
			json.put("coordinates", KmlGeometry.geoJSONPosition(mCoordinates.get(0)));
			return json;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	//Cloneable implementation ------------------------------------
	
	@Override public KmlPoint clone(){
		KmlPoint kmlPoint = (KmlPoint)super.clone();
		return kmlPoint;
	}
	
	//Parcelable implementation ------------
	
	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
	}
	
	public static final Parcelable.Creator<KmlPoint> CREATOR = new Parcelable.Creator<KmlPoint>() {
		@Override public KmlPoint createFromParcel(Parcel source) {
			return new KmlPoint(source);
		}
		@Override public KmlPoint[] newArray(int size) {
			return new KmlPoint[size];
		}
	};
	
	public KmlPoint(Parcel in){
		super(in);
	}
}