package cn.ezid.activiti.extension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class EzidDataGenerator implements ModelDataJsonConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(EzidDataGenerator.class);

	protected transient ProcessEngine processEngine;
	protected transient IdentityService identityService;
	protected transient RepositoryService repositoryService;

	protected boolean createUsersAndGroups;
	protected boolean createProcessDefinitions;

	public void init() {
		this.identityService = processEngine.getIdentityService();
		this.repositoryService = processEngine.getRepositoryService();

		if (createUsersAndGroups) {
			LOGGER.info("Initializing ezid groups.");
			initEzidGroups();
			LOGGER.info("Initializing ezid users.");
			initEzidUsers();
		}

		if (createProcessDefinitions) {
			LOGGER.info("Initializing ezid process definitions");
			initProcessDefinitions();
		}
	}

	public void setProcessEngine(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	public void setCreateUsersAndGroups(boolean createUsersAndGroups) {
		this.createUsersAndGroups = createUsersAndGroups;
	}

	public void setCreateProcessDefinitions(boolean createProcessDefinitions) {
		this.createProcessDefinitions = createProcessDefinitions;
	}

	protected void initEzidGroups() {
		String[] assignmentGroups = new String[] { EzidConfig.GROUP_PHOTO_PREPROCESS, EzidConfig.GROUP_PHOTO_ADAPTER,
				EzidConfig.GROUP_MACHINE_CERTIFICATION, EzidConfig.GROUP_MANUAL_CERTIFICATION,
				EzidConfig.GROUP_MANUAL_REVIEW };
		for (String groupId : assignmentGroups) {
			createGroup(groupId, "assignment");
		}

		String[] securityGroups = new String[] { EzidConfig.SECURITY_ROLE_USER, EzidConfig.SECURITY_ROLE_ADMIN };
		for (String groupId : securityGroups) {
			createGroup(groupId, "security-role");
		}
	}

	protected void createGroup(String groupId, String type) {
		if (identityService.createGroupQuery().groupId(groupId).count() == 0) {
			Group newGroup = identityService.newGroup(groupId);
			newGroup.setName(groupId.substring(0, 1).toUpperCase() + groupId.substring(1));
			newGroup.setType(type);
			identityService.saveGroup(newGroup);
		}
	}

	protected void initEzidUsers() {
		this.createUser(EzidConfig.USER_ADMIN_NAME, EzidConfig.USER_ADMIN_NAME, EzidConfig.USER_ADMIN_NAME,
				EzidConfig.USER_ADMIN_PASSWORD, EzidConfig.USER_ADMIN_EMAIL, null, Arrays.asList(
						EzidConfig.GROUP_PHOTO_PREPROCESS, EzidConfig.GROUP_PHOTO_ADAPTER,
						EzidConfig.GROUP_MACHINE_CERTIFICATION, EzidConfig.GROUP_MANUAL_CERTIFICATION,
						EzidConfig.GROUP_MANUAL_REVIEW, EzidConfig.SECURITY_ROLE_USER, EzidConfig.SECURITY_ROLE_ADMIN),
				Arrays.asList("birthDate", "10-10-1955", "jobTitle", "Muppet", "location", "Hollywoord", "phone",
						"+123456789", "twitterName", "alfresco", "skype", "activiti_kermit_frog"));

		for (int i = 0; i < 20; i++) {
			String name = EzidConfig.USER_MACHINE_PREFIX + i;
			createUser(name, name, name, name, EzidConfig.USER_MACHINE_EMAIL, null, Arrays.asList(
					EzidConfig.GROUP_PHOTO_PREPROCESS, EzidConfig.GROUP_PHOTO_ADAPTER,
					EzidConfig.GROUP_MACHINE_CERTIFICATION), null);
		}

		createUser(EzidConfig.USER_MANUAL_NAME, EzidConfig.USER_MANUAL_NAME, EzidConfig.USER_MANUAL_NAME,
				EzidConfig.USER_MANUAL_PASSWORD, EzidConfig.USER_MANUAL_EMAIL, null,
				Arrays.asList(EzidConfig.GROUP_MANUAL_CERTIFICATION, EzidConfig.GROUP_MANUAL_REVIEW), null);

	}

	protected void createUser(String userId, String firstName, String lastName, String password, String email,
			String imageResource, List<String> groups, List<String> userInfo) {

		if (identityService.createUserQuery().userId(userId).count() == 0) {
			// Following data can already be set by demo setup script
			User user = identityService.newUser(userId);
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setPassword(password);
			user.setEmail(email);
			identityService.saveUser(user);

			if (groups != null) {
				for (String group : groups) {
					identityService.createMembership(userId, group);
				}
			}
		}

		// Following data is not set by demo setup script

		// image
		if (imageResource != null) {
			byte[] pictureBytes = IoUtil.readInputStream(
					this.getClass().getClassLoader().getResourceAsStream(imageResource), null);
			Picture picture = new Picture(pictureBytes, "image/jpeg");
			identityService.setUserPicture(userId, picture);
		}

		// user info
		if (userInfo != null) {
			for (int i = 0; i < userInfo.size(); i += 2) {
				identityService.setUserInfo(userId, userInfo.get(i), userInfo.get(i + 1));
			}
		}
	}

	protected void initProcessDefinitions() {
		String deploymentName = "ezid processes";

		List<Deployment> deploymentList = repositoryService.createDeploymentQuery().deploymentName(deploymentName)
				.list();
		if (deploymentList == null || deploymentList.size() == 0) {
			DeploymentBuilder builder = repositoryService.createDeployment().name(deploymentName);
			try {
				ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
				String packageName = "cn/ezid/activiti/extension/process/";
				for (Resource resource : resolver.getResources(packageName + "*.bpmn")) {
					builder.addClasspathResource(packageName + resource.getFilename());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				LOGGER.error("Failed to init processes.", e);
			}
			builder.deploy();
		}
	}
}