module JavafxJdbc {
	requires javafx.controls;
	requires javafx.fxml;
	
	opens application to javafx.graphics, javafx.fxml;
	opens gui to javafx.graphics, javafx.fxml;
	opens entities to javafx.graphics, javafx.fxml;
	
	exports entities;
}
