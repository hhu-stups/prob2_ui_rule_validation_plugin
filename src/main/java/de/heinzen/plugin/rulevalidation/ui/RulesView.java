package de.heinzen.plugin.rulevalidation.ui;


import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.ComputationOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.model.brules.RuleResult;
import de.prob.model.brules.RuleResults;
import de.prob.model.brules.RulesModel;
import de.prob.statespace.Trace;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
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

	@FXML
	public void initialize() {
		tvNameColumn.setCellFactory(column -> new TreeTableCell<Object, Object>(){
			@Override
			protected void updateItem(Object item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty)
					setText(null);
				else if (item instanceof String)
					setText((String)item);
				else if (item instanceof AbstractOperation)
					setText(((AbstractOperation)item).getName());
				LOGGER.debug("Text is: " + getText());
				setGraphic(null);
			}
		});
		tvNameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue()));

		tvValueColumn.setCellFactory(column -> new TreeTableCell<Object, Object>(){
			@Override
			protected void updateItem(Object item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty || item instanceof String)
					setText(null);
				else if (item instanceof RuleResult)
					setText(((RuleResult)item).getResultValue());
				else if (item instanceof ComputationOperation)
					setText("Testi McTestface");
				else if (item instanceof IdentifierNotInitialised)
					setText(((IdentifierNotInitialised)item).getResult());
				setGraphic(null);
			}
		});
		tvValueColumn.setCellValueFactory(param -> {
			Object item = param.getValue().getValue();
			if (item instanceof RuleOperation) {
				return ruleValueMap.get(((RuleOperation) item).getName());
			} else if (item instanceof ComputationOperation) {
				return computationValueMap.get(((ComputationOperation) item).getName());
			}
			return null;
		} );

		createRootItems();

	}

	@FXML
	public void handleFilterButton(){

	}

	public void clear(){

	}

	public void build(RulesModel model, Trace currentTrace) {

		Map<String, RuleOperation> rulesMap = new LinkedHashMap<>();
		Map<String, ComputationOperation> computationMap = new LinkedHashMap<>();

		// sort operations by type
		for (Map.Entry<String, AbstractOperation> entry : model.getRulesProject().getOperationsMap().entrySet()) {
			if (entry.getValue() instanceof RuleOperation)
				rulesMap.put(entry.getKey(), (RuleOperation) entry.getValue());
			if (entry.getValue() instanceof ComputationOperation)
				computationMap.put(entry.getKey(), (ComputationOperation) entry.getValue());
		}

		//sort operations by name
		List<String> sortedRules = new ArrayList<>(rulesMap.keySet());
		Collections.sort(sortedRules);
		List<String> sortedComputations = new ArrayList<>(computationMap.keySet());
		Collections.sort(sortedComputations);

		//get results
		Map ruleResultMap = null;
		Map computationResultMap = null;
		if (currentTrace.getCurrentState().isInitialised()) {
			RuleResults ruleResults = new RuleResults(model.getRulesProject(), currentTrace.getCurrentState(), 10); //TODO check number of counterexamples
			ruleResultMap = ruleResults.getRuleResultMap();
			//TODO get computation results
		}


		ruleValueMap = new HashMap<>();
		for (String ruleStr : sortedRules) {
			TreeItem<Object> ruleTreeItem = new TreeItem<>(rulesMap.get(ruleStr));
			if (ruleResultMap != null) {
				ruleValueMap.put(ruleStr, new SimpleObjectProperty<>(ruleResultMap.get(ruleStr)));
			} else {
				ruleValueMap.put(ruleStr, new SimpleObjectProperty<>(new IdentifierNotInitialised(null)));
			}
			tvRulesItem.getChildren().add(ruleTreeItem);
		}

		computationValueMap = new HashMap<>();
		for (String computationStr : sortedComputations) {
			TreeItem<Object> computationTreeItem = new TreeItem<>(computationMap.get(computationStr));
			if (computationResultMap != null) {
				computationValueMap.put(computationStr, new SimpleObjectProperty<>(ruleResultMap.get(computationStr)));
			} else {
				computationValueMap.put(computationStr, new SimpleObjectProperty<>(new IdentifierNotInitialised(null)));
			}
			tvComputationsItem.getChildren().add(computationTreeItem);
		}

	}
	
	public void updateTreeTable(RulesModel model, Trace currentTrace) {

		if (currentTrace.getCurrentState().isInitialised()) {
			RuleResults ruleResults = new RuleResults(model.getRulesProject(), currentTrace.getCurrentState(), 10); //TODO check number of counterexamples
			Map<String, RuleResult> ruleResultMap = ruleResults.getRuleResultMap();
			for (String ruleStr : ruleValueMap.keySet()) {
				ruleValueMap.get(ruleStr).set(ruleResultMap.get(ruleStr));
			}
		} else {
			for (String ruleStr : ruleValueMap.keySet()) {
				ruleValueMap.get(ruleStr).set(new IdentifierNotInitialised(null));
			}
		}
		
	}

	private void createRootItems() {
		tvRulesItem = new TreeItem<>("RULES");
		tvComputationsItem = new TreeItem<>("COMPUTATIONS");
		tvRootItem.getChildren().add(tvRulesItem);
		tvRootItem.getChildren().add(tvComputationsItem);
	}


}
