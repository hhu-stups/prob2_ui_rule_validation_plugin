package de.heinzen.plugin.rulevalidation.ui;


import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Description of class
 *
 * @author Christoph Heinzen
 * @version 0.1.0
 * @since 11.12.17
 */
public class RulesView extends AnchorPane{

	private static final Logger LOGGER = LoggerFactory.getLogger(RulesView.class);

	public RulesView() {

	}

	@FXML
	public void initialize() {
		LOGGER.debug("\n\n\n\n\n INITIALISATION \n\n\n\n\n");
	}

	@FXML
	public void handleFilterButton(){

	}

}
