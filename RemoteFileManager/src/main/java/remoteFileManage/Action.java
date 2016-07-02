package remoteFileManage;

public enum Action {
		download, list, rename, copy, move, remove, 
		getContent, //Edit a file
		edit, //Save changes after edit 
		createFolder, changePermissions, compress, extract
}
