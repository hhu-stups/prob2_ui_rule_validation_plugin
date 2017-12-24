package de.heinzen.plugin.rulevalidation.ui;

import de.heinzen.plugin.rulevalidation.RulesController;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.prob.model.brules.ComputationResults;
import de.prob.model.brules.RuleResult;

import de.prob2.ui.layout.FontSize;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TreeTableCell;

import java.util.Map;
import java.util.Map.Entry;

import static de.prob.model.brules.RuleState.NOT_CHECKED;

/**
 * Description of class
 *
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 20.12.17
 */
public class ExecutionCell extends TreeTableCell<Object, Object> {

	private final FontSize fontSize;
	private final RulesController controller;

	public ExecutionCell(FontSize fontSize, RulesController controller) {
		this.fontSize = fontSize;
		this.controller = controller;
		setAlignment(Pos.CENTER_LEFT);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void updateItem(Object item, boolean empty) {
		super.updateItem(item, empty);
		if (item instanceof RuleResult) {
			configureForRule((RuleResult) item);
		} else if (item instanceof Entry<?,?>) {
			configureForComputation((Entry<String, ComputationResults.RESULT>)item);
		} else {
			setGraphic(null);
		}
	}

	private void configureForComputation(Entry<String, ComputationResults.RESULT> resultEntry) {
		ComputationResults.RESULT result = resultEntry.getValue();
		String computation = resultEntry.getKey();
		if (result == ComputationResults.RESULT.NOT_EXECUTED) {
			Button btn = createButton(computation);
			setGraphic(btn);
		} else {
			setGraphic(null);
		}
	}

	private void configureForRule(RuleResult result) {
		if (result.getRuleState() == NOT_CHECKED &&
				result.getFailedDependencies().isEmpty()) {
			Button btn = createButton(result.getRuleName());
			setGraphic(btn);
		} else {
			setGraphic(null);
		}
	}

	private Button createButton(String operation) {
		Button btn = new Button();

		FontAwesomeIconView buttonGraphic = new FontAwesomeIconView(FontAwesomeIcon.PLAY);
		buttonGraphic.setGlyphSize(fontSize.get());
		buttonGraphic.glyphSizeProperty().bind(fontSize);

		btn.setGraphic(buttonGraphic);
		btn.setStyle("-fx-background-color: #037875");
		btn.setOnAction(event -> controller.executeOperation(operation));
		return btn;
	}
}