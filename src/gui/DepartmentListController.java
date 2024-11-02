package gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Department;
import model.services.DepartmentService;

public class DepartmentListController implements Initializable, DataChangeListener{
	
	private DepartmentService service;

	@FXML
	private TableView<Department> tableViewDepartment;
	
	@FXML
	private TableColumn<Department, Integer> tableColumnId;
	
	@FXML
	private TableColumn<Department, String> tableColumnName;
	
	@FXML
	private TableColumn<Department, Department> tableColumnEDIT;
	
	@FXML
	private TableColumn<Department, Department> tableColumnREMOVE;
	
	@FXML
	private Button btNew;
	
	private ObservableList<Department> obsList;
	
	@FXML
	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currentStage(event);
		Department obj = new Department();
		createDialogForm(obj, "/gui/DepartmentForm.fxml", parentStage);
	}
	
	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		tableColumnId.setCellValueFactory(new PropertyValueFactory<Department, Integer>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<Department, String>("name"));
		Stage stage = (Stage)Main.getMainScene().getWindow();
		tableViewDepartment.prefHeightProperty().bind(stage.heightProperty());
	}
	
	public void updateTableView() {
		if(service == null) {
			throw new IllegalStateException("Services was null");
		}
		List<Department> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		tableViewDepartment.setItems(obsList);
		initEditButtons();
		initRemoveButtons();
	}
	
	private void createDialogForm(Department obj,String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();
			
			//Controller da tela DepartmentFormController
			DepartmentFormController controller = loader.getController();
			//Instancia do objeto Department para ser setado e assim ser adicionado ou atualizado
			controller.setDepartment(obj);
			controller.setDepartmentService(new DepartmentService());
			controller.subscribeDataChangeListener(this);
			controller.updateFormData();
			
			
			//É preciso instanciar um novo stage para carregar uma nova tela e será o Dialog Stage
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Enter Department Data");
			//Como é um novo stage terá que receber uma nova cena
			dialogStage.setScene(new Scene(pane));
			//setResizable diz se a janela pode ou não ser redimensionada, sendo não possível
			dialogStage.setResizable(false);
			//A funçao initOwner diz quem é o pai do Stage atual
			dialogStage.initOwner(parentStage);
			//initModality indica qual a modalidade, WindowModal indica que só podera retornar se estiver fechada
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();
		}catch(IOException e){
			Alerts.ShowAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChanged() {
		updateTableView();
	}
	
	
	//Criação do botão EDIT em cada linha da tabela
	private void initEditButtons() {
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Department, Department>(){
			private final Button button = new Button("edit");
			
			@Override
			protected void updateItem(Department obj, boolean empty) {
				super.updateItem(obj, empty);
				
				if(obj == null) {
					setGraphic(null);
					return;
				}
				
				setGraphic(button);
				//Ao clicar no botão ele já vai levar para a tela de UpdateDepartment
				button.setOnAction(event -> {
					createDialogForm(obj, "/gui/DepartmentForm.fxml", Utils.currentStage(event));
				});
			}
		});
	}
	
	//Criação e ação do botão remove em cada linha da tabela
	private void initRemoveButtons() { 
		 tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue())); 
		 tableColumnREMOVE.setCellFactory(param -> new TableCell<Department, Department>() { 
		        private final Button button = new Button("remove"); 
		 
		        @Override 
		        protected void updateItem(Department obj, boolean empty) { 
		            super.updateItem(obj, empty); 
		 
		            if (obj == null) { 
		                setGraphic(null); 
		                return; 
		            }
		            //Criação do botão remove em todas as linhas
		            setGraphic(button); 
		            button.setOnAction(event -> removeEntity(obj)); 
		        } 
		    }); 
		}

	public void removeEntity(Department obj) {
		//Alert sendo chamado e Optional para pegar a entrada
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmation", "Are you sure to delete");
		
		if(result.get() == ButtonType.OK) {
			if(service == null) {
				throw new IllegalStateException("Service was null");
			}
			try {
				service.remove(obj);
				//Atualizar o dados da tela ao apagar
				updateTableView();
			} catch (DbIntegrityException e) {
				Alerts.ShowAlert("Error removing object", null, e.getMessage(), AlertType.ERROR);
			}
			
		}
	} 
}
