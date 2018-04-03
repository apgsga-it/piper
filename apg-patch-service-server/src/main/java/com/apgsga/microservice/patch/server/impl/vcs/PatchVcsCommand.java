package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.assertj.core.util.Lists;

public abstract class PatchVcsCommand implements VcsCommand {
	
	public static VcsCommand createCreatePatchBranchCmd(String patchBranch, String prodBranch, List<String> modules) {
		return new CreatePatchBranchCmd(patchBranch, prodBranch, modules);
	}
	
	public static VcsCommand createDiffPatchModulesCmd(String patchBranch, String prodBranch, List<String> modules) {
		return new DiffPatchModulesCmd(patchBranch, prodBranch, modules);
	}
	
	public static VcsCommand createDiffPatchModulesCmd(String patchBranch, String prodBranch, String module) {
		return new DiffPatchModulesCmd(patchBranch, prodBranch, Lists.newArrayList(module));
	}
	
	public static VcsCommand createTagPatchModulesCmd(String patchBranch, String prodBranch, List<String> modules) {
		return new TagPatchModulesCmd(patchBranch, prodBranch, modules);
	}

	
	protected String patchBranch;

	protected String prodBranch;

	protected List<String> modules;


	public PatchVcsCommand(String patchBranch, String prodBranch, List<String> modules) {
		super();
		this.patchBranch = patchBranch;
		this.prodBranch = prodBranch;
		this.modules = modules;
	}


	@Override
	public String[] getCommand() {
		return Stream.concat(Arrays.stream(getFristPart()), Arrays.stream(modules.toArray())).toArray(String[]::new);
	}
	
	
	protected abstract String[] getFristPart();

}
