package de.heinzen.plugin.rulevalidation.ui;


import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.ComputationOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.heinzen.plugin.rulevalidation.RulesController;
import de.heinzen.plugin.rulevalidation.RulesDataModel;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.model.brules.RuleResult;
import de.prob2.ui.layout.FontSize;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Description of class
 *
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 11.12.17
 */
public class RulesView extends AnchorPane{

	private static final Logger LOGGER = LoggerFactory.getLogger(RulesView.class);

	private static final IdentifierNotInitialised IDENTIFIER_NOT_INITIALISED = new IdentifierNotInitialised(null);
	private final FontSize fontsize;

	@FXML
	private Button filterButton;

	@FXML
	private TextField filterTextField;

	@FXML
	private Label rulesLabel;

	@FXML
	private Label notCheckedLabel;

	@FXML
	private Label successLabel;

	@FXML
	private Label failLabel;

	@FXML
	private Label disabledLabel;

	@FXML
	private TreeTableView<Object> treeTableView;

	@FXML
	private TreeTableColumn<Object, Object> tvNameColumn;
	@FXML
	private TreeTableColumn<Object, Object> tvValueColumn;
	@FXML
	private TreeTableColumn<Object, Object> tvExecuteColumn;

	@FXML
	private TreeItem<Object> tvRootItem;
	private TreeItem<Object> tvRulesItem;
	private TreeItem<Object> tvComputationsItem;

	//private RulesModel model;
	private List<TreeItem<Object>> ruleItems;
	private List<TreeItem<Object>> computationItems;

	private RulesDataModel dataModel;
	private RulesController controller;

	public RulesView(FontSize fontsize, RulesController controller) {
		super();
		this.fontsize = fontsize;
		this.controller = controller;
		this.dataModel = controller.getModel();
	}

	@FXML
	public void initialize() {

		tvNameColumn.setCellFactory(column -> new NameCell());
		tvNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));

		tvValueColumn.setCellFactory(column -> new ValueCell());
		tvValueColumn.setCellValueFactory(param -> {
			Object item = param.getValue().getValue();
			if (item instanceof RuleOperation) {
				return dataModel.getRuleValue(((RuleOperation) item).getName());
			} else if (item instanceof ComputationOperation) {
				return dataModel.getComputationValue(((ComputationOperation) item).getName());
			} else if (item instanceof RuleResult.CounterExample) {
				return new ReadOnlyObjectWrapper<>(item);
			} else if (item instanceof String) {
				if (dataModel.getRuleValueMap().containsKey(item)) {
					return dataModel.getRuleValue((String)item);
				}
			}
			return null;
		} );

		tvExecuteColumn.setCellFactory(column -> new ExecutionCell(fontsize, controller));
		tvExecuteColumn.setCellValueFactory(param -> {
			Object item = param.getValue().getValue();
			if (item instanceof RuleOperation) {
				return dataModel.getRuleValue(((RuleOperation) item).getName());
			} else if (item instanceof ComputationOperation) {
				return dataModel.getComputationValue(((ComputationOperation) item).getName());
			}
			return null;
		});

		FontAwesomeIconView buttonGraphic = ((FontAwesomeIconView) (filterButton.getGraphic()));
		buttonGraphic.setGlyphSize(fontsize.get());
		buttonGraphic.glyphSizeProperty().bind(fontsize);
	}

	@FXML
	public void handleFilterButton(){

		LOGGER.debug("Filter Operations");

		tvRootItem.getChildren().clear();
		tvRulesItem.getChildren().clear();
		tvComputationsItem.getChildren().clear();

		String filterText = filterTextField.getText();
		List<TreeItem<Object>> rulesToShow = null;
		List<TreeItem<Object>> computationsToShow = null;
		if (filterText != null && !filterText.isEmpty()) {
			//filter
			filterText = filterText.toLowerCase();
			rulesToShow = filterItems(filterText, ruleItems);
			computationsToShow = filterItems(filterText, computationItems);
		} else {
			//don't filter, show all
			rulesToShow = ruleItems;
			computationsToShow = computationItems;
		}
		if (!rulesToShow.isEmpty()) {
			tvRulesItem.getChildren().addAll(rulesToShow);
			tvRootItem.getChildren().add(tvRulesItem);
		}
		if (!computationsToShow.isEmpty()) {
			tvComputationsItem.getChildren().addAll(computationsToShow);
			tvRootItem.getChildren().add(tvComputationsItem);
		}
	}

	private List<TreeItem<Object>> filterItems(String filterText, List<TreeItem<Object>> allItems) {
		List<TreeItem<Object>> filtered = new ArrayList<>();
		for (TreeItem<Object> item : allItems) {
			String itemName = ((AbstractOperation) item.getValue()).getName().toLowerCase();
			if (itemName.contains(filterText)) {
				filtered.add(item);
			}
		}
		return filtered;
	}

	@FXML
	public void export(){



	}

	public void clear(){

		LOGGER.debug("Clear RulesView!");

		tvRootItem.getChildren().clear();

		rulesLabel.setText("-");
		notCheckedLabel.setText("-");
		successLabel.setText("-");
		failLabel.setText("-");
		disabledLabel.setText("-");

		filterTextField.setText("");
	}

	public void build() {

		LOGGER.debug("Build RulesView!");

		if (!dataModel.getRuleMap().isEmpty()) {
			tvRulesItem = new TreeItem<>("RULES");
			for (Map.Entry<String, RuleOperation> entry : dataModel.getRuleMap().entrySet()) {
				LOGGER.debug("Add item for rule " + entry.getKey() + "   " + entry.getValue());
				tvRulesItem.getChildren()
						.add(new OperationItem(entry.getValue(), dataModel.getRuleValue(entry.getKey())));
			}
			tvRootItem.getChildren().add(tvRulesItem);
		}
		if (!dataModel.getComputationMap().isEmpty()) {
			tvComputationsItem = new TreeItem<>("COMPUTATIONS");
			for (Map.Entry<String, ComputationOperation> entry : dataModel.getComputationMap().entrySet()) {
				LOGGER.debug("Add item for computation " + entry.getKey());
				tvComputationsItem.getChildren()
						.add(new OperationItem(entry.getValue(), dataModel.getComputationValue(entry.getKey())));
			}
			tvRootItem.getChildren().add(tvComputationsItem);
		}

		ruleItems = new ArrayList<>(tvRulesItem.getChildren());
		computationItems = new ArrayList<>(tvComputationsItem.getChildren());

		rulesLabel.setText(dataModel.getRuleMap().size() + "");
		disabledLabel.textProperty().bind(dataModel.disabledRulesProperty());
		failLabel.textProperty().bind(dataModel.failedRulesProperty());
		notCheckedLabel.textProperty().bind(dataModel.notCheckedRulesProperty());
		successLabel.textProperty().bind(dataModel.successRulesProperty());

	}
}
