package de.heinzen.plugin.rulevalidation;

import de.heinzen.plugin.rulevalidation.ui.RulesView;
import de.prob.model.brules.RulesChecker;
import de.prob.model.brules.RulesModel;
import de.prob.statespace.Trace;
import de.prob2.ui.prob2fx.CurrentTrace;
import javafx.beans.value.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 20.12.17
 */
public class RulesController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RulesController.class);

	private final CurrentTrace currentTrace;
	private RulesModel ruleModel;

	private ChangeListener<Trace> traceListener;
	private RulesView rulesView;
	private final RulesDataModel model;

	public RulesController(final CurrentTrace currentTrace) {
		this.currentTrace = currentTrace;
		this.model = new RulesDataModel();

		traceListener = (observable, oldTrace, newTrace) -> {
			LOGGER.debug("Trace changed!");
			if (rulesView != null) {
				if (newTrace == null || !(newTrace.getModel() instanceof RulesModel)) {
					rulesView.clear();
					model.clear();
				} else if (oldTrace == null || !newTrace.getModel().equals(oldTrace.getModel())) {
					// the model changed -> rebuild view
					ruleModel = (RulesModel) newTrace.getModel();
					RulesChecker rulesChecker = new RulesChecker(newTrace);
					rulesChecker.init();
					initialize(ruleModel);
					model.update(rulesChecker.getCurrentTrace());
				} else {
					// model didn't change
					model.update(newTrace);
				}
			}
		};

		currentTrace.addListener(traceListener);
	}

	private void initialize(RulesModel newModel) {
		model.initialize(newModel);
		rulesView.build();
	}

	public void stop() {
		currentTrace.removeListener(traceListener);
	}

	public void setView(RulesView view) {
		this.rulesView = view;
		traceListener.changed(null, null, currentTrace.get());
	}

	public RulesDataModel getModel() {
		return model;
	}

	public void executeOperation(String operationName) {
		RulesChecker rulesChecker = new RulesChecker(currentTrace.get());
		rulesChecker.executeOperationAndDependencies(operationName);
		currentTrace.set(rulesChecker.getCurrentTrace());
	}

	public void executeAllOperations() {
		RulesChecker rulesChecker = new RulesChecker(currentTrace.get());
		rulesChecker.executeAllOperations();
		currentTrace.set(rulesChecker.getCurrentTrace());
	}
}
