package de.heinzen.plugin.rulevalidation;

import de.heinzen.plugin.rulevalidation.ui.RulesView;
import de.prob.model.brules.RulesModel;
import de.prob.statespace.Trace;
import de.prob2.ui.plugin.ProBPlugin;
import de.prob2.ui.plugin.ProBPluginManager;
import de.prob2.ui.plugin.ProBPluginUIConnection;
import de.prob2.ui.prob2fx.CurrentTrace;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginWrapper;

import java.net.URL;
import java.util.Objects;

/**
 * Description of class
 *
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 11.12.17
 */
public class RuleValidationPlugin extends ProBPlugin{


	private static final Logger LOGGER = LoggerFactory.getLogger(RuleValidationPlugin.class);
	private final CurrentTrace currentTrace;

	private Tab rulesTab;
	private RulesView rulesView;

	private RulesModel rulesModel;
	private ChangeListener<Trace> traceListener;

	public RuleValidationPlugin(PluginWrapper wrapper, ProBPluginManager manager, ProBPluginUIConnection uiConnection) {
		super(wrapper, manager, uiConnection);
		this.currentTrace = uiConnection.getCurrentTrace();
	}

	@Override
	public String getName() {
		return "Rule Validation Language Plugin";
	}

	@Override
	public void startPlugin() {
		createTab();

		traceListener = (observable, oldTrace, newTrace) -> {
			LOGGER.debug("Trace changed!");
			if (newTrace == null || !(newTrace.getModel() instanceof RulesModel)) {
				rulesView.clear();
			} else if (oldTrace == null || !newTrace.getModel().equals(oldTrace.getModel())){
				//the model changed -> rebuild view
				rulesModel = (RulesModel) newTrace.getModel();
				rulesView.build((RulesModel) newTrace.getModel());
				rulesView.updateTreeTable(newTrace);
			} else {
				//model didn't change -> update view
				rulesView.updateTreeTable(newTrace);
			}
		};

		traceListener.changed(null, null, currentTrace.get());

		currentTrace.addListener(traceListener);
	}

	@Override
	public void stopPlugin() {
		getProBPluginUIConnection().removeTab(rulesTab);
		currentTrace.removeListener(traceListener);
	}

	private void createTab(){
		rulesTab = new Tab("Rules Machine");
		rulesView = new RulesView();
		loadFXML("fxml/rules_view.fxml", rulesView);
		rulesTab.setContent(rulesView);
		getProBPluginUIConnection().addTab(rulesTab);
	}

	private <T> T loadFXML(String file, T controller) {
		Objects.requireNonNull(file);
		Objects.requireNonNull(controller);
		try {
			URL viewURL = getClass().getClassLoader().getResource(file);
			LOGGER.debug("URL of fxml is {}.", viewURL);
			if (viewURL != null) {
				FXMLLoader loader = new FXMLLoader(viewURL);
				loader.setClassLoader(getWrapper().getPluginClassLoader());
				loader.setController(controller);
				loader.setRoot(controller);
				return loader.load();
			}
		} catch (Exception e) {
			LOGGER.error("Exception while loading the the {}.", file, e);
		}
		return null;
	}

}
