package de.heinzen.plugin.rulevalidation.ui;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.ComputationOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.heinzen.plugin.rulevalidation.RulesDataModel;
import de.prob.model.brules.ComputationStatus;
import de.prob.model.brules.RuleResult;
import de.prob.model.brules.RuleStatus;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;

import java.util.*;

/**
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 16.12.17
 */
class OperationItem extends TreeItem<Object> {

	private final RulesDataModel model;

	OperationItem(AbstractOperation operation, SimpleObjectProperty<Object> resultProperty, RulesDataModel model) {
		super(operation);
		this.model = model;
		resultProperty.addListener((observable, oldValue, newValue) -> {
			OperationItem.this.getChildren().clear();
			if (newValue instanceof RuleResult) {
				RuleResult ruleResult = (RuleResult) newValue;
				switch (ruleResult.getRuleState()) {
					case FAIL:
					case NOT_CHECKED:
						createRuleChildren(ruleResult);
						break;
					case DISABLED:
					case SUCCESS:
						OperationItem.this.getChildren().clear();
						break;
				}
			} else if (newValue instanceof Map.Entry && operation instanceof ComputationOperation) {
				createComputationChildren((Map.Entry)newValue, (ComputationOperation) operation);
			}
		});
	}

	private void createComputationChildren(Map.Entry result, ComputationOperation op) {
		String opName = (String) result.getKey();
		ComputationStatus state = (ComputationStatus) result.getValue();
		if (state == ComputationStatus.NOT_EXECUTED) {
			List<String> failedDependencies = model.getFailedDependenciesOfComputation(op.getName());
			List<String> notCheckedDependencies = model.getNotCheckedDependenciesOfComputation(op.getName());
			if (notCheckedDependencies.size() > 0) {
				TreeItem<Object> notCheckedItem = new TreeItem<>("UNCHECKED DEPENDENCIES");
				Collections.sort(notCheckedDependencies);
				for (String notChecked : notCheckedDependencies) {
					notCheckedItem.getChildren().add(new TreeItem<>(notChecked));
				}
				this.getChildren().add(notCheckedItem);
			}
			if (failedDependencies.size() > 0) {
				TreeItem<Object> failedItem = new TreeItem<>("FAILED DEPENDENCIES");
				Collections.sort(failedDependencies);
				for (String failed : failedDependencies) {
					failedItem.getChildren().add(new TreeItem<>(failed));
				}
				this.getChildren().add(failedItem);
			}
		}
	}

	private void createRuleChildren(RuleResult result) {
		switch(result.getRuleState()) {
			case FAIL:
				TreeItem<Object> violationItem = new TreeItem<>("VIOLATIONS");
				result.getCounterExamples().sort(Comparator.comparingInt(RuleResult.CounterExample::getErrorType));
				for (RuleResult.CounterExample example : result.getCounterExamples()) {
					violationItem.getChildren().add(new TreeItem<>(example));
				}
				this.getChildren().add(violationItem);
				break;
			case NOT_CHECKED:
				if (result.getNotCheckedDependencies().size() > 0) {
					TreeItem<Object> notCheckedItem = new TreeItem<>("UNCHECKED DEPENDENCIES");
					Collections.sort(result.getNotCheckedDependencies());
					for (String notChecked : result.getNotCheckedDependencies()) {
						notCheckedItem.getChildren().add(new TreeItem<>(notChecked));
					}
					this.getChildren().add(notCheckedItem);
				}
				if (result.getFailedDependencies().size() > 0) {
					TreeItem<Object> failedItem = new TreeItem<>("FAILED DEPENDENCIES");
					Collections.sort(result.getFailedDependencies());
					for (String failed : result.getFailedDependencies()) {
						failedItem.getChildren().add(new TreeItem<>(failed));
					}
					this.getChildren().add(failedItem);
				}
				break;
		}
	}


}
