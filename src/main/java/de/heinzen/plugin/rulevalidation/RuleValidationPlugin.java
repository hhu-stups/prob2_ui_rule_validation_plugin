package de.heinzen.plugin.rulevalidation;

import de.heinzen.plugin.rulevalidation.ui.RulesView;
import de.prob2.ui.layout.FontSize;
import de.prob2.ui.operations.OperationsView;
import de.prob2.ui.plugin.ProBPlugin;
import de.prob2.ui.plugin.ProBPluginHelper;
import de.prob2.ui.plugin.ProBPluginManager;
import de.prob2.ui.prob2fx.CurrentTrace;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TitledPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginWrapper;

import java.net.URL;
import java.util.Objects;

/**
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 11.12.17
 */
public class RuleValidationPlugin extends ProBPlugin{


	private static final Logger LOGGER = LoggerFactory.getLogger(RuleValidationPlugin.class);
	private final CurrentTrace currentTrace;

	private Tab rulesTab;
	private RulesView rulesView;

	private TitledPane operationsPane;
	private Accordion operationsAccordion;
	private int operationsPosition;
	private SplitPane operationsSplitPane;
	private int operationsAccordionPosition;
	private RulesController ruleController;

	public RuleValidationPlugin(PluginWrapper wrapper, ProBPluginManager manager, ProBPluginHelper helper) {
		super(wrapper, manager, helper);
		this.currentTrace = helper.getCurrentTrace();
	}

	@Override
	public String getName() {
		return "Rule Validation Language Plugin";
	}

	@Override
	public void startPlugin() {

		ruleController = new RulesController(getProBPluginHelper().getCurrentTrace());
		//add the tab
		createTab();
		//remove operations view
		OperationsView op = getInjector().getInstance(OperationsView.class);
		if (op.getParent() != null && op.getParent().getParent() instanceof TitledPane) {
			LOGGER.debug("Found that contains the OperationsView.");
			operationsPane = (TitledPane) op.getParent().getParent();
			if (operationsPane.getParent() != null && operationsPane.getParent() instanceof Accordion) {
				LOGGER.debug("Remove OperationsView from surrounding Accordion.");
				operationsAccordion = (Accordion) operationsPane.getParent();
				operationsPosition = operationsAccordion.getPanes().indexOf(operationsPane);
				operationsAccordion.getPanes().remove(operationsPane);
				if (operationsAccordion.getPanes().isEmpty() && operationsAccordion.getParent() != null
						&& operationsAccordion.getParent().getParent() instanceof SplitPane) {
					LOGGER.debug("Accordion is now empty -> remove Accordion.");
					operationsSplitPane = (SplitPane) operationsAccordion.getParent().getParent();
					operationsAccordionPosition = operationsSplitPane.getItems().indexOf(operationsAccordion);
					operationsSplitPane.getItems().remove(operationsAccordion);
				}
			}
		}
	}

	@Override
	public void stopPlugin() {
		LOGGER.debug("Remove Listener for the current Trace.");
		ruleController.stop();
		//remove tab
		getProBPluginHelper().removeTab(rulesTab);
		//restore OperationsView
		if (operationsAccordion != null && operationsPane != null) {
			LOGGER.debug("Add OperationsView to Accordion again.");
			operationsAccordion.getPanes().add(operationsPosition, operationsPane);
			if (operationsSplitPane != null) {
				LOGGER.debug("Accordion was also removed -> add it again.");
				operationsSplitPane.getItems().add(operationsAccordionPosition, operationsAccordion);
			}
		}
	}

	private void createTab(){
		rulesTab = new Tab("Rules Machine");
		rulesView = new RulesView(getInjector().getInstance(FontSize.class), ruleController);
		loadFXML("fxml/rules_view.fxml", rulesView);
		ruleController.setView(rulesView);
		rulesTab.setContent(rulesView);
		getProBPluginHelper().addTab(rulesTab);
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
