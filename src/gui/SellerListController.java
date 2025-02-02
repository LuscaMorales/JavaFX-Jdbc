package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
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
import model.entities.Seller;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerListController implements Initializable, DataChangeListener{
	
	private SellerService service;

	@FXML
	private TableView<Seller> tableViewSeller;
	
	@FXML
	private TableColumn<Seller, Integer> tableColumnId;
	
	@FXML
	private TableColumn<Seller, String> tableColumnName;
	
	@FXML
	private TableColumn<Seller, String> tableColumnEmail;
	
	@FXML
	private TableColumn<Seller, Date> tableColumnBirthDate;
	
	@FXML
	private TableColumn<Seller, Double> tableColumnBaseSalary;
	
	@FXML
	private TableColumn<Seller, Seller> tableColumnEDIT;
	
	@FXML
	private TableColumn<Seller, Seller> tableColumnREMOVE;
	
	@FXML
	private Button btNew;
	
	private ObservableList<Seller> obsList;
	
	@FXML
	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currentStage(event);
		Seller obj = new Seller();
		createDialogForm(obj, "/gui/SellerForm.fxml", parentStage);
	}
	
	public void setSellerService(SellerService service) {
		this.service = service;
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		tableColumnId.setCellValueFactory(new PropertyValueFactory<Seller, Integer>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<Seller, String>("name"));
		tableColumnEmail.setCellValueFactory(new PropertyValueFactory<Seller, String>("email"));
		tableColumnBirthDate.setCellValueFactory(new PropertyValueFactory<Seller, Date>("birthDate"));
		Utils.formatTableColumnnDate(tableColumnBirthDate, "dd/MM/yyyy");
		tableColumnBaseSalary.setCellValueFactory(new PropertyValueFactory<Seller, Double>("baseSalary"));
		Utils.formatTableColumnDouble(tableColumnBaseSalary, 2);
		Stage stage = (Stage)Main.getMainScene().getWindow();
		tableViewSeller.prefHeightProperty().bind(stage.heightProperty());
	}
	
	public void updateTableView() {
		if(service == null) {
			throw new IllegalStateException("Services was null");
		}
		List<Seller> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		tableViewSeller.setItems(obsList);
		initEditButtons();
		initRemoveButtons();
	}
	
	
	private void createDialogForm(Seller obj,String absoluteName, Stage parentStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			Pane pane = loader.load();
			
			//Controller da tela SellerFormController
			SellerFormController controller = loader.getController();
			//Instancia do objeto Seller para ser setado e assim ser adicionado ou atualizado
			controller.setSeller(obj);
			controller.setServices(new SellerService(), new DepartmentService());
			//Carregara a lista de departamentos abaixo
			controller.loadAssociatedObjects();
			controller.subscribeDataChangeListener(this);
			controller.updateFormData();
			
			
			//É preciso instanciar um novo stage para carregar uma nova tela e será o Dialog Stage
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Enter Seller Data");
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
			e.printStackTrace();
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
		tableColumnEDIT.setCellFactory(param -> new TableCell<Seller, Seller>(){
			private final Button button = new Button("edit");
			
			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				
				if(obj == null) {
					setGraphic(null);
					return;
				}
				
				setGraphic(button);
				//Ao clicar no botão ele já vai levar para a tela de UpdateSeller
				button.setOnAction(event -> {
					createDialogForm(obj, "/gui/SellerForm.fxml", Utils.currentStage(event));
				});
			}
		});
	}
	
	//Criação e ação do botão remove em cada linha da tabela
	private void initRemoveButtons() { 
		 tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue())); 
		 tableColumnREMOVE.setCellFactory(param -> new TableCell<Seller, Seller>() { 
		        private final Button button = new Button("remove"); 
		 
		        @Override 
		        protected void updateItem(Seller obj, boolean empty) { 
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

	public void removeEntity(Seller obj) {
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
