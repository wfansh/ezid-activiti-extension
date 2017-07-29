package cn.ezid.activiti.extension.callback;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.activiti.rest.service.application.ApplicationContextHolder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallbackServiceImpl implements CallbackService {
	private static final Logger log = LoggerFactory.getLogger(CallbackServiceImpl.class); 

	private HttpClient httpClient = this.wrapClient();

	@Override
	public void callback(final String callback, final String processInstatnceId) {
		// TODO Auto-generated method stub
		try {
			invokeCallback(callback, processInstatnceId);
		} catch (IOException e) {
			log.warn("Failed to invoke call back for instance {}. Caused by : {}.", processInstatnceId, e.getMessage());
			CustomSqlExecution<CallbackFailoverMapper, Boolean> insertCallbackExecution = new AbstractCustomSqlExecution<CallbackFailoverMapper, Boolean>(CallbackFailoverMapper.class)  {
				@Override
				public Boolean execute(CallbackFailoverMapper mapper) {
					// TODO Auto-generated method stub
					mapper.insertCallback(processInstatnceId, callback);
					return true;
				}
			};
			
			log.info("Insert call failover record for instance {}, callback {}.", processInstatnceId, callback);
			ApplicationContextHolder.getApplicationContext().getBean(ProcessEngineConfiguration.class)
					.getManagementService().executeCustomSql(insertCallbackExecution);
		}

	}

	private void invokeCallback(String callback, String processInstatnceId) throws IOException {
		String url = callback.contains("?") ? callback + "&instanceId=" + processInstatnceId : 
			callback + "?instanceId=" + processInstatnceId;
		
		log.warn("Invoke call back url {}", url);
		
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml,application/json;q=0.9,*/*;q=0.8");
		httpGet.setHeader("Accept-Charset", "utf-8;q=0.7,*;q=0.7");
		httpGet.setHeader("Connection", "Keep-Alive");  
		
		HttpResponse response = httpClient.execute(httpGet);
		if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_BAD_REQUEST) {
			throw new IOException("Response code is " + response.getStatusLine().getStatusCode());
		}

		String body = new String(EntityUtils.toByteArray(response.getEntity()), "UTF-8");
		log.debug("Response body {}", body);
	}

	
	private final HttpClient wrapClient() {
		try {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustStrategy() {
				@Override
				public boolean isTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws java.security.cert.CertificateException {
					return true;
				}
			});

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), null, null,
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | RuntimeException e) {
			log.error("Failed to init http client. Caused by {}", e);
			return null;
		}
	}
}
