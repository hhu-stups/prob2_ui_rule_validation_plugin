package de.heinzen.plugin.rulevalidation;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.ComputationOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.model.brules.ComputationResults;
import de.prob.model.brules.RuleResult;
import de.prob.model.brules.RuleResults;
import de.prob.model.brules.RulesModel;
import de.prob.statespace.State;
import de.prob.statespace.Trace;
import javafx.beans.property.SimpleObjectProperty;

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

	private Map<String, SimpleObjectProperty<Object>> ruleValueMap;
	private Map<String, SimpleObjectProperty<Object>> computationValueMap;
	private LinkedHashMap<String, RuleOperation> ruleMap;
	private LinkedHashMap<String, ComputationOperation> computationMap;

	private SimpleObjectProperty<String> failedRules = new SimpleObjectProperty<>("-");
	private SimpleObjectProperty<String> successRules = new SimpleObjectProperty<>("-");
	private SimpleObjectProperty<String> notCheckedRules = new SimpleObjectProperty<>("-");
	private SimpleObjectProperty<String> disabledRules = new SimpleObjectProperty<>("-");

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

	public SimpleObjectProperty<String> failedRulesProperty() {
		return failedRules;
	}

	public SimpleObjectProperty<String> successRulesProperty() {
		return successRules;
	}

	public SimpleObjectProperty<String> notCheckedRulesProperty() {
		return notCheckedRules;
	}

	public SimpleObjectProperty<String> disabledRulesProperty() {
		return disabledRules;
	}

	public void update(Trace newTrace) {
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

	public void initialize(RulesModel newModel) {
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
		for (String ruleStr : ruleValueMap.keySet()) {
			ruleValueMap.get(ruleStr).set(ruleResults.getRuleResultMap().get(ruleStr));
		}

		//update summary
		failedRules.set(ruleResults.getSummary().numberOfRulesFailed + "");
		successRules.set(ruleResults.getSummary().numberOfRulesSucceeded + "");
		notCheckedRules.set(ruleResults.getSummary().numberOfRulesNotChecked + "");
		disabledRules.set(ruleResults.getSummary().numberOfRulesDisabled + "");
	}

	private void updateComputationResults(State currentState) {
		ComputationResults computationResults = new ComputationResults(model.getRulesProject(), currentState);
		for (Map.Entry<String, ComputationResults.RESULT> computationResult : computationResults.getResults().entrySet()) {
			SimpleObjectProperty<Object> prop = computationValueMap.get(computationResult.getKey());
			if (prop != null) {
				prop.set(computationResult);
			}
		}
	}
}
