package de.heinzen.plugin.rulevalidation.ui;

import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.model.brules.ComputationStatus;
import de.prob.model.brules.RuleResult;
import javafx.geometry.Pos;
import javafx.scene.control.TreeTableCell;

import java.util.Map;

/**
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 14.12.17
 */
public class ValueCell extends TreeTableCell<Object, Object>{

	ValueCell() {
		setAlignment(Pos.CENTER_LEFT);
	}

	@Override
	protected void updateItem(Object item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null || empty || item instanceof String)
			configureEmptyCell();
		else if (item instanceof RuleResult)
			configureForRuleResult((RuleResult)item);
		else if (item instanceof RuleResult.CounterExample)
			setText(((RuleResult.CounterExample)item).getMessage());
		else if (item instanceof Map.Entry)
			configureForComputationResult((ComputationStatus)((Map.Entry)item).getValue());
		else if (item instanceof IdentifierNotInitialised)
			configureForNotInitialised((IdentifierNotInitialised)item);
		setGraphic(null);
	}

	private void configureForComputationResult(ComputationStatus result) {
		setText(result.toString());
		switch (result) {
			case EXECUTED:
				setStyle("-fx-background-color:palegreen");
				break;
			case DISABLED:
				setStyle("-fx-background-color:lightgray");
				break;
			case NOT_EXECUTED:
				setStyle(null);
				break;
		}
	}

	private void configureEmptyCell() {
		setText(null);
		setStyle(null);
	}

	private void configureForRuleResult(RuleResult result) {
		setText(result.getRuleState().name());
		switch (result.getRuleState()) {
			case FAIL:
				setStyle("-fx-background-color:pink");
				break;
			case SUCCESS:
				setStyle("-fx-background-color:palegreen");
				break;
			case NOT_CHECKED:
				if (result.getFailedDependencies() != null && !result.getFailedDependencies().isEmpty()) {
					setText("NOT CHECKABLE");
				}
				setStyle(null);
				break;
			case DISABLED:
				setStyle("-fx-background-color:lightgray");
				break;
		}
	}

	private void configureForNotInitialised(IdentifierNotInitialised item) {
		setText(item.getResult());
		setStyle(null);
	}

}
