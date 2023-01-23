package basyx.components.databridge.core.dto;

import java.util.Map;
import org.apache.camel.health.HealthCheck;
import org.apache.camel.health.HealthCheck.Result;

public class HealthCheckResultDTO {
	
    private String message;
    private Throwable error;
    private Map<String, Object> details;
    private HealthCheck.State state;
    
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public Throwable getError() {
		return error;
	}
	
	public void setError(Throwable error) {
		this.error = error;
	}
	
	public Map<String, Object> getDetails() {
		return details;
	}
	
	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}
	
	public HealthCheck.State getState() {
		return state;
	}
	
	public void setState(HealthCheck.State state) {
		this.state = state;
	}
	
	public static HealthCheckResultDTO toDTO(HealthCheck.Result result) {
		HealthCheckResultDTO healthCheckResultDTO = new HealthCheckResultDTO();
		healthCheckResultDTO.setMessage(getOptionalMessage(result));
		healthCheckResultDTO.setError(getOptionalError(result));
		healthCheckResultDTO.setDetails(result.getDetails());
	    healthCheckResultDTO.setState(result.getState());
	    
	    return healthCheckResultDTO;
	}

	private static String getOptionalMessage(HealthCheck.Result result) {
		if(!result.getMessage().isPresent()) {
			return "";
		}
		
		return result.getMessage().get();
	}
	
	private static Throwable getOptionalError(Result result) {
		if(!result.getError().isPresent()) {
			return null;
		}
		
		return result.getError().get();
	}
}
