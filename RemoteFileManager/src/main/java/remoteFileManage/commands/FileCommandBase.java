package remoteFileManage.commands;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import remoteFileManage.FileManageUtil;

public abstract class FileCommandBase implements FileCommand {

	private Set<String> WHITELISTED_FILES_SET = new HashSet<String>();

	public JSONObject applyCommand(ServletContext context, boolean CONTEXT_GET_REAL_PATH, String WHITELISTED_FILES,
			String RESTRICTED_FUNCTIONS, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		try {
			// "params" consists of action | path | item | items, throws
			// JSONException if not found
			LOG.info("Action: {}", params.getString("action"));
			
			//If no white list items or restricted functions is defined, filtering will not take effective
			if ("".equals(WHITELISTED_FILES) || "".equals(RESTRICTED_FUNCTIONS) || !RESTRICTED_FUNCTIONS.contains(params.getString("action"))) {
				return apply(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL, params);
			}
			return applyCommandWithWhitelisted(context, CONTEXT_GET_REAL_PATH, WHITELISTED_FILES,
					REPOSITORY_BASE_URL, params);
		} catch (Exception e) {
			LOG.error("editFile", e);
			return FileManageUtil.error(e.getMessage());
		}
	}

	public JSONObject applyCommandWithWhitelisted(ServletContext context, boolean CONTEXT_GET_REAL_PATH,
			String WHITELISTED_FILES, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		try {
			if (this.WHITELISTED_FILES_SET.size() == 0 && !"".equals(WHITELISTED_FILES)) {
				LOG.info("Whitelisted: {}", WHITELISTED_FILES);
				for (String file : WHITELISTED_FILES.split(",")) {
					this.WHITELISTED_FILES_SET.add(file);
				}
			}

			String targetFile = getFile(params);
			if (targetFile != null) {
				if (this.WHITELISTED_FILES_SET.contains(targetFile))
					return apply(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL, params);
			} else {
				boolean permitToAccess = true;
				JSONArray targetFiles = getFiles(params);
				if (targetFiles != null) {
					for (Object obj : targetFiles) {
						if (!this.WHITELISTED_FILES_SET.contains(obj.toString())) {
							permitToAccess = false;
							break;
						}
					}
					if (permitToAccess)
						return apply(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL, params);
				}
			}

			return FileManageUtil.error("ACCESS DENICED");
		} catch (Exception e) {
			LOG.error("editFile", e);
			return FileManageUtil.error(e.getMessage());
		}
	}

	private String getFile(JSONObject params) {
		try {
			return params.getString("path");
		} catch (JSONException e) {
		}
		try {
			return params.getString("item");
		} catch (JSONException e) {
		}
		return null;
	}

	private JSONArray getFiles(JSONObject params) {
		try {
			return params.getJSONArray("items");
		} catch (JSONException e) {
		}
		return null;
	}

	protected abstract JSONObject apply(ServletContext context, boolean CONTEXT_GET_REAL_PATH,
			String REPOSITORY_BASE_URL, JSONObject params) throws Exception;
}
