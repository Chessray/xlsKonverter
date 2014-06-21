package de.hsk1830.klubturnier.xlskonverter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by kleinr on 18/05/2014.
 */
public class XlsxToHtmlKonverterApplication extends Application {

	private static final FileChooser.ExtensionFilter HTML_FILE_EXTENSION_FILTER = new FileChooser.ExtensionFilter("HTML-Dateien", "*.htm", "*.html");
	private static final FileChooser.ExtensionFilter XLSX_FILE_EXTENSION_FILTER = new FileChooser.ExtensionFilter("Excel 2010 Format", "*.xlsx");
	private static final Insets ROOT_PANE_CONTENT_INSETS = new Insets(5, 5, 5, 5);
	private static final Insets FILENAME_FIELD_INSETS = new Insets(2, 2, 2, 2);
	private final ObjectProperty<File> xlsxFileProperty;
	private final ObjectProperty<File> htmlTemplateFileProperty;
	private final TextField excelFileNameField;
	private final TextField htmlTemplateFilenameField;
	private static final String PREFERENCES_KEY_FOR_OUTPUT_DIRECTORY = "outputDirectory";

	public XlsxToHtmlKonverterApplication() {
		htmlTemplateFileProperty = new SimpleObjectProperty<>();
		xlsxFileProperty = new SimpleObjectProperty<>();
		excelFileNameField = createFilenameField();
		htmlTemplateFilenameField = createFilenameField();
		htmlTemplateFileProperty.addListener((observable, oldValue, newValue) -> htmlTemplateFilenameField.setText(newValue == null ? StringUtils.EMPTY : newValue.getName()));
		xlsxFileProperty.addListener((observable, oldValue, newValue) -> excelFileNameField.setText(newValue == null ? StringUtils.EMPTY : newValue.getName()));
	}

	private static void showSuccessPopup() {
		final Button okButton = new Button("OK");
		final Stage confirmationStage = new Stage();
		okButton.setOnAction(event -> confirmationStage.close());
		confirmationStage.setScene(new Scene(new BorderPane(new Label("Erfolgreich konvertiert"), null, null, new BorderPane(okButton), null)));
		confirmationStage.showAndWait();
	}

	private static void selectAndWriteOutputFile(DocumentProcessor htmlDocumentProcessor) throws IOException {
		final File saveFile = selectOutputFile();
		if (saveFile != null) {
			htmlDocumentProcessor.writeConvertedString(saveFile);
		}
	}

	private static File selectOutputFile() {
		final File saveFile = createOutputFileChooser().showSaveDialog(null);
		ApplicationPreferences.putParentIntoPreferencesIffFileNotNull(PREFERENCES_KEY_FOR_OUTPUT_DIRECTORY, saveFile);
		return saveFile;
	}

	private static FileChooser createOutputFileChooser() {
		final FileChooser outputFileChooser = new FileChooser();
		final File outputDirectory = ApplicationPreferences.getInitialDirectory(PREFERENCES_KEY_FOR_OUTPUT_DIRECTORY);
		if (outputDirectory != null) {
			outputFileChooser.setInitialDirectory(outputDirectory);
		}
		outputFileChooser.setTitle("Ausgabedatei wählen");
		outputFileChooser.getExtensionFilters().add(HTML_FILE_EXTENSION_FILTER);
		return outputFileChooser;
	}

	private static Node createFileChooserButton(final Window owner, final ObjectProperty<File> fileProperty, final String fileChooserTitle, final FileChooser.ExtensionFilter extensionFilter, final String preferencesKeyForDirectory) {
		final Button fileChooserButton = new Button(String.format("%1s...", fileChooserTitle));
		final File initialDirectory = ApplicationPreferences.getInitialDirectory(preferencesKeyForDirectory);

		fileChooserButton.setOnAction(event -> {
			final FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle(fileChooserTitle);
			if (initialDirectory != null) {
				fileChooser.setInitialDirectory(initialDirectory);
			}
			fileChooser.getExtensionFilters().add(extensionFilter);
			final File file = fileChooser.showOpenDialog(owner);
			ApplicationPreferences.putParentIntoPreferencesIffFileNotNull(preferencesKeyForDirectory, file);
			fileProperty.set(file);
		});
		return new BorderPane(fileChooserButton);
	}

	private static Pane createFilePane(final Window owner, final TextField filenameField, final ObjectProperty<File> fileProperty, final String fileChooserTitle, final FileChooser.ExtensionFilter extensionFilter, final String preferencesKeyForDirectory) {
		final BorderPane filePane = new BorderPane(filenameField, createFileChooserButton(owner, fileProperty, fileChooserTitle, extensionFilter, preferencesKeyForDirectory), null, null, null);
		BorderPane.setMargin(filenameField, FILENAME_FIELD_INSETS);
		return filePane;
	}

	private static TextField createFilenameField() {
		final TextField fileNameField = new TextField();
		fileNameField.setEditable(false);
		fileNameField.setMinWidth(200);
		return fileNameField;
	}

	public static void main(final String[] args) {
		launch(args);
	}

	private BorderPane buildRootPane(final Window owner) {
		final BorderPane root = new BorderPane(null, null, createHtmlTemplateFilePane(owner), createLaunchButtonPane(), createExcelFilePane(owner));
		BorderPane.setMargin(root.getLeft(), ROOT_PANE_CONTENT_INSETS);
		BorderPane.setMargin(root.getRight(), ROOT_PANE_CONTENT_INSETS);
		BorderPane.setMargin(root.getBottom(), ROOT_PANE_CONTENT_INSETS);
//		root.setOnDragOver((EventHandler<DragEvent>) dragEvent -> {
//			if (validateFileDragEvent(dragEvent)) {
//				dragEvent.acceptTransferModes(TransferMode.ANY);
//			}
//		});
//		root.setOnDragDropped((EventHandler<DragEvent>) dragEvent -> {
//			if (validateFileDragEvent(dragEvent)) {
//				currentFile.setValue(dragEvent.getDragboard().getFiles().iterator()
//						.next());
//			}
//		});
		return root;
	}

	private Node createLaunchButtonPane() {
		final Button launchButton = new Button("Starte Konvertierung");
		launchButton.setOnAction(event -> launchConversion());
		enableButtonIffBothFilesAreChosen(launchButton);
		htmlTemplateFileProperty.addListener((observable, oldValue, newValue) -> enableButtonIffBothFilesAreChosen(launchButton));
		xlsxFileProperty.addListener((observable, oldValue, newValue) -> enableButtonIffBothFilesAreChosen(launchButton));
		return new BorderPane(launchButton);
	}

	private void enableButtonIffBothFilesAreChosen(final Button launchButton) {
		launchButton.setDisable(htmlTemplateFileProperty.get() == null || xlsxFileProperty.get() == null);
	}

	private void launchConversion() {

		try {
			final DocumentProcessor htmlDocumentProcessor = new DocumentProcessor(Objects.requireNonNull(htmlTemplateFileProperty.get()));
			final List<String> tableColumnHeaders = htmlDocumentProcessor.getTableColumnHeaders();

			htmlDocumentProcessor.addSpielerDataToTableElement(tableColumnHeaders, new WorkbookProcessor(xlsxFileProperty.get()).getSpielerList(tableColumnHeaders));

			selectAndWriteOutputFile(htmlDocumentProcessor);

			showSuccessPopup();

			Platform.exit();
		} catch (IOException e) {
			// TODO
			e.printStackTrace();
		}
	}

	private Pane createHtmlTemplateFilePane(final Window owner) {
		return createFilePane(owner, htmlTemplateFilenameField, htmlTemplateFileProperty, "HTML-Vorlage auswählen", HTML_FILE_EXTENSION_FILTER, "xlsxDirectory");
	}

	private Pane createExcelFilePane(final Window owner) {
		return createFilePane(owner, excelFileNameField, xlsxFileProperty, "Exceldatei auswählen", XLSX_FILE_EXTENSION_FILTER, "htmlTemplateDirectory");
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		primaryStage.setTitle("HSK Klubturnier - Excel-Html-Konverter");
		primaryStage.setScene(new Scene(buildRootPane(primaryStage)));
		primaryStage.show();
	}
}
