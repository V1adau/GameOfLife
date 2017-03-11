package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Oscar_000 on 09.03.2017.
 */



public class PatternExportController implements Initializable {

    @FXML
    Canvas editorCanvas;
    @FXML
    TextField titleField;
    @FXML
    TextField authorField;
    @FXML
    TextField commentField;
    @FXML
    CheckBox dateCheckBox;


    private StaticBoard exportBoard;
    private GameOfLife gameOfLife;
    public double cellSize;
    private Color currentCellColor = Color.LIMEGREEN;
    private Color currentBackgroundColor = Color.LIGHTGRAY;
    private boolean erase = false;
    private byte[][] boardAtMousePressed;


    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        //drawEditorBoard();
    }

    public void drawEditorBoard() {
        GraphicsContext graphicsContext = editorCanvas.getGraphicsContext2D();
        graphicsContext.setFill(currentBackgroundColor);
        graphicsContext.fillRect(0,0,editorCanvas.getWidth(), editorCanvas.getHeight());
        graphicsContext.setFill(currentCellColor);
        //if (exportBoard != null) {
            cellSize = editorCanvas.getWidth() / exportBoard.boardGrid.length;


            for (int x = 0; x < exportBoard.boardGrid.length; x++) {
                for (int y = 0; y < exportBoard.boardGrid[0].length; y++) {
                    if (exportBoard.boardGrid[x][y] == 1) {
                        graphicsContext.fillRect(x * cellSize + 1, y * cellSize + 1, cellSize, cellSize);
                    }
                }
            }
        //}
    }

    public void closeClick(ActionEvent actionEvent) {
        Stage currentStage = (Stage) editorCanvas.getScene().getWindow();
        currentStage.close();
    }

    public void setExportBoard(StaticBoard board) {
        this.exportBoard = board;
    }

    public void setGameOfLife(GameOfLife gOL) {
        this.gameOfLife = gOL;
    }

    public void mousePressed(MouseEvent mouseEvent) {
        //Checks the x and y coordinates of the mouse-pointer and compares it to the current cell size to find the cell.
        int x = (int)(mouseEvent.getX()/cellSize);
        int y = (int)(mouseEvent.getY()/cellSize);

        //Checks that the user is within the playing board during click, and changes the cells if yes.
        if ((x < exportBoard.boardGrid.length) && (y < exportBoard.boardGrid[0].length) && x >= 0 && y >= 0) {

            //Sets global variable erase to true if the first clicked cell was alive.
            //If true, drag and click will only erase until the mouse is released. If false, method will only draw.
            if (exportBoard.boardGrid[x][y] == 1) {
                erase = true;
            }

            //Makes a copy of the board when the mouse button is pressed, stores as a global variable.
            boardAtMousePressed = new byte[exportBoard.boardGrid.length][exportBoard.boardGrid[0].length];
            for (int i = 0; i < boardAtMousePressed.length; i++) {
                for (int j = 0; j < boardAtMousePressed[i].length; j++) {
                    boardAtMousePressed[i][j] = exportBoard.boardGrid[i][j];
                }
            }

            if (exportBoard.boardGrid[x][y] == 0) {
                if (exportBoard.boardGrid[x][y] != 1) {
                    exportBoard.cellsAlive++;
                }
                exportBoard.boardGrid[x][y] = 1;
            } else {
                if (exportBoard.cellsAlive > 0 && exportBoard.boardGrid[x][y] == 1) {
                    exportBoard.cellsAlive--;
                }
                exportBoard.boardGrid[x][y] = 0;
            }
        }
        drawEditorBoard();
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        //Checks the x and y coordinates of the mouse-pointer and compares it to the current cell size to find the cell.
        int x = (int) (mouseEvent.getX() / cellSize);
        int y = (int) (mouseEvent.getY() / cellSize);

        //Checks that the user is drawing within the borders of the board.
        if ((x < exportBoard.boardGrid.length) && (y < exportBoard.boardGrid[0].length) && x >= 0 && y >= 0) {

            //Checks whether the current cell has been changed since the mouse button was pressed.
            if (exportBoard.boardGrid[x][y] == boardAtMousePressed[x][y]) {

                //If boolean erase is true, it sets the cell to 0. Else, sets the cell to 1.
                if (erase) {
                    if (exportBoard.cellsAlive > 0 && exportBoard.boardGrid[x][y] == 1) {
                        exportBoard.cellsAlive--;
                    }
                    exportBoard.boardGrid[x][y] = 0;
                } else {
                    if (exportBoard.boardGrid[x][y] != 1) {
                        exportBoard.cellsAlive++;
                    }
                    exportBoard.boardGrid[x][y] = 1;
                }
            }
        }
        drawEditorBoard();
    }

    public void mouseDragOver() {
        erase = false;
    }


    public void saveRLEClick() {
        int[] boundingbox = exportBoard.getBoundingBox();
        int x = Math.abs(boundingbox[1]-boundingbox[0]);
        int y = Math.abs(boundingbox[3]-boundingbox[2]);
        System.out.println(exportBoard.getBoundingBoxPattern());
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Run-length encoding", "*.rle"));
        File file = fileChooser.showSaveDialog(new Stage());
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date date = new Date();

        if (file != null) {
            try {
                PrintWriter printWriter = new PrintWriter(file);
                if (titleField.getText() != null)
                    printWriter.println("#N "+titleField.getText()+".");
                if (authorField.getText() != null) {
                    if (dateCheckBox.isSelected()) {
                        printWriter.println("#O "+authorField.getText()+". Created "+ dateFormat.format(date) + ".");
                    } else {
                        printWriter.println("#O "+authorField.getText()+".");
                    }
                } else if (authorField.getText() == null && dateCheckBox.isSelected()){
                    printWriter.println("#O Created "+ dateFormat.format(date) + ".");
                }
                if (commentField.getText() != null) {
                    printWriter.println("#C "+commentField.getText());
                }
                printWriter.print("x = "+x+", y = "+y+", rule = "+gameOfLife.getRuleString());
                printWriter.close();
            } catch (IOException ioe) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Could not save file.");
                alert.setContentText("Your rle file was not saved. Please try again.");
                alert.showAndWait();
            }

        }
        System.out.println(file.toString());
    }

}
