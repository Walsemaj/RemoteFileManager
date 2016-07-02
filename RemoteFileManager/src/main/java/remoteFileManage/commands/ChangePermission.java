package remoteFileManage.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;

import javax.servlet.ServletContext;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remoteFileManage.FileManageUtil;

public class ChangePermission implements FileCommand {
		
	public JSONObject apply(ServletContext context, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
//		"permsCode":"000","action":"changePermissions","perms":"---------","items":["/temp/Testing.notesairdropdocument"],"recursive":false		
		try {
			String path = params.getString("item");
			String perms = params.getString("perms"); // "653"
			String permsCode = params.getString("permsCode"); // "rw-r-x-wx"
			boolean recursive = params.getBoolean("recursive");
			LOG.debug("changepermissions path: {} perms: {} permsCode: {} recursive: {}", path, perms, permsCode,
					recursive);
			File f = new File(context.getRealPath(REPOSITORY_BASE_URL), path);
			setPermissions(f, permsCode, recursive);
			return FileManageUtil.success(params);
		} catch (Exception e) {
			LOG.error("changepermissions", e);
			return FileManageUtil.error(e.getMessage());
		}
	}
	
	private String setPermissions(File file, String permsCode, boolean recursive) throws IOException {
		// http://www.programcreek.com/java-api-examples/index.php?api=java.nio.file.attribute.PosixFileAttributes
		PosixFileAttributeView fileAttributeView = Files.getFileAttributeView(file.toPath(),
				PosixFileAttributeView.class);
		if (fileAttributeView == null) //For Windows
			return setACL(file, permsCode, recursive);
		fileAttributeView.setPermissions(PosixFilePermissions.fromString(permsCode));
		if (file.isDirectory() && recursive && file.listFiles() != null) {
			for (File f : file.listFiles()) {
				setPermissions(f, permsCode, recursive);
			}
		}
		return permsCode;
	}
	
	private String setACL(File file, String permsCode, boolean recursive) throws IOException {

		AclFileAttributeView aclView = Files.getFileAttributeView(file.toPath(), AclFileAttributeView.class);
		if (aclView == null) {
			System.out.format("ACL view  is not  supported.%n");
			throw new IOException();
		}
		return "";
	}
}