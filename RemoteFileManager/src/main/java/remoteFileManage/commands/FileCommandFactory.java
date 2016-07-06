package remoteFileManage.commands;

import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import remoteFileManage.Action;

public final class FileCommandFactory {
	private final HashMap<Action, FileCommand> fileCommands;
	private static final Logger LOG = LoggerFactory.getLogger(FileCommandFactory.class);

	private FileCommandFactory() {
		fileCommands = new HashMap<>();
	}

	public void addFileCommand(Action action, FileCommand fileCommand) {
		fileCommands.put(action, fileCommand);
	}

	public JSONObject executeFileCommand(Action action, ServletContext context, boolean CONTEXT_GET_REAL_PATH, String REPOSITORY_BASE_URL, JSONObject params) throws Exception {
		try {
			if (fileCommands.containsKey(action)) {
				return fileCommands.get(action).apply(context, CONTEXT_GET_REAL_PATH, REPOSITORY_BASE_URL, params);
			} else
				throw new ServletException("not implemented");
		} catch (Exception e) {
			throw new ServletException("not implemented");
		}
	}

	public void listCommands() {
		//1.8
//		LOG.info("Enabled file comamnds: " + fileCommands.keySet().stream().map(k->k.name()).collect(Collectors.joining(",", "{", "}")));
		Iterator<Action> it = fileCommands.keySet().iterator();
		String output = ""; 
		while(it.hasNext())
		{
			Action action = it.next();
			output += action.name() + "|";
		}
		LOG.info("Enabled file comamnds: " + output);
	}

	public static FileCommandFactory init() {
		FileCommandFactory factory = new FileCommandFactory();
		factory.addFileCommand(Action.list, new ListFile());
		factory.addFileCommand(Action.rename, new Rename());
		factory.addFileCommand(Action.copy, new Copy());
		factory.addFileCommand(Action.move, new Move());
		factory.addFileCommand(Action.remove, new Delete());
		factory.addFileCommand(Action.getContent, new EditFile());
		factory.addFileCommand(Action.edit, new SaveFile());
		factory.addFileCommand(Action.createFolder, new CreateFolder());
		factory.addFileCommand(Action.changePermissions, new ChangePermission());
		factory.addFileCommand(Action.compress, new Compress());
		factory.addFileCommand(Action.extract, new Extract());
		
		return factory;
	}
}
