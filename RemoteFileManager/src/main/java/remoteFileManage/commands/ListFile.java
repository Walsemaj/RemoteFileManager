package remoteFileManage.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.json.JSONObject;

import remoteFileManage.FileManageConstant;
import remoteFileManage.FileManagePermission;
import remoteFileManage.FileManageUtil;

public class ListFile implements FileCommand {

	public JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		try {
			boolean onlyFolders;
			try {
				onlyFolders = params.getBoolean("onlyFolders");
			} catch (Exception e) {
				onlyFolders = false;
			}
			String path = params.getString("path");
			File dir = new File(FileManageUtil.getPath(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL), path);
			File[] fileList = dir.listFiles();
			LOG.debug("ListFile: REPOSITORY_BASE_URL={} PATH={}", REPOSITORY_BASE_URL, path);

			List<JSONObject> resultList = new ArrayList<JSONObject>();
			SimpleDateFormat dt = new SimpleDateFormat(FileManageConstant.DATE_FORMAT);
			if (fileList != null) {
				for (File f : fileList) {
					if (!f.exists() || (onlyFolders && !f.isDirectory())) {
						continue;
					}
					BasicFileAttributes attrs = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
					JSONObject el = new JSONObject();
					el.put("name", f.getName());
					el.put("rights", getPermissions(f));
					el.put("date", dt.format(new Date(attrs.lastModifiedTime().toMillis())));
					el.put("size", f.length());
					el.put("type", f.isFile() ? "file" : "dir");
					resultList.add(el);
				}
			}

			return new JSONObject().put("result", resultList);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("list", e);
			return FileManageUtil.error(e.getMessage());
		}
	}

	private String getPermissions(File f) throws IOException {
		PosixFileAttributeView fileAttributeView = Files.getFileAttributeView(f.toPath(), PosixFileAttributeView.class);
		if (fileAttributeView == null) // For Windows
			return getACL(f);
		PosixFileAttributes readAttributes = fileAttributeView.readAttributes();
		Set<PosixFilePermission> permissions = readAttributes.permissions();
		return PosixFilePermissions.toString(permissions);
	}

	private String getACL(File f) throws IOException {

		LOG.debug(f.toString());
		AclFileAttributeView aclView = Files.getFileAttributeView(f.toPath(), AclFileAttributeView.class);
		if (aclView == null) {
			LOG.error("ACL view  is not  supported.%n");
			throw new IOException();
		}
		AclFileAttributeView fileAttributeView = Files.getFileAttributeView(f.toPath(), AclFileAttributeView.class);

		List<AclEntry> entries;
		try {
			entries = fileAttributeView.getAcl();
			return new FileManagePermission().convertACLtoPOSIX(entries);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "---------";
	}

	public void filter() {
		return;
	}
}
