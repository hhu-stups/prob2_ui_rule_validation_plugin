package de.heinzen.plugin.rulevalidation.ui;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.prob.model.brules.RuleResult;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;

import java.util.Collections;
import java.util.Comparator;

/**
 * Description of class
 *
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 16.12.17
 */
public class OperationItem extends TreeItem<Object> {


	public OperationItem(AbstractOperation operation, SimpleObjectProperty<Object> resultProperty) {
		super(operation);
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
				//TODO check if we need to create items when the item is expanded
			}
		});
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
