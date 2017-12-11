package de.heinzen.plugin.rulevalidation;

import de.heinzen.plugin.rulevalidation.ui.RulesView;
import de.prob.model.brules.RulesMachineRun;
import de.prob2.ui.plugin.ProBPlugin;
import de.prob2.ui.plugin.ProBPluginManager;
import de.prob2.ui.plugin.ProBPluginUIConnection;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginWrapper;

import java.net.URL;

/**
 * Description of class
 *
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 11.12.17
 */
public class RulevalidationPlugin extends ProBPlugin{


	private static final Logger LOGGER = LoggerFactory.getLogger(RulevalidationPlugin.class);
	private Tab rulesTab;
	private RulesView rulesView;

	public RulevalidationPlugin(PluginWrapper wrapper, ProBPluginManager manager, ProBPluginUIConnection uiConnection) {
		super(wrapper, manager, uiConnection);
	}

	@Override
	public String getName() {
		return "Rule Validation Language Plugin";
	}

	@Override
	public void startPlugin() {
		rulesTab = new Tab("Rules Machine");
		rulesView = new RulesView();
		loadFXML("fxml/rules_view.fxml", rulesView);
		rulesTab.setContent(rulesView);
		getProBPluginUIConnection().addTab(rulesTab);
	}

	@Override
	public void stopPlugin() {
		getProBPluginUIConnection().removeTab(rulesTab);
	}

	private <T> void loadFXML(String file, T controller) {
		try {
			URL viewURL = getClass().getClassLoader().getResource(file);
			LOGGER.debug("URL of fxml is {}.", viewURL);
			if (viewURL != null) {
				FXMLLoader loader = new FXMLLoader(viewURL);
				loader.setClassLoader(getWrapper().getPluginClassLoader());
				//loader.setController(controller);
				loader.setRoot(controller);
				loader.load();
			}
		} catch (Exception e) {
			LOGGER.error("Exception while loading the the {}.", file, e);
		}
	}

}
