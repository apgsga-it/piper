package com.apgsga.microservice.patch.client;

import com.apgsga.microservice.patch.api.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class MicroservicePatchClient implements PatchService {

	private static final String FIND_BY_IDS = "/findByIds";

	private static final String FIND_BY_ID = "/findById/{id}";
	
	private static final String FIND_PATCH_LOG_BY_ID = "/findPatchLogById/{id}";

	private static final String SAVE = "/save";
	
	private static final String LIST_DBMODULES = "/listDbModules";

	private static final String LIST_MAVENARTIFACTS = "/listMavenArtifacts/{serviceName}";

	private static final String LIST_MAVENARTIFACTS_WITH_FILTER = "/listMavenArtifactsWithFilter/{serviceName}/{searchCondition}";

	private static final String LIST_SERVICEDATA = "/listServiceData";

	private static final String LIST_DBOBJECTS = "/listDbObjectsChanged/{id}/{search}";

	private static final String LIST_ALL_DBOBJECTS = "/listAllDbObjects/{id}/{search}";

	private static final String LIST_ALL_DBOBJECTS_FOR_USER = "/listAllDbObjectsForUser/{id}/{search}/{username}";

	private static final String LIST_INSTALLTARGETS = "/listInstallationTargets";

	private static final String REMOVE = "/remove";
	
	private static final String FIND_WITH_OBJECT_NAME = "/findWithObjectName";

	private static final String ON_DEMAND_INSTALLATION = "/onDemand";

	protected static Log LOGGER = LogFactory.getLog(MicroservicePatchClient.class.getName());

	private final String baseUrl;

	private final RestTemplate restTemplate;

	public MicroservicePatchClient(String baseUrl, RestTemplate restTemplate) {
		super();
		this.baseUrl = baseUrl;
		this.restTemplate = restTemplate;
	}

	public MicroservicePatchClient(String baseUrl) {
		this(baseUrl, new RestTemplate());
	}

	private String getRestBaseUri() {
		return "http://" + baseUrl + "/patch/public";
	}

	@Override
	public List<String> listDbModules() {
		String[] result = restTemplate.getForObject(getRestBaseUri() + LIST_DBMODULES, String[].class);
		return Lists.newArrayList(result);
	}

	@Override
	public Patch findById(String patchNummer) {
		Map<String, String> params = Maps.newHashMap();
		params.put("id", patchNummer);
		return restTemplate.getForObject(getRestBaseUri() + FIND_BY_ID, Patch.class, params);
	}
	
	@Override
	public PatchLog findPatchLogById(String patchNummer) {
		Map<String, String> params = Maps.newHashMap();
		params.put("id", patchNummer);
		return restTemplate.getForObject(getRestBaseUri() + FIND_PATCH_LOG_BY_ID, PatchLog.class, params);
		
	}

	@Override
	public Patch save(Patch patch) {
		return restTemplate.postForObject(getRestBaseUri() + SAVE, patch, Patch.class);
	}
	
	@Override
	public void log(String patchNumber, PatchLogDetails logDetails) {
		throw new UnsupportedOperationException("Logging patch activity not supported");
	}

	@Override
	public void remove(Patch patch) {
		restTemplate.postForLocation(getRestBaseUri() + REMOVE, patch);
	}

	@Override
	public List<DbObject> listAllObjectsChangedForDbModule(String patchNumber, String searchString) {
		Map<String, String> params = Maps.newHashMap();
		params.put("id", patchNumber);
		params.put("search", searchString);
		DbObject[] result = restTemplate.getForObject(getRestBaseUri() + LIST_DBOBJECTS, DbObject[].class, params);
		return Lists.newArrayList(result);
	}

	@Override
	public List<DbObject> listAllObjectsForDbModule(String patchNumber, String searchString) {
		Map<String, String> params = Maps.newHashMap();
		params.put("id", patchNumber);
		params.put("search", searchString);
		DbObject[] result = restTemplate.getForObject(getRestBaseUri() + LIST_ALL_DBOBJECTS, DbObject[].class, params);
		return Lists.newArrayList(result);
	}

	@Override
	public List<DbObject> listAllObjectsForDbModule(String patchNumber, String searchString, String username) {
		Map<String, String> params = Maps.newHashMap();
		params.put("id", patchNumber);
		params.put("search", searchString);
		params.put("username", username);
		DbObject[] result = restTemplate.getForObject(getRestBaseUri() + LIST_ALL_DBOBJECTS_FOR_USER, DbObject[].class, params);
		return Lists.newArrayList(result);
	}

	@Override
	public List<MavenArtifact> listMavenArtifacts(String serviceName) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("serviceName", serviceName);
		MavenArtifact[] result = restTemplate.getForObject(getRestBaseUri() + LIST_MAVENARTIFACTS,
				MavenArtifact[].class, params);
		return Lists.newArrayList(result);
	}

	@Override
	public List<MavenArtifact> listMavenArtifacts(String serviceName, SearchCondition filter) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("serviceName", serviceName);
		params.put("searchCondition", filter.toValue());
		MavenArtifact[] result = restTemplate.getForObject(getRestBaseUri() + LIST_MAVENARTIFACTS_WITH_FILTER,
				MavenArtifact[].class, params);
		return Lists.newArrayList(result);
	}

	@Override
	public List<ServiceMetaData> listServiceData() {
		ServiceMetaData[] result = restTemplate.getForObject(getRestBaseUri() + LIST_SERVICEDATA,
				ServiceMetaData[].class);
		return Lists.newArrayList(result);
	}

	@Override
	public List<String> listOnDemandTargets() {
		String[] result = restTemplate.getForObject(getRestBaseUri() + LIST_INSTALLTARGETS, String[].class);
		return Lists.newArrayList(result);
	}

	@Override
	public List<Patch> findByIds(List<String> patchIds) {
		Patch[] result = restTemplate.postForEntity(getRestBaseUri() + FIND_BY_IDS, patchIds, Patch[].class).getBody();
		return Lists.newArrayList(result);
	}

	@Override
	public List<Patch> findWithObjectName(String objectName) {
		Patch[] result = restTemplate.postForEntity(getRestBaseUri() + FIND_WITH_OBJECT_NAME, objectName, Patch[].class).getBody();
		return Lists.newArrayList(result);
	}

	@Override
	public void startOnDemandInstallation(OnDemandParameter params) {
		restTemplate.postForLocation(getRestBaseUri() + ON_DEMAND_INSTALLATION, params);
	}
}
