package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;
import com.jcraft.jsch.Logger;

public abstract class PatchVcsCommand implements VcsCommand {
	
	protected final static Log LOGGER = LogFactory.getLog(PatchVcsCommand.class.getName());


	public static VcsCommand createCreatePatchBranchCmd(String patchBranch, String prodBranch, List<String> modules) {
		return new CreatePatchBranchCmd(patchBranch, prodBranch, modules);
	}

	public static VcsCommand createCreatePatchBranchCmd(String patchBranch, String prodBranch, String additionalOptions,
			List<String> modules) {
		return new CreatePatchBranchCmd(patchBranch, prodBranch, additionalOptions, modules);
	}

	public static VcsCommand createDiffPatchModulesCmd(String patchBranch, String prodBranch, List<String> modules) {
		return new DiffPatchModulesCmd(patchBranch, prodBranch, modules);
	}

	public static VcsCommand createDiffPatchModulesCmd(String patchBranch, String prodBranch, String module) {
		return new DiffPatchModulesCmd(patchBranch, prodBranch, Lists.newArrayList(module));
	}
	
	public static VcsCommand createDiffPatchModulesCmd(String patchBranch, String prodBranch,String additionalOptions, String module) {
		return new DiffPatchModulesCmd(patchBranch, prodBranch,additionalOptions, Lists.newArrayList(module));
	}
	
	public static VcsCommand createDiffPatchModulesCmd(String patchBranch, String prodBranch,String additionalOptions, List<String> modules) {
		return new DiffPatchModulesCmd(patchBranch, prodBranch, additionalOptions,modules);
	}


	public static VcsCommand createTagPatchModulesCmd(String patchBranch, String prodBranch, List<String> modules) {
		return new TagPatchModulesCmd(patchBranch, prodBranch, modules);
	}

	public static VcsCommand createTagPatchModulesCmd(String patchBranch, String prodBranch, String additionalOptions,
			List<String> modules) {
		return new TagPatchModulesCmd(patchBranch, prodBranch, additionalOptions, modules);
	}
	
	public static VcsCommand createSilentCoCvsModuleCmd(String cvsBranch, List<String> modules, String lastPart) {
		return new SilentCOCvsModuleCmd(cvsBranch, modules, lastPart);
	}

	protected String patchTag;

	protected String prodBranch;

	protected List<String> modules;

	protected String additionalOptions = null;
	
	protected String lastPart = null;
	
	protected boolean noSystemCheck = false; 

	public PatchVcsCommand(String patchBranch, String prodBranch, List<String> modules) {
		super();
		this.patchTag = patchBranch;
		this.prodBranch = prodBranch;
		this.modules = modules;
	}
	
	public PatchVcsCommand(String cvsBranch, List<String> modules, String lastPart) {
		super();
		this.prodBranch = cvsBranch;
		this.modules = modules;
		this.lastPart = lastPart;
	}

	public PatchVcsCommand(String patchBranch, String prodBranch, String additionalOptions, List<String> modules) {
		super();
		this.patchTag = patchBranch;
		this.prodBranch = prodBranch;
		this.additionalOptions = additionalOptions;
		this.modules = modules;
	}

	@Override
	public String[] getCommand() {
		String[] processBuilderParm;
		if (SystemUtils.IS_OS_WINDOWS && !noSystemCheck) {
			processBuilderParm = new String[] { "bash.exe", "-c", "-s", "cvs " + getParameterSpaceSeperated() };
		} else {
			processBuilderParm = getParameterAsArray();
		}
		LOGGER.info("ProcessBuilder Parameters: " + Arrays.toString(processBuilderParm).toString()); 
		return processBuilderParm; 
	}

	private String getParameterSpaceSeperated() {
		String[] processParm = Stream.concat(Arrays.stream(getFristPart()), Arrays.stream(modules.toArray()))
				.toArray(String[]::new);
		if(lastPart != null) {
			processParm = Stream.concat(Arrays.stream(processParm), Arrays.stream(new String[] {lastPart})).toArray(String[]::new);
		}		
		String parameter = String.join(" ", processParm);
		if (additionalOptions != null) {
			return additionalOptions + " " + parameter;
		}
		return parameter;
	}
	
	private String[] getParameterAsArray() {
		String[] parameter = Stream.concat(Arrays.stream(getFristPart()), Arrays.stream(modules.toArray()))
				.toArray(String[]::new);
		
		if(lastPart != null) {
			parameter = Stream.concat(Arrays.stream(parameter), Arrays.stream(new String[] {lastPart})).toArray(String[]::new);
		}
		
		// TODO (che, 4.4.2018) : either via bash or path 
		// TODO (che, 4.4.2018) : cvs Root either in Enviroment or Configuration
		String[] processBuilderParm = Stream.concat(Arrays.stream(new String[] { "/usr/bin/cvs", "-d", "/var/local/cvs/root" }), Arrays.stream(parameter)).toArray(String[]::new);
		return processBuilderParm;
	}

	protected abstract String[] getFristPart();

	@Override
	public void noSystemCheck(boolean noChecnk) {
		this.noSystemCheck = noChecnk;
	}
	
	

}
