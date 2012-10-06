package org.kunicki.squirrel_sql.plugins;

import net.sourceforge.squirrel_sql.client.plugin.DefaultSessionPlugin;
import net.sourceforge.squirrel_sql.client.plugin.PluginException;
import net.sourceforge.squirrel_sql.client.plugin.PluginResources;
import net.sourceforge.squirrel_sql.client.plugin.PluginSessionCallback;
import net.sourceforge.squirrel_sql.client.plugin.PluginSessionCallbackAdaptor;
import net.sourceforge.squirrel_sql.client.preferences.IGlobalPreferencesPanel;
import net.sourceforge.squirrel_sql.client.session.IObjectTreeAPI;
import net.sourceforge.squirrel_sql.client.session.ISession;
import net.sourceforge.squirrel_sql.fw.dialects.DialectFactory;
import net.sourceforge.squirrel_sql.fw.sql.DatabaseObjectType;
import net.sourceforge.squirrel_sql.fw.util.IMessageHandler;

/**
 * <pre>
 * The Example plugin class. If the database session's type is DB2, this plugin registers a menu action in 
 * the popup menu for view and procedure nodes in the ObjectTree. 
 * 
 * For detailed information and usage of the Plugin API see the following:
 * 
 * https://sourceforge.net/apps/trac/squirrel-sql/wiki/SQuirreLSQLClientPluginAPI
 * </pre>
 */
public class FollowForeignKeyPlugin extends DefaultSessionPlugin
{
	private PluginResources _resources;

	/**
	 * Return the internal name of this plugin.
	 * 
	 * @return the internal name of this plugin.
	 */
	public String getInternalName()
	{
		return "squirrel-sql-follow-foreign-key-plugin";
	}

	/**
	 * Return the descriptive name of this plugin.
	 * 
	 * @return the descriptive name of this plugin.
	 */
	public String getDescriptiveName()
	{
		return "follow-foreign-key Plugin";
	}

	/**
	 * Returns the current version of this plugin.
	 * 
	 * @return the current version of this plugin.
	 */
	public String getVersion()
	{
		return "0.01";
	}

	/**
	 * Returns the authors name.
	 * 
	 * @return the authors name.
	 */
	public String getAuthor()
	{
		return "Your Name Here";
	}

	/**
	 * Returns the name of the change log for the plugin. This should be a text or HTML file residing in the
	 * <TT>getPluginAppSettingsFolder</TT> directory.
	 * 
	 * @return the changelog file name or <TT>null</TT> if plugin doesn't have a change log.
	 */
	public String getChangeLogFileName()
	{
		return "changes.txt";
	}

	/**
	 * Returns the name of the Help file for the plugin. This should be a text or HTML file residing in the
	 * <TT>getPluginAppSettingsFolder</TT> directory.
	 * 
	 * @return the Help file name or <TT>null</TT> if plugin doesn't have a help file.
	 */
	public String getHelpFileName()
	{
		return "readme.txt";
	}

	/**
	 * Returns the name of the Licence file for the plugin. This should be a text or HTML file residing in the
	 * <TT>getPluginAppSettingsFolder</TT> directory.
	 * 
	 * @return the Licence file name or <TT>null</TT> if plugin doesn't have a licence file.
	 */
	public String getLicenceFileName()
	{
		return "licence.txt";
	}

	/**
	 * @return Comma separated list of contributors.
	 */
	public String getContributors()
	{
		return "";
	}

	/**
	 * Create preferences panel for the Global Preferences dialog.
	 * 
	 * @return Preferences panel.
	 */
	public IGlobalPreferencesPanel[] getGlobalPreferencePanels()
	{
		return new IGlobalPreferencesPanel[0];
	}

	/**
	 * Initialize this plugin.
	 */
	public synchronized void initialize() throws PluginException
	{
		_resources = new PluginResources("net.sourceforge.squirrel_sql.plugins.squirrel-sql-follow-foreign-key-plugin.squirrel-sql-follow-foreign-key-plugin", this);
	}

	/**
	 * Called when a session started. Add commands to popup menu in object tree.
	 * 
	 * @param session
	 *           The session that is starting.
	 * @return An implementation of PluginSessionCallback or null to indicate the plugin does not work with
	 *         this session
	 */
	public PluginSessionCallback sessionStarted(ISession session)
	{
		// Adds the view and procedure script actions if the session is DB2.
		addTreeNodeMenuActionsForDB2(session);

		// Register a custom ISQLExecutionListener implementation that simply prints all SQL being executed to
		// the message panel.
		IMessageHandler messageHandler = session.getApplication().getMessageHandler();
		ExampleSqlExecutionListener sqlExecutionListener = new ExampleSqlExecutionListener(messageHandler);
		session.getSessionSheet().getSQLPaneAPI().addSQLExecutionListener(sqlExecutionListener);

		// We will override the default behavior of formatting exception messages that SQuirreL provides for 
		// this session with our own.  If this was a real plugin implementation, care would need to be taken 
		// that the custom formatter is only applied to database vendor sessions that this plugin was written 
		// for.  SQuirreL doesn't support registering multiple exception formatters for a single session.  The
		// last one to register overrides all former registrations and results in a log warning message.
		session.setExceptionFormatter(new ExampleExceptionFormatter());
		
		return new PluginSessionCallbackAdaptor(this);
	}

	private void addTreeNodeMenuActionsForDB2(ISession session)
	{
		try
		{
			if (DialectFactory.isDB2(session.getMetaData()))
			{
				// Plugin knows only how to script Views and Stored Procedures on DB2.
				// So if it's not a DB2 Session we tell SQuirreL the Plugin should not be used.

				// Add context menu items to the object tree's view and procedure nodes.
				IObjectTreeAPI otApi = session.getSessionInternalFrame().getObjectTreeAPI();
				otApi.addToPopup(DatabaseObjectType.VIEW, new ScriptDB2ViewAction(session.getApplication(),
					_resources, session));
				otApi.addToPopup(DatabaseObjectType.PROCEDURE, new ScriptDB2ProcedureAction(
					session.getApplication(), _resources, session));
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
