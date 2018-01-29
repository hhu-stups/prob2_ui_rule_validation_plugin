package de.heinzen.plugin.rulevalidation.ui;

import de.heinzen.plugin.rulevalidation.RulesController;
import de.prob.model.brules.ComputationStatus;
import de.prob.model.brules.RuleResult;
import de.prob.model.brules.RuleStatus;
import de.prob2.ui.operations.OperationsView;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

import java.util.Map;

/**
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 20.12.17
 */
public class ExecutionCell extends TreeTableCell<Object, Object> {

	private final RulesController controller;
	private boolean executable;

	ExecutionCell(RulesController controller) {
		this.controller = controller;
		setAlignment(Pos.CENTER_LEFT);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void updateItem(Object item, boolean empty) {
		super.updateItem(item, empty);
		TreeItem<Object> treeItem = getTreeTableRow().getTreeItem();
		if (treeItem instanceof OperationItem) {
			executable = ((OperationItem) treeItem).isExecutable();
		}

		if (item instanceof RuleResult) {
			configureForRule((RuleResult) item);
		} else if (item instanceof Map.Entry) {
			configureForComputation((Map.Entry<String, ComputationStatus>)item);
		} else {
			setGraphic(null);
		}
	}

	private void configureForComputation(Map.Entry<String, ComputationStatus> resultEntry) {
		ComputationStatus result = resultEntry.getValue();
		String computation = resultEntry.getKey();
		if (result == ComputationStatus.NOT_EXECUTED && executable) {
			setGraphic(createLabel(computation));
		} else {
			setGraphic(null);
		}
	}

	private void configureForRule(RuleResult result) {
		if (result.getRuleState() == RuleStatus.NOT_CHECKED && executable) {
				//result.getFailedDependencies().isEmpty()) {
			setGraphic(createLabel(result.getRuleName()));
		} else {
			setGraphic(null);
		}
	}

	private Label createLabel(String operation) {
		Label label = new Label("Execute");
		label.setUnderline(true);
		label.setTextFill(Color.valueOf("#037875"));
		label.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
				controller.executeOperation(operation);
			}
		});
		return label;
	}
}
