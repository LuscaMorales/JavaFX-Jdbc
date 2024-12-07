package gui.util;

import javafx.scene.control.TextField;

public class Constraints {

	//Verificador se é inteiro e não nulo
		public static void setTextFieldInteger(TextField txt) {
			txt.textProperty().addListener((obs, oldValue, newValue) -> {
				if(newValue != null && !newValue.matches("\\d*")) {
					txt.setText(oldValue);
				}
			});
		}
		
		//Verificador se é nulo ou passa do limite 
		public static void setTextFieldMaxLength(TextField txt, int max) {
			txt.textProperty().addListener((obs, oldValue, newValue) -> {
				if(newValue != null && newValue.length() > max) {
					txt.setText(oldValue);
				}
			});
		}
		
		//Verificador se é nulo ou double
		public static void setTextFieldDouble(TextField txt) {
			txt.textProperty().addListener((obs, oldValue, newValue) -> {
				if(newValue != null && !newValue.matches("\\d*([\\.]\\d*)?")) {
					txt.setText(oldValue);
				}
			});
		}
}
