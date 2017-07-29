package cn.ezid.activiti.extension.callback;

public interface CallbackService {
	public static final String IN_VAR_CERT_CALLBACK = "in_certCallback";

	public void callback(final String callback, final String processInstatnceId);
}
