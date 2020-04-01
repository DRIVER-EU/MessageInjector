package eu.driver.adaptor.controller;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import eu.driver.adapter.constants.TopicConstants;
import eu.driver.adapter.core.CISAdapter;
import eu.driver.adapter.excpetion.CommunicationException;
import eu.driver.adaptor.callback.AdapterCallback;
import eu.driver.adaptor.mapper.cap.XMLToAVROMapper;
import eu.driver.model.core.DataType;
import eu.driver.model.core.LargeDataUpdate;
import eu.driver.model.core.LayerType;
import eu.driver.model.core.Level;
import eu.driver.model.core.Log;
import eu.driver.model.core.MapLayerUpdate;
import eu.driver.model.core.PhaseMessage;
import eu.driver.model.core.RequestChangeOfTrialStage;
import eu.driver.model.core.RolePlayerMessage;
import eu.driver.model.core.UpdateType;
import eu.driver.model.geojson.Feature;
import eu.driver.model.geojson.FeatureCollection;
import eu.driver.model.geojson.FeatureCollectionType;
import eu.driver.model.geojson.FeatureType;
import eu.driver.model.geojson.GeoJSONEnvelope;
import eu.driver.model.geojson.MultiPolygon;
import eu.driver.model.geojson.MultiPolygonType;
import eu.driver.model.geojson.Polygon;
import eu.driver.model.geojson.PolygonType;
import eu.driver.model.sim.config.SessionManagement;
import eu.driver.model.sim.config.SessionState;
import eu.driver.model.tm.Phase;
import eu.driver.model.tm.Type;

@RestController
public class SendRestController implements
		ResourceProcessor<RepositoryLinksResource> {

	private Logger log = Logger.getLogger(this.getClass());
	private XMLToAVROMapper avroMapper = new XMLToAVROMapper();
	private CISAdapter adapter = CISAdapter.getInstance();
	
	private Map<String, String> registeredTopics = new HashMap<String, String>();

	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resource) {
		resource.add(ControllerLinkBuilder.linkTo(
				ControllerLinkBuilder.methodOn(SendRestController.class)
						.sendXMLMessage("CAP", "defaultCGOR", "XML")).withRel(
				"sendXMLMessage"));
		return resource;
	}

	public SendRestController() {
	}

	@ApiOperation(value = "sendXMLMessage", nickname = "sendXMLMessage")
	@RequestMapping(value = "/MIRestAdaptor/sendXMLMessage/{type}", method = RequestMethod.POST, consumes = { "application/xml" })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "type", value = "the type of the xml content", required = true, dataType = "string", paramType = "path", allowableValues = "CAP, MLP, GEOJSON, EMSI"),
			@ApiImplicitParam(name = "cgorName", value = "name of the cgor, if not provided, default public distribution group is used", required = false, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "xmlMsg", value = "the XML message as string", required = true, dataType = "string", paramType = "body", example = "<Alert></Alert>") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Response.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Response.class),
			@ApiResponse(code = 500, message = "Failure", response = Response.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Response> sendXMLMessage(@PathVariable String type,
			@QueryParam("cgorName") String cgorName, @RequestBody String xmlMsg) {
		log.info("--> sendXMLMessage");
		log.debug(xmlMsg);

		Response response = new Response();
		GenericRecord avroRecord = null;
		// check message type
		if (type.equalsIgnoreCase("CAP")) {
			log.info("Processing CAP message.");
			boolean valid = true;//avroMapper.validateCAP(xmlMsg);
			if (valid) {
				avroRecord = avroMapper.convertCapToAvro(xmlMsg);	
			} else {
				response.setMessage("CAP message not valid!");
				return new ResponseEntity<Response>(response,
						HttpStatus.BAD_REQUEST);
			}
		} else if (type.equalsIgnoreCase("MLP")) {
			log.info("Processing MLP message.");
			avroRecord = avroMapper.convertMlpToAvro(xmlMsg);

		} else if (type.equalsIgnoreCase("GEOJSON")) {
			log.info("Processing GEOJSON message.");
			avroRecord = avroMapper.convertGeoJsonToAvro(xmlMsg);

		} else if (type.equalsIgnoreCase("EMSI")) {
			log.info("Processing EMSI message.");
			avroRecord = avroMapper.convertEMSIToAvro(xmlMsg);
		}

		if (avroRecord != null) {
			try {
				log.info(avroRecord.toString());
				if (cgorName == null) {
					log.info("Send Message to default topic!");
					adapter.sendMessage(avroRecord);	
				} else {
					log.info("Send Message to topic: " + cgorName);
					adapter.sendMessage(avroRecord, cgorName);
				}
				response.setMessage("The message was send successfully!");
			} catch (CommunicationException cEx) {
				log.error("Error sending the record!", cEx);
				response.setMessage("Error sending the record!");
				response.setDetails(cEx.getMessage());
				return new ResponseEntity<Response>(response,
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			response.setMessage("Unknown message type!");
			return new ResponseEntity<Response>(response,
					HttpStatus.BAD_REQUEST);
		}

		log.info("sendXMLMessage -->");
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	@ApiOperation(value = "sendLogMsg", nickname = "sendLogMsg")
	@RequestMapping(value = "/MIRestAdaptor/sendLogMsg", method = RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "level", value = "level of the log record", required = true, dataType = "string", paramType = "query", allowableValues = "DEBUG, INFO, WARN, ERROR, CRITICAL, SILLY"),
			@ApiImplicitParam(name = "message", value = "level of the log record", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Boolean> sendLogMsg(
			@QueryParam("level") String level, @RequestBody String message) {
		log.info("--> sendLogMsg");
		Boolean send = true;

		Log logMsg = new Log();
		logMsg.setDateTimeSent(new Date().getTime());
		logMsg.setId(adapter.getClientID());
		logMsg.setLevel(Level.valueOf(level));
		logMsg.setLog(message);

		try {
			adapter.addLogEntry(logMsg);
		} catch (CommunicationException e) {
			log.error("Error sending the log request!");
		}

		log.info("sendLogMsg -->");
		return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendEvalLogMsg", nickname = "sendEvalLogMsg")
	@RequestMapping(value = "/MIRestAdaptor/sendEvalLogMsg", method = RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "level", value = "level of the log record", required = true, dataType = "string", paramType = "query", allowableValues = "DEBUG, INFO, WARN, ERROR, CRITICAL, SILLY"),
			@ApiImplicitParam(name = "message", value = "level of the log record", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Boolean> sendEvalLogMsg(
			@QueryParam("level") String level, @RequestBody String message) {
		log.info("--> sendEvalLogMsg");
		Boolean send = true;

		Log logMsg = new Log();
		logMsg.setDateTimeSent(new Date().getTime());
		logMsg.setId(adapter.getClientID());
		logMsg.setLevel(Level.valueOf(level));
		logMsg.setLog(message);

		try {
			adapter.addEvaluationLogEntry(logMsg);
		} catch (CommunicationException e) {
			log.error("Error sending the evaluation log request!");
		}

		log.info("sendEvalLogMsg -->");
		return new ResponseEntity<Boolean>(send, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendPhaseMessage", nickname = "sendPhaseMessage")
	@RequestMapping(value = "/MIRestAdaptor/sendPhaseMessage", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "msgBody", value = "the message as string", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> sendPhaseMessage(@RequestBody String msgBody) {
		log.info("--> sendPhaseMessage");
		
		try {
			JSONObject msgJson = new JSONObject(msgBody);
			
			PhaseMessage msg = new PhaseMessage();
			msg.setId(msgJson.getString("phaseId"));
			msg.setPhase(Phase.valueOf(msgJson.getString("phase")));
			msg.setIsStarting(msgJson.getBoolean("isStarting"));
			msg.setAlternativeName(msgJson.getString("altName"));
			
			this.adapter.sendMessage(msg, TopicConstants.PHASE_MESSAGE_TOPIC);
		} catch (Exception e) {
			log.error("Error sending the PhaseMessage!", e);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendPhaseMessage -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendRolePlayerMessage", nickname = "sendRolePlayerMessage")
	@RequestMapping(value = "/MIRestAdaptor/sendRolePlayerMessage", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "msgBody", value = "the message as string", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> sendRolePlayerMessage(@RequestBody String msgBody) {
		log.info("--> sendRolePlayerMessage");
		
		try {
			JSONObject msgJson = new JSONObject(msgBody);
			
			RolePlayerMessage msg = new RolePlayerMessage();
			msg.setId(msgJson.getString("msgId"));
			msg.setType(Type.valueOf(msgJson.getString("type")));
			msg.setTitle(msgJson.getString("title"));
			msg.setHeadline(msgJson.getString("headline"));
			msg.setDescription(msgJson.getString("description"));
			msg.setRolePlayerName(msgJson.getString("rolePlayerName"));
			//msg.setParticipantNames(msgJson.getString(name));
			msg.setComment(msgJson.getString("comment"));

			
			this.adapter.sendMessage(msg, TopicConstants.ROLE_PLAYER_TOPIC);
		} catch (Exception e) {
			log.error("Error sending the RolePlayerMessage!", e);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendRolePlayerMessage -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendSessionMessage", nickname = "sendSessionMessage")
	@RequestMapping(value = "/MIRestAdaptor/sendSessionMessage", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "msgBody", value = "the message as string", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> sendSessionMessage(@RequestBody String msgBody) {
		log.info("--> sendSessionMessage");
		
		try {
			JSONObject msgJson = new JSONObject(msgBody);
			
			SessionManagement msg = new SessionManagement();
			msg.setId(msgJson.getString("sessionId"));
			msg.setName(msgJson.getString("sessionName"));
			msg.setState(SessionState.valueOf(msgJson.getString("state")));
			Map<CharSequence, CharSequence> tags = new HashMap<CharSequence, CharSequence>();
			tags.put("trialId", msgJson.getString("trialId"));
			tags.put("trialName", msgJson.getString("trialName"));
			tags.put("scenarioId", msgJson.getString("scenarioId"));
			tags.put("scenarioName", msgJson.getString("scenarioName"));
			
			msg.setTags(tags);
			
			
			this.adapter.sendMessage(msg, TopicConstants.SESSION_MGMT_TOPIC);
		} catch (Exception e) {
			log.error("Error sending the SessionMessage!", e);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendSessionMessage -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendOstStageChangeMessage", nickname = "sendOstStageChangeMessage")
	@RequestMapping(value = "/MIRestAdaptor/sendOstStageChangeMessage", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "msgBody", value = "the message as string", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> sendOstStageChangeMessage(@RequestBody String msgBody) {
		log.info("--> sendOstStageChangeMessage");
		
		try {
			JSONObject msgJson = new JSONObject(msgBody);
			
			RequestChangeOfTrialStage msg = new RequestChangeOfTrialStage();
			msg.setOstTrialId(Integer.parseInt(msgJson.getString("trialId")));
			msg.setOstTrialSessionId(Integer.parseInt(msgJson.getString("sessionId")));
			msg.setOstTrialStageId(Integer.parseInt(msgJson.getString("stageId")));
			
			this.adapter.sendMessage(msg, TopicConstants.TRIAL_STATE_CHANGE_TOPIC);
		} catch (Exception e) {
			log.error("Error sending the OST Stage Change Message!", e);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendOstStageChangeMessage -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	@ApiOperation(value = "sendGeoJson", nickname = "sendGeoJson")
	@RequestMapping(value = "/MIRestAdaptor/sendGeoJson", method = RequestMethod.POST)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "cgorName", value = "name of the cgor, if not provided, default public distribution group is used", required = false, dataType = "string", paramType = "query"),
		@ApiImplicitParam(name = "requestJson", value = "the XML message as string", required = true, dataType = "string", paramType = "body") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	@Produces({ "application/json" })
	public ResponseEntity<Boolean> sendGeoJson(@QueryParam("cgorName") String cgorName, @RequestBody String requestJson ) {
		System.out.println(requestJson);
		log.info("--> sendGeoJson");

		try {
			JSONObject geoJson = new JSONObject(requestJson);
			
			FeatureCollection featureCollection = new FeatureCollection();
			featureCollection.setType(FeatureCollectionType.FeatureCollection);
			if (geoJson.isNull("bbox")) {
				featureCollection.setBbox(null);
			} else {
				JSONArray bboxList = geoJson.getJSONArray("bbox");
				List<Double> bboxDoubleList = new ArrayList<Double>();
				for (int i = 0; i < bboxList.length(); i++) {
					bboxDoubleList.add(Double.parseDouble(bboxList.getString(i)));
				}
				featureCollection.setBbox(bboxDoubleList);
			}
			
			// features
			JSONArray featureList = geoJson.getJSONArray("features");
			List<Feature> listOfFeatures = new ArrayList<Feature>();
			
			int count = featureList.length();
			for (int i = 0; i < count; i++) {
				Feature listFeature = new Feature();
				JSONObject feature = featureList.getJSONObject(i);
				listFeature.setType(FeatureType.Feature);
				
				// geometry
				JSONObject geometryObj = feature.getJSONObject("geometry");
				if (geometryObj.getString("type").equalsIgnoreCase("Polygon")) {
					Polygon polygon = new Polygon();
					polygon.setType(PolygonType.Polygon);
					List<List<List<Double>>> polygonList = new ArrayList<List<List<Double>>>();
					List<List<Double>> coordList = new ArrayList<>();
					
					JSONArray coordObj = geometryObj.getJSONArray("coordinates");
					JSONArray coordObjArray = coordObj.getJSONArray(0);
					
					for (int a = 0; a < coordObjArray.length(); a++) {
						JSONArray jsonCoord = coordObjArray.getJSONArray(0);
						List<Double> featCoord = new ArrayList<>();
						featCoord.add(jsonCoord.getDouble(0));
						featCoord.add(jsonCoord.getDouble(1));
						coordList.add(featCoord);
					}
					polygonList.add(coordList);
					
					polygon.setCoordinates(polygonList);
					
					listFeature.setGeometry(polygon);
				} else if (geometryObj.getString("type").equalsIgnoreCase("MultiPolygon")) {
					MultiPolygon multiPolygon = new MultiPolygon();
					multiPolygon.setType(MultiPolygonType.MultiPolygon);
					List<List<List<List<Double>>>> multiPolygonList = new ArrayList<List<List<List<Double>>>>();
					
					List<List<List<Double>>> polygonList = new ArrayList<List<List<Double>>>();
					List<List<Double>> coordList = new ArrayList<>();
					
					JSONArray coordObj = geometryObj.getJSONArray("coordinates");
					JSONArray coordObjArray = coordObj.getJSONArray(0);
					
					for (int a = 0; a < coordObjArray.length(); a++) {
						JSONArray jsonCoord = coordObjArray.getJSONArray(0);
						List<Double> featCoord = new ArrayList<>();
						featCoord.add(jsonCoord.getDouble(0));
						featCoord.add(jsonCoord.getDouble(1));
						coordList.add(featCoord);
					}
					polygonList.add(coordList);
					multiPolygonList.add(polygonList);
					
					multiPolygon.setCoordinates(multiPolygonList);
					
					listFeature.setGeometry(multiPolygon);
				}
				
				// properties
				Map<CharSequence, Object> jsonProperties = new HashMap<CharSequence, Object>();
				JSONObject jsonPop = feature.getJSONObject("properties");
				Iterator<String> keys = jsonPop.keys();

				while(keys.hasNext()) {
				    String key = keys.next();
				    jsonProperties.put(key, jsonPop.get(key));
				}
				
				listFeature.setProperties(jsonProperties);
				listOfFeatures.add(listFeature);
			}
			
			featureCollection.setFeatures(listOfFeatures);
			
			adapter.sendMessage(featureCollection, cgorName);
		} catch (CommunicationException cEx) {
			log.error("Error sending GeoJson message!", cEx);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception Ex) {
			log.error("Error sending GeoJson message!", Ex);
			return new ResponseEntity<Boolean>(false, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		log.info("sendGeoJson -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	
	@ApiOperation(value = "subscribeOnTopic", nickname = "subscribeOnTopic")
	@RequestMapping(value = "/MIRestAdaptor/subscribeOnTopic", method = RequestMethod.POST)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "topic", value = "the name of the topic to which you want to subscribe", required = true, dataType = "string", paramType = "query") })
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Boolean.class),
			@ApiResponse(code = 400, message = "Bad Request", response = Boolean.class),
			@ApiResponse(code = 500, message = "Failure", response = Boolean.class) })
	public ResponseEntity<Boolean> subscribeOnTopic(@QueryParam("topic") String topic) {
		log.info("--> subscribeOnTopic");
		
		if (this.registeredTopics.get(topic) == null) {
			this.adapter.addCallback(new AdapterCallback(), topic);
			this.registeredTopics.put(topic, topic);
		}
		
		log.info("subscribeOnTopic -->");
		return new ResponseEntity<Boolean>(true, HttpStatus.OK);
	}
	
	

}
