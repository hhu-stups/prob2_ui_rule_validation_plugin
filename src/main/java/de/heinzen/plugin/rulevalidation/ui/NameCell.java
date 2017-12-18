package de.heinzen.plugin.rulevalidation.ui;

import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.prob.model.brules.RuleResult;
import javafx.scene.control.TreeTableCell;

/**
 * Description of class
 *
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 14.12.17
 */
public class NameCell extends TreeTableCell<Object, Object>{

	@Override
	protected void updateItem(Object item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null || empty)
			setText(null);
		else if (item instanceof String)
			setText((String)item);
		else if (item instanceof AbstractOperation)
			setText(((AbstractOperation)item).getName());
		else if (item instanceof RuleResult.CounterExample)
			setText(((RuleResult.CounterExample)item).getErrorType() + "");
		setGraphic(null);
	}
}
