package cn.ezid.activiti.extension.callback;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface CallbackFailoverMapper {

	@Insert("insert into ezid_callback_failover_exchange (process_instance_id, callback_url, enabled, time_created)	values (#{processInstanceId}, #{callbackUrl}, true, now())")
	public void insertCallback(@Param("processInstanceId") String processInstanceId,
			@Param("callbackUrl") String callbackUrl);

}
