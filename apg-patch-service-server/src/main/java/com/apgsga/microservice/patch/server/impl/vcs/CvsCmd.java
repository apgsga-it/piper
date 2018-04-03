package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.Arrays;
import java.util.stream.Stream;

public abstract class CvsCmd implements VcsCommand {
	
	public static VcsCommand createCreatePatchBranchCmd(String patchBranch, String prodBranch, String... modules) {
		return new CreatePatchBranchCmd(patchBranch, prodBranch, modules);
	}
	
	public static VcsCommand createDiffPatchModulesCmd(String patchBranch, String prodBranch, String ...modules) {
		return new DiffPatchModulesCmd(patchBranch, prodBranch, modules);
	}
	
	public static VcsCommand createTagPatchModulesCmd(String patchBranch, String prodBranch, String... modules) {
		return new TagPatchModulesCmd(patchBranch, prodBranch, modules);
	}

	
	protected String patchBranch;

	protected String prodBranch;

	protected String[] modules;


	public CvsCmd(String patchBranch, String prodBranch, String[] modules) {
		super();
		this.patchBranch = patchBranch;
		this.prodBranch = prodBranch;
		this.modules = modules;
	}


	@Override
	public String[] getCommand() {
		return Stream.concat(Arrays.stream(getFristPart()), Arrays.stream(modules)).toArray(String[]::new);
	}
	
	
	protected abstract String[] getFristPart();

}
