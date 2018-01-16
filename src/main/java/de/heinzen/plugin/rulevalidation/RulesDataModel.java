package de.heinzen.plugin.rulevalidation;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.ComputationOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.model.brules.ComputationStatuses;
import de.prob.model.brules.RuleResult;
import de.prob.model.brules.RuleResults;
import de.prob.model.brules.RulesModel;
import de.prob.statespace.State;
import de.prob.statespace.Trace;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import java.util.*;

/**
 * Description of class
 *
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 20.12.17
 */
public class RulesDataModel {

	private static final IdentifierNotInitialised IDENTIFIER_NOT_INITIALISED = new IdentifierNotInitialised(null);

	private RulesModel model;

	// dynamic information about a rules machine
	private Map<String, SimpleObjectProperty<Object>> ruleValueMap;
	private Map<String, SimpleObjectProperty<Object>> computationValueMap;
	// static information about a rules machine
	private LinkedHashMap<String, RuleOperation> ruleMap;
	private LinkedHashMap<String, ComputationOperation> computationMap;

	// Summary properties
	private SimpleStringProperty failedRules = new SimpleStringProperty("-");
	private SimpleStringProperty successRules = new SimpleStringProperty("-");
	private SimpleStringProperty notCheckedRules = new SimpleStringProperty("-");
	private SimpleStringProperty disabledRules = new SimpleStringProperty("-");

	// Methods to access properties
	public Map<String, SimpleObjectProperty<Object>> getRuleValueMap() {
		return ruleValueMap;
	}
	public SimpleObjectProperty<Object> getRuleValue(String rule) {
		return ruleValueMap.get(rule);
	}
	public LinkedHashMap<String, RuleOperation> getRuleMap() {
		return ruleMap;
	}
	public LinkedHashMap<String, ComputationOperation> getComputationMap() {
		return computationMap;
	}
	public SimpleObjectProperty<Object> getComputationValue(String computation) {
		return computationValueMap.get(computation);
	}
	public SimpleStringProperty failedRulesProperty() {
		return failedRules;
	}
	public SimpleStringProperty successRulesProperty() {
		return successRules;
	}
	public SimpleStringProperty notCheckedRulesProperty() {
		return notCheckedRules;
	}
	public SimpleStringProperty disabledRulesProperty() {
		return disabledRules;
	}

	void update(Trace newTrace) {
		if (newTrace.getCurrentState().isInitialised()) {
			updateRuleResults(newTrace.getCurrentState());
			updateComputationResults(newTrace.getCurrentState());
		} else {
			for (SimpleObjectProperty<Object> prop : ruleValueMap.values()) {
				prop.set(IDENTIFIER_NOT_INITIALISED);
			}
			for (SimpleObjectProperty<Object> prop : computationValueMap.values()) {
				prop.set(IDENTIFIER_NOT_INITIALISED);
			}
		}
	}

	void initialize(RulesModel newModel) {
		this.model = newModel;

		Map<String, RuleOperation> rulesMap = new HashMap<>();
		Map<String, ComputationOperation> computationsMap = new HashMap<>();

		for (Map.Entry<String, AbstractOperation> entry : model.getRulesProject().getOperationsMap().entrySet()) {
			if (entry.getValue() instanceof RuleOperation)
				rulesMap.put(entry.getKey(), (RuleOperation) entry.getValue());
			if (entry.getValue() instanceof ComputationOperation)
				computationsMap.put(entry.getKey(), (ComputationOperation) entry.getValue());
		}

		ruleValueMap = new LinkedHashMap<>(rulesMap.size());
		ruleMap = new LinkedHashMap<>(rulesMap.size());
		initializeValueMap(rulesMap, ruleMap, ruleValueMap);

		computationValueMap = new LinkedHashMap<>(computationsMap.size());
		computationMap = new LinkedHashMap<>(computationsMap.size());
		initializeValueMap(computationsMap, computationMap, computationValueMap);
	}

	private <T> void initializeValueMap(Map<String, T> operations,
									Map<String, T> operationsMap,
									Map<String, SimpleObjectProperty<Object>> operationsValueMap) {
		//sort
		List<String> sortedOperations = new ArrayList<>(operations.keySet());
		Collections.sort(sortedOperations);

		for (String operation : sortedOperations) {
			operationsMap.put(operation, operations.get(operation));
			operationsValueMap.put(operation, new SimpleObjectProperty<>(IDENTIFIER_NOT_INITIALISED));
		}

	}

	private void updateRuleResults(State currentState) {
		RuleResults ruleResults = new RuleResults(model.getRulesProject(), currentState, 10); //TODO check number of counterexamples
		int notCheckableCounter = 0;
		for (String ruleStr : ruleValueMap.keySet()) {
			RuleResult result = ruleResults.getRuleResultMap().get(ruleStr);
			ruleValueMap.get(ruleStr).set(result);
			if (result.getFailedDependencies() != null && !result.getFailedDependencies().isEmpty()) {
				notCheckableCounter++;
			}
		}

		//update summary
		RuleResults.ResultSummary summary = ruleResults.getSummary();
		failedRules.set(summary.numberOfRulesFailed + "");
		successRules.set(summary.numberOfRulesSucceeded + "");
		notCheckedRules.set((summary.numberOfRulesNotChecked - notCheckableCounter) + " (" + notCheckableCounter + ")");
		disabledRules.set(summary.numberOfRulesDisabled + "");
	}

	private void updateComputationResults(State currentState) {
		ComputationStatuses computationResults = new ComputationStatuses(model.getRulesProject(), currentState);
		computationResults.getResults().entrySet().forEach(computationResult -> {
			SimpleObjectProperty<Object> prop = computationValueMap.get(computationResult.getKey());
			if (prop != null) {
				prop.set(computationResult);
			}
		});
	}

	void clear() {
		failedRules.set("-");
		successRules.set("-");
		notCheckedRules.set("-");
		disabledRules.set("-");
	}
}
