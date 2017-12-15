package de.heinzen.plugin.rulevalidation.ui;

import de.be4.classicalb.core.parser.rules.ComputationOperation;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.model.brules.RuleResult;
import javafx.scene.control.TreeTableCell;

/**
 * Description of class
 *
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 14.12.17
 */
public class ValueCell extends TreeTableCell<Object, Object>{

		@Override
		protected void updateItem(Object item, boolean empty) {
			super.updateItem(item, empty);
			if (item == null || empty || item instanceof String)
				configureEmptyCell();
			else if (item instanceof RuleResult)
				configureForRuleResult((RuleResult)item);
			else if (item instanceof ComputationOperation)
				//TODO get correct Computation result
				setText("Testi McTestface");
			else if (item instanceof IdentifierNotInitialised)
				configureForNotInitialised((IdentifierNotInitialised)item);
			setGraphic(null);
		}

	private void configureEmptyCell() {
		setText(null);
		setStyle(null);
	}

	private void configureForRuleResult(RuleResult result) {
		setText(result.getResultValue());
		switch (result.getResultEnum()) {
			case FAIL:
				setStyle("-fx-background-color:pink");
				break;
			case SUCCESS:
				setStyle("-fx-background-color:palegreen");
				break;
			case NOT_CHECKED:
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
