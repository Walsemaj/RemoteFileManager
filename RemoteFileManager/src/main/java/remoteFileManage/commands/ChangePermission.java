package remoteFileManage.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import remoteFileManage.FileManagePermission;
import remoteFileManage.FileManagePosixPermission;
import remoteFileManage.FileManageUtil;

public class ChangePermission extends FileCommandBase {
	
	protected JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		try {
			JSONArray array = params.getJSONArray("items");
			for (Object temp : array) {
				String path = StringUtils.substringAfter(temp.toString(), "/");
				String perms = params.getString("perms"); // "653"
				String permsCode = params.getString("permsCode"); // "rw-r-x-wx"
				boolean recursive = params.getBoolean("recursive");
				LOG.debug("changepermissions path: {} perms: {} permsCode: {} recursive: {}", path, perms, permsCode,
						recursive);
//				File f = new File(context.getRealPath(REPOSITORY_BASE_URL), path);
				File f = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL), path);
				setPermissions(f, permsCode, perms, recursive);
			}
			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("changepermissions", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
	
	private String setPermissions(File file, String permsCode, String perms, boolean recursive) throws IOException, ClassNotFoundException {
		PosixFileAttributeView fileAttributeView = Files.getFileAttributeView(file.toPath(),
				PosixFileAttributeView.class);
		if (fileAttributeView == null) //For Windows
			return setACL(file, perms, recursive);
//		fileAttributeView.setPermissions(PosixFilePermissions.fromString(permsCode));
		new FileManagePosixPermission().setPermissions(file, perms);
		if (file.isDirectory() && recursive && file.listFiles() != null) {
			for (File f : file.listFiles()) {
				setPermissions(f, permsCode, perms, recursive);
			}
		}
		return permsCode;
	}
	
	private String setACL(File file, String perms, boolean recursive) throws IOException {
		//Test Commit
		AclFileAttributeView aclView = Files.getFileAttributeView(file.toPath(), AclFileAttributeView.class);
		if (aclView == null) {
			System.out.format("ACL view  is not  supported.%n");
			throw new IOException();
		}
		new FileManagePermission().setPermissions(file, perms);
		if (file.isDirectory() && recursive && file.listFiles() != null) {
			for (File f : file.listFiles()) {
				setACL(f, perms, recursive);
			}
		}
		return "";
	}
}