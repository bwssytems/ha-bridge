package com.bwssystems.HABridge.plugins.fibaro;

public class FibaroFilter {
	boolean useSaveLogs;
	boolean useUserDescription;
	boolean scenesLiliCmddOnly;
	boolean replaceTrash;
	
	public boolean isUseSaveLogs() {
		return useSaveLogs;
	}
	public void setUseSaveLogs(boolean useSaveLogs) {
		this.useSaveLogs = useSaveLogs;
	}
	public boolean isUseUserDescription() {
		return useUserDescription;
	}
	public void setUseUserDescription(boolean useUserDescription) {
		this.useUserDescription = useUserDescription;
	}
	public boolean isReplaceTrash() {
		return replaceTrash;
	}
	public void setReplaceTrash(boolean replaceTrash) {
		this.replaceTrash = replaceTrash;
	}
	public boolean isScenesLiliCmddOnly() {
		return scenesLiliCmddOnly;
	}
	public void setScenesLiliCmddOnly(boolean scenesLiliCmddOnly) {
		this.scenesLiliCmddOnly = scenesLiliCmddOnly;
	}

}
