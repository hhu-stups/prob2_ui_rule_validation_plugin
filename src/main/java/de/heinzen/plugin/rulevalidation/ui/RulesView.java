package de.heinzen.plugin.rulevalidation.ui;


import de.be4.classicalb.core.parser.rules.AbstractOperation;
import de.be4.classicalb.core.parser.rules.ComputationOperation;
import de.be4.classicalb.core.parser.rules.RuleOperation;
import de.prob.animator.domainobjects.IdentifierNotInitialised;
import de.prob.model.brules.RuleResult;
import de.prob.model.brules.RuleResults;
import de.prob.model.brules.RulesModel;
import de.prob.statespace.Trace;
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

	@FXML
	private TextField filterTextField;

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
					//TODO
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
	}

	@FXML
	public void handleFilterButton(){



	}

	public void clear(){

		LOGGER.debug("clear RulesView!");

		tvRootItem.getChildren().clear();



	}

	public void build(RulesModel model) {

		LOGGER.debug("build RulesView!");

		this.model = model;

		Map<String, RuleOperation> rulesMap = new HashMap<>();
		Map<String, ComputationOperation> computationsMap = new HashMap<>();

		// sort operations by type
		for (Map.Entry<String, AbstractOperation> entry : model.getRulesProject().getOperationsMap().entrySet()) {
			if (entry.getValue() instanceof RuleOperation)
				rulesMap.put(entry.getKey(), (RuleOperation) entry.getValue());
			if (entry.getValue() instanceof ComputationOperation)
				computationsMap.put(entry.getKey(), (ComputationOperation) entry.getValue());
		}

		tvRulesItem = new TreeItem<>("RULES");
		tvComputationsItem = new TreeItem<>("COMPUTATIONS");
		if (!rulesMap.isEmpty())
			tvRootItem.getChildren().add(tvRulesItem);
		if (!computationsMap.isEmpty())
			tvRootItem.getChildren().add(tvComputationsItem);

		ruleValueMap = new HashMap<>();
		computationValueMap = new HashMap<>();
		tvRulesItem.getChildren().addAll(createItems(rulesMap, ruleValueMap));
		tvComputationsItem.getChildren().addAll(createItems(computationsMap, computationValueMap));
	}
	
	public void updateTreeTable(Trace currentTrace) {

		LOGGER.debug("update RulesView!");

		if (currentTrace.getCurrentState().isInitialised()) {
			RuleResults ruleResults = new RuleResults(model.getRulesProject(), currentTrace.getCurrentState(), 10); //TODO check number of counterexamples
			Map<String, RuleResult> ruleResultMap = ruleResults.getRuleResultMap();
			for (String ruleStr : ruleValueMap.keySet()) {
				ruleValueMap.get(ruleStr).set(ruleResultMap.get(ruleStr));
			}
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

	private <T> List<TreeItem<Object>> createItems(Map<String, T> operations, Map<String, SimpleObjectProperty<Object>> props){
		//sort
		List<String> sortedOperations = new ArrayList<>(operations.keySet());
		Collections.sort(sortedOperations);

		List<TreeItem<Object>> ret = new ArrayList<>(sortedOperations.size());
		for (String elementStr : sortedOperations) {
			TreeItem<Object> operationTreeItem = new TreeItem<>(operations.get(elementStr));
			props.put(elementStr, new SimpleObjectProperty<>(IDENTIFIER_NOT_INITIALISED));
			ret.add(operationTreeItem);
		}
		return ret;
	}
}
