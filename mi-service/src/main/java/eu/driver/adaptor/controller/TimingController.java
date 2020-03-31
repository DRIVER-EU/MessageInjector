package eu.driver.adaptor.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import eu.driver.adapter.core.CISAdapter;
import eu.driver.model.sim.config.TimeState;


@RestController
public class TimingController implements ResourceProcessor<RepositoryLinksResource> {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public RepositoryLinksResource process(RepositoryLinksResource resource) {
		return resource;
	}
	@ApiOperation(value = "getTrialTime", nickname = "getTrialTime")
	@RequestMapping(value = "/MIRestAdaptor/getTrialTime/", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Long.class)})
	public ResponseEntity<Long> getTrialTime() {
		log.info("--> getTrialTime");
		
		Date trialTime = CISAdapter.getInstance().getTrialTime();
		if (trialTime == null) {
			trialTime = new Date();
		}
		
		log.info("getTrialTime -->");
		return new ResponseEntity<Long>(trialTime.getTime(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "getTimeElapsed", nickname = "getTimeElapsed")
	@RequestMapping(value = "/MIRestAdaptor/getTimeElapsed/", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Long.class)})
	public ResponseEntity<Long> getTimeElapsed() {
		log.info("--> getTrialTime");
		
		Long timeElapsed = CISAdapter.getInstance().getTimeElapsed();
		
		log.info("getTrialTime -->");
		return new ResponseEntity<Long>(timeElapsed, HttpStatus.OK);
	}
	
	@ApiOperation(value = "getState", nickname = "getState")
	@RequestMapping(value = "/MIRestAdaptor/getState/", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = String.class)})
	public ResponseEntity<String> getState() {
		log.info("--> getTrialTime");
		
		TimeState state = CISAdapter.getInstance().getState();
		
		log.info("getTrialTime -->");
		return new ResponseEntity<String>(state.toString(), HttpStatus.OK);
	}
	
	@ApiOperation(value = "getTrialSpeed", nickname = "getTrialSpeed")
	@RequestMapping(value = "/MIRestAdaptor/getTrialSpeed/", method = RequestMethod.GET)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Long.class)})
	public ResponseEntity<Long> getTrialSpeed() {
		log.info("--> getTrialTime");
		
		Long trialSpeed = CISAdapter.getInstance().getTrialSpeed();
		
		log.info("getTrialTime -->");
		return new ResponseEntity<Long>(trialSpeed, HttpStatus.OK);
	}

}
