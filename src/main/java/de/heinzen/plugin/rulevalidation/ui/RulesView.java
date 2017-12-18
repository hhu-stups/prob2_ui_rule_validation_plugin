package de.heinzen.plugin.rulevalidation.ui;


import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.ComputationOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.model.brules.ComputationResults;
import de.prob.model.brules.RuleResult;
import de.prob.model.brules.RuleResults;
import de.prob.model.brules.RulesModel;
import de.prob.statespace.State;
import de.prob.statespace.Trace;
import de.prob2.ui.layout.FontSize;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


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
	private TreeItem<Object> tvRootItem;
	private TreeItem<Object> tvRulesItem;
	private TreeItem<Object> tvComputationsItem;

	private Map<String, SimpleObjectProperty<Object>> ruleValueMap;
	private Map<String, SimpleObjectProperty<Object>> computationValueMap;

	private RulesModel model;
	private List<TreeItem<Object>> ruleItems;
	private List<TreeItem<Object>> computationItems;

	public RulesView(FontSize fontsize) {
		super();
		this.fontsize = fontsize;
	}

	@FXML
	public void initialize() {

		tvNameColumn.setCellFactory(column -> new NameCell());
		tvNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));

		tvValueColumn.setCellFactory(column -> new ValueCell());
		tvValueColumn.setCellValueFactory(param -> {
			Object item = param.getValue().getValue();
			if (item instanceof RuleOperation) {
				return ruleValueMap.get(((RuleOperation) item).getName());
			} else if (item instanceof ComputationOperation) {
				return computationValueMap.get(((ComputationOperation) item).getName());
			} else if (item instanceof RuleResult.CounterExample) {
				return new ReadOnlyObjectWrapper<>(item);
			} else if (item instanceof String) {
				if (ruleValueMap.containsKey(item)) {
					return ruleValueMap.get(item);
				}
			}
			return null;
		} );

		FontAwesomeIconView buttonGraphic = ((FontAwesomeIconView) (filterButton.getGraphic()));
		buttonGraphic.setGlyphSize(fontsize.get());
		buttonGraphic.glyphSizeProperty().bind(fontsize);

	}

	@FXML
	public void handleFilterButton(){

		tvRootItem.getChildren().clear();
		tvRulesItem.getChildren().clear();
		tvComputationsItem.getChildren().clear();

		String filterText = filterTextField.getText();
		if (filterText != null && !filterText.isEmpty()) {
			//filter
			filterText = filterText.toLowerCase();
			List<TreeItem<Object>> filteredRules = filterItem(filterText, ruleItems);
			List<TreeItem<Object>> filteredComputations = filterItem(filterText, computationItems);
			if (!filteredRules.isEmpty()) {
				tvRulesItem.getChildren().addAll(filteredRules);
				tvRootItem.getChildren().add(tvRulesItem);
			}
			if (!filteredComputations.isEmpty()) {
				tvComputationsItem.getChildren().addAll(filteredComputations);
				tvRootItem.getChildren().add(tvComputationsItem);
			}
		} else {
			//don't filter, show all
			if (!ruleItems.isEmpty()) {
				tvRulesItem.getChildren().addAll(ruleItems);
				tvRootItem.getChildren().add(tvRulesItem);
			}
			if (!computationItems.isEmpty()) {
				tvComputationsItem.getChildren().addAll(computationItems);
				tvRootItem.getChildren().add(tvComputationsItem);
			}
		}
	}

	private List<TreeItem<Object>> filterItem(String filterText, List<TreeItem<Object>> allItems) {
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

		LOGGER.debug("clear RulesView!");

		tvRootItem.getChildren().clear();
		if (ruleValueMap != null) ruleValueMap = null;
		if (computationValueMap != null) computationValueMap = null;

		rulesLabel.setText("-");
		notCheckedLabel.setText("-");
		successLabel.setText("-");
		failLabel.setText("-");
		disabledLabel.setText("-");

		filterTextField.setText("");
	}

	public void build(RulesModel model) {

		LOGGER.debug("build RulesView!");

		this.model = model;

		Map<String, AbstractOperation> rulesMap = new HashMap<>();
		Map<String, AbstractOperation> computationsMap = new HashMap<>();

		// sort operations by type
		for (Map.Entry<String, AbstractOperation> entry : model.getRulesProject().getOperationsMap().entrySet()) {
			if (entry.getValue() instanceof RuleOperation)
				rulesMap.put(entry.getKey(), entry.getValue());
			if (entry.getValue() instanceof ComputationOperation)
				computationsMap.put(entry.getKey(), entry.getValue());
		}

		tvRulesItem = new TreeItem<>("RULES");
		tvComputationsItem = new TreeItem<>("COMPUTATIONS");
		if (!rulesMap.isEmpty())
			tvRootItem.getChildren().add(tvRulesItem);
		if (!computationsMap.isEmpty())
			tvRootItem.getChildren().add(tvComputationsItem);

		ruleValueMap = new HashMap<>();
		computationValueMap = new HashMap<>();
		ruleItems = createItems(rulesMap, ruleValueMap);
		computationItems = createItems(computationsMap, computationValueMap);
		tvRulesItem.getChildren().addAll(ruleItems);
		tvComputationsItem.getChildren().addAll(computationItems);

		rulesLabel.setText(rulesMap.size() + "");
	}
	
	public void updateTreeTable(Trace currentTrace) {

		LOGGER.debug("update RulesView!");

		if (currentTrace.getCurrentState().isInitialised()) {
			updateRuleResults(currentTrace.getCurrentState());
			updateComputationResults(currentTrace.getCurrentState());
			//TODO get computations results
		} else {
			for (SimpleObjectProperty<Object> prop : ruleValueMap.values()) {
				prop.set(IDENTIFIER_NOT_INITIALISED);
			}
			for (SimpleObjectProperty<Object> prop : computationValueMap.values()) {
				prop.set(IDENTIFIER_NOT_INITIALISED);
			}
		}
		
	}

	private void updateRuleResults(State currentState) {
		RuleResults ruleResults = new RuleResults(model.getRulesProject(), currentState, 10); //TODO check number of counterexamples
		Map<String, RuleResult> ruleResultMap = ruleResults.getRuleResultMap();
		for (String ruleStr : ruleValueMap.keySet()) {
			ruleValueMap.get(ruleStr).set(ruleResultMap.get(ruleStr));
		}
		RuleResults.ResultSummary summary = ruleResults.getSummary();
		notCheckedLabel.setText(summary.numberOfRulesNotChecked + "");
		successLabel.setText(summary.numberOfRulesSucceeded + "");
		failLabel.setText(summary.numberOfRulesFailed + "");
		disabledLabel.setText(summary.numberOfRulesDisabled + "");
	}


	private void updateComputationResults(State currentState) {
		ComputationResults computationResults = new ComputationResults(model.getRulesProject(), currentState);
		for (String computation : computationValueMap.keySet()) {
			computationValueMap.get(computation).set(computationResults.getResult(computation));
		}
	}

	private List<TreeItem<Object>> createItems(Map<String, AbstractOperation> operations, Map<String, SimpleObjectProperty<Object>> props){
		//sort
		List<String> sortedOperations = new ArrayList<>(operations.keySet());
		Collections.sort(sortedOperations);

		List<TreeItem<Object>> ret = new ArrayList<>(sortedOperations.size());
		for (String elementStr : sortedOperations) {
			props.put(elementStr, new SimpleObjectProperty<>(IDENTIFIER_NOT_INITIALISED));
			TreeItem<Object> operationTreeItem = new OperationItem(operations.get(elementStr), props.get(elementStr));
			ret.add(operationTreeItem);
		}
		return ret;
	}
}
