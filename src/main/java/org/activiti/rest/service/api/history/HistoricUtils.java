package org.activiti.rest.service.api.history;

import java.util.Date;

import org.activiti.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntity;

public class HistoricUtils {
	
	@SuppressWarnings("deprecation")
	public static void copyVariableValues(HistoricVariableInstanceEntity hisVariableEntity, VariableInstanceEntity variableEntity, Date current) {
		hisVariableEntity.setTextValue(variableEntity.getTextValue());
		hisVariableEntity.setTextValue2(variableEntity.getTextValue2());
		hisVariableEntity.setDoubleValue(variableEntity.getDoubleValue());
		hisVariableEntity.setLongValue(variableEntity.getLongValue());
		hisVariableEntity.setVariableType(variableEntity.getType());
	    if (variableEntity.getByteArrayValueId()!=null) {
	    	hisVariableEntity.setByteArrayValue(variableEntity.getByteArrayValue().getBytes());
	    }
	    hisVariableEntity.setLastUpdatedTime(current);
	}

}
