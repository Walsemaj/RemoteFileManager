package remoteFileManage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FileManageConstant {
	public static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
	public static final int BUFFER = 2048;
	
	public static final String BUILTIN_USER = "BUILTIN\\Users (Alias)";
	public static final String AUTHENTICAED_USERS = "NT AUTHORITY\\Authenticated Users (Well-known group)";
	public static final String EVERYONE = "\\Everyone (Well-known group)";
	public static final String CURRENT_USER_SUFFIX = "(User)";
	
	public static final char READ = 'r';
	public static final char WRITE = 'w';
	public static final char EXECUTE = 'x';
	public static final char EMPTY = '-';
	
	public static final String PERMISSION_CLASS_OWNER = "OWNER";
	public static final String PERMISSION_CLASS_GROUP = "GROUP";
	public static final String PERMISSION_CLASS_OTHERS = "OTHERS";
	public static final String POSIX_PERMISSION_READ_SUFFIX = "_READ";
	public static final String POSIX_PERMISSION_WRITE_SUFFIX = "_WRITE";
	public static final String POSIX_PERMISSION_EXECUTE_SUFFIX = "_EXECUTE";
	
	public static final List<String> PERMISSION_GROUP_LIST = new LinkedList<String>(Arrays.asList(FileManageConstant.PERMISSION_CLASS_OWNER, FileManageConstant.PERMISSION_CLASS_GROUP, FileManageConstant.PERMISSION_CLASS_OTHERS));
	
	public static final String EMPTY_PERMISSION_CLASS = "---";
}
