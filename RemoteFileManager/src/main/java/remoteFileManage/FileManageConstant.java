package remoteFileManage;

public class FileManageConstant {
	public static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss"; // (2001-07-04 // 12:08:56)
	public static final int BUFFER = 2048;
	
	public static final String BUILTIN_USER = "BUILTIN\\Users (Alias)";
	public static final String AUTHENTICAED_USERS = "NT AUTHORITY\\Authenticated Users (Well-known group)";
	public static final String EVERYONE = "\\Everyone (Well-known group)";
	public static final String CURRENT_USER_SUFFIX = "(User)";
	
	public static final String READ = "r";
	public static final String WRITE = "w";
	public static final String EXECUTE = "x";
	public static final String EMPTY = "-";
	
	public static final String PERMISSION_CLASS_OWNER = "OWNER";
	public static final String PERMISSION_CLASS_GROUP = "GROUP";
	public static final String PERMISSION_CLASS_OTHER = "OTHER";
	
	public static final String EMPTY_PERMISSION_CLASS = "---";
}
