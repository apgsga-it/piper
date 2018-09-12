package com.apgsga.microservice.patch.client;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestTemplate;

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchService;
import com.apgsga.microservice.patch.api.SearchFilter;
import com.apgsga.microservice.patch.api.ServiceMetaData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class MicroservicePatchClient implements PatchService {

	private static final String FIND_BY_IDS = "/findByIds";

	private static final String FIND_BY_ID = "/findById/{id}";

	private static final String START_INSTALL_PIPELINE = "/startInstallationForTarget";

	private static final String SAVE = "/save";

	private static final String LIST_DBMODULES = "/listDbModules";

	private static final String LIST_MAVENARTIFACTS = "/listMavenArtifacts";
	
	private static final String LIST_MAVENARTIFACTS_WITH_FILTER = "/listMavenArtifactsWithFilter";

	private static final String LIST_SERVICEDATA = "/listServiceData";

	private static final String LIST_DBOBJECTS = "/listDbObjectsChanged/{id}/{search}";

	private static final String LIST_INSTALLTARGETS = "/listInstallationTargets/{requestingTarget}";

	private static final String REMOVE = "/remove";

	protected final Log LOGGER = LogFactory.getLog(getClass());

	private String baseUrl;

	private RestTemplate restTemplate;

	public MicroservicePatchClient(String baseUrl, RestTemplate restTemplate) {
		super();
		this.baseUrl = baseUrl;
		this.restTemplate = restTemplate;
	}

	public MicroservicePatchClient(String baseUrl) {
		this(baseUrl, new RestTemplate());
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
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
	public Patch save(Patch patch) {
		return restTemplate.postForObject(getRestBaseUri() + SAVE, patch, Patch.class);
	}

	@Override
	public void remove(Patch patch) {
		restTemplate.postForLocation(getRestBaseUri() + REMOVE, patch);
	}

	@Override
	public void startInstallPipeline(Patch patch) {
		restTemplate.postForLocation(getRestBaseUri() + START_INSTALL_PIPELINE, patch);
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
	public List<MavenArtifact> listMavenArtifacts(Patch patch) {
		MavenArtifact[] result = restTemplate
				.postForEntity(getRestBaseUri() + LIST_MAVENARTIFACTS, patch, MavenArtifact[].class).getBody();
		return Lists.newArrayList(result);
	}
	
	

	@Override
	public List<MavenArtifact> listMavenArtifacts(Patch patch, SearchFilter filter) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("patch", patch);
		params.put("searchFilter", filter);
		MavenArtifact[] result = restTemplate.getForObject(getRestBaseUri() + LIST_MAVENARTIFACTS_WITH_FILTER, MavenArtifact[].class, params);
		return Lists.newArrayList(result);
	}

	@Override
	public List<ServiceMetaData> listServiceData() {
		ServiceMetaData[] result = restTemplate.getForObject(getRestBaseUri() + LIST_SERVICEDATA,
				ServiceMetaData[].class);
		return Lists.newArrayList(result);
	}

	@Override
	public List<String> listInstallationTargetsFor(String requestingTarget) {
		Map<String, String> params = Maps.newHashMap();
		params.put("requestingTarget", requestingTarget);
		String[] result = restTemplate.getForObject(getRestBaseUri() + LIST_INSTALLTARGETS, String[].class, params);
		return Lists.newArrayList(result);
	}

	@Override
	public List<Patch> findByIds(List<String> patchIds) {
		Patch[] result = restTemplate.postForEntity(getRestBaseUri() + FIND_BY_IDS, patchIds, Patch[].class).getBody();
		return Lists.newArrayList(result);	
	}



}
