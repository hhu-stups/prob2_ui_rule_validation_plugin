package de.heinzen.plugin.rulevalidation;

import de.heinzen.plugin.rulevalidation.ui.RulesView;
import de.prob2.ui.layout.FontSize;
import de.prob2.ui.operations.OperationsView;
import de.prob2.ui.plugin.ProBPlugin;
import de.prob2.ui.plugin.ProBPluginHelper;
import de.prob2.ui.plugin.ProBPluginManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.fortsoft.pf4j.PluginWrapper;

import java.net.URL;

/**
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 11.12.17
 */
public class RuleValidationPlugin extends ProBPlugin{


	private static final Logger LOGGER = LoggerFactory.getLogger(RuleValidationPlugin.class);

	private Tab rulesTab;
	private RulesController ruleController;

	private TitledPane operationsPane;
	private Accordion operationsAccordion;
	private int operationsPosition;
	private SplitPane operationsSplitPane;
	private int operationsAccordionPosition;
	private double[] operationsSplitPaneDivider;
	private Menu menu;
	private MenuItem opViewMenuItem;

	public RuleValidationPlugin(PluginWrapper wrapper, ProBPluginManager manager, ProBPluginHelper helper) {
		super(wrapper, manager, helper);
	}

	@Override
	public String getName() {
		return "Rule Validation Language Plugin";
	}

	@Override
	public void startPlugin() {
		//add menu
		createMenu();
		ruleController = new RulesController(getProBPluginHelper().getCurrentTrace(), this,
				getProBPluginHelper().getStageManager());
		//add the tab
		createTab();

	}

	@Override
	public void stopPlugin() {
		LOGGER.debug("Remove Listener for the current Trace.");
		ruleController.stop();
		// remove tab
		getProBPluginHelper().removeTab(rulesTab);
		// make sure that the op view will be restored
		restoreOperationsView(true);
		// remove menu item
		getProBPluginHelper().removeMenu(menu);
	}

	private void createMenu() {
		menu = new Menu("Rule Lang");
		opViewMenuItem = new MenuItem("Restore Operations View");
		opViewMenuItem.setOnAction(event -> {
			if (operationsPane == null) {
				removeOperationsView();
			} else {
				restoreOperationsView(false);
			}
		});
		menu.getItems().add(opViewMenuItem);
		getProBPluginHelper().addMenu(menu);
	}

	private void createTab(){
		rulesTab = new Tab("Rules Machine");
		RulesView rulesView = loadView();
		ruleController.setView(rulesView);
		rulesTab.setContent(rulesView);
		getProBPluginHelper().addTab(rulesTab);
	}

	private RulesView loadView() {
		try {
			URL viewURL = getClass().getClassLoader().getResource("fxml/rules_view.fxml");
			RulesView rulesView = new RulesView(getInjector().getInstance(FontSize.class), ruleController);
			LOGGER.debug("URL of fxml is {}.", viewURL);
			if (viewURL != null) {
				FXMLLoader loader = new FXMLLoader(viewURL);
				loader.setClassLoader(getWrapper().getPluginClassLoader());
				loader.setController(rulesView);
				loader.setRoot(rulesView);
				return loader.load();
			}
		} catch (Exception e) {
			LOGGER.error("Exception while loading the RulesView.", e);
		}
		return null;
	}

	void removeOperationsView() {
		// only remove the op view if it is not already removed
		if (operationsPane == null) {
			//remove operations view
			OperationsView op = getInjector().getInstance(OperationsView.class);
			operationsPane = getParent(op, TitledPane.class);
			if (operationsPane != null) {
				LOGGER.debug("Found pane that contains the OperationsView.");
				operationsAccordion = getParent(operationsPane, Accordion.class);
				if (operationsAccordion != null) {
					operationsPosition = operationsAccordion.getPanes().indexOf(operationsPane);
					operationsAccordion.getPanes().remove(operationsPane);
					LOGGER.debug("Removed OperationsView from surrounding Accordion.");
					opViewMenuItem.setText("Restore Operations View");
					opViewMenuItem.setDisable(false);
				}
			}
			//if Accordion is empty remove it
			if (operationsAccordion != null && operationsAccordion.getPanes().isEmpty()) {
				LOGGER.debug("Accordion is now empty -> remove Accordion.");
				operationsSplitPane = getParent(operationsAccordion, SplitPane.class);
				if (operationsSplitPane != null) {
					operationsAccordionPosition = operationsSplitPane.getItems().indexOf(operationsAccordion);
					operationsSplitPaneDivider = operationsSplitPane.getDividerPositions();
					operationsSplitPane.getItems().remove(operationsAccordion);
				}

			}
		}
	}

	void restoreOperationsView(boolean menuDisable) {
		//restore OperationsView
		if (operationsAccordion != null && operationsPane != null) {
			LOGGER.debug("Add OperationsView to Accordion again.");
			operationsAccordion.getPanes().add(operationsPosition, operationsPane);
			if (operationsSplitPane != null) {
				LOGGER.debug("Accordion was also removed -> add it again.");
				operationsSplitPane.getItems().add(operationsAccordionPosition, operationsAccordion);
				operationsSplitPane.setDividerPositions(operationsSplitPaneDivider);
			}

		}
		opViewMenuItem.setText("Remove Operations View");
		opViewMenuItem.setDisable(menuDisable);
		operationsPane = null;
		operationsAccordion = null;
		operationsSplitPane = null;
	}

	private <T extends Parent> T getParent(Node child, Class<T> parentClazz) {
		if (child.getParent() != null) {
			if (child.getParent().getClass().equals(parentClazz)) {
				return (T) child.getParent();
			} else {
				return getParent(child.getParent(), parentClazz);
			}
		}
		return null;
	}

}
