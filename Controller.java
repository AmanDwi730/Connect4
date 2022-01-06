package com.Aman.Connect4;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable
{
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private static final int Circle_Diameter = 80;
    private static final String DiscColor1 ="#FF0000";
    private static final String DiscColor2 ="#FFFF00";

    private static String Player_One = "Player One";
    private static String Player_Two = "Player Two";

    private boolean isPlayerOneTurn = true;

    private boolean isAllowedToInsert = true;

    private Disc[][] InsertedDisc = new Disc[ROWS][COLUMNS];

    @FXML
    public GridPane rootGridPane;

    @FXML
    public Pane InsertedDiscsPane;

    @FXML
    public Label playerOnelabel;

    @FXML
    public TextField playerOneTextField, playerTwoTextField;

    @FXML
    public Button setNamesButton;


    public void createPlayGround() {
        Shape Holes = createGameStructuralGrid();
        rootGridPane.add(Holes, 0, 1);

        List<Rectangle> rectangleList = ClickableColumns();
        for (Rectangle rect : rectangleList) {
            rootGridPane.add(rect, 0, 1);
         }
            setNamesButton.setOnAction(actionEvent -> {
                Player_One = playerOneTextField.getText();
                Player_Two = playerTwoTextField.getText();
                playerOnelabel.setText(isPlayerOneTurn ? Player_One : Player_Two);
            });

    }

    private Shape createGameStructuralGrid()
    {
        Shape Holes = new Rectangle((COLUMNS + 1) * Circle_Diameter , (ROWS + 1) * Circle_Diameter);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLUMNS ; col++) {

                Circle circle = new Circle();
                circle.setRadius(Circle_Diameter / 2);
                circle.setCenterX(Circle_Diameter / 2);
                circle.setCenterY(Circle_Diameter / 2);
                circle.setSmooth(true);
                circle.setTranslateX(col * (Circle_Diameter + 5) + Circle_Diameter / 4);
                circle.setTranslateY(row * (Circle_Diameter + 5) + Circle_Diameter / 4);

                Holes = Shape.subtract(Holes,circle);

            }
        }

        Holes.setFill(Color.WHITE);

        return Holes;
    }

    private List<Rectangle> ClickableColumns() {
        List<Rectangle> rectangleList = new ArrayList<>();

        for (int col = 0; col < COLUMNS; col++)
        {
            Rectangle rect = new Rectangle(Circle_Diameter, (ROWS + 1) * Circle_Diameter);
            rect.setFill(Color.TRANSPARENT);
            rect.setTranslateX(col * (Circle_Diameter + 5) + Circle_Diameter / 4);

            rect.setOnMouseEntered(mouseEvent -> rect.setFill(Color.valueOf("#eeeeee26")));
            rect.setOnMouseExited(mouseEvent -> rect.setFill(Color.TRANSPARENT));

            final int column = col;
            rect.setOnMouseClicked(mouseEvent -> {
                if(isAllowedToInsert){
                    isAllowedToInsert = false; // When disc is being dropped then no more disc will be inserted
                    insertDisc(new Disc(isPlayerOneTurn), column);
                }
            });
            rectangleList.add(rect);
        }
            return rectangleList;
        }

    private void insertDisc( Disc disc , int column){

        int row = ROWS - 1;
        while (row >= 0) {

            if(getDiscIfPresent(row , column) == null)
                break;

            row--;
        }

        if (row < 0)
            return;

        InsertedDisc[row][column] = disc;
        InsertedDiscsPane.getChildren().add(disc);

         int currentRow = row;
        disc.setTranslateX(column * (Circle_Diameter + 5) + Circle_Diameter / 4);
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.4), disc);
        translateTransition.setToY(row * (Circle_Diameter + 5) + Circle_Diameter / 4);
        translateTransition.play();
        translateTransition.setOnFinished(actionEvent -> {

            isAllowedToInsert = true; // Finally, when the disc is dropped allow next player to insert disc.
            if (gameEnded(currentRow, column)){
                   gameOver();
                   return;
            }
            isPlayerOneTurn = !isPlayerOneTurn;
            playerOnelabel.setText(isPlayerOneTurn? Player_One : Player_Two);
        });
    }

    private boolean gameEnded(int row , int column) {

      List<Point2D> VerticalPoints = IntStream.rangeClosed(row - 3, row + 3)
                                     .mapToObj(r -> new Point2D(r , column))
                                     .collect(Collectors.toList());

        List<Point2D> HorizontalPoints = IntStream.rangeClosed(column - 3, column + 3)
                                       .mapToObj(col -> new Point2D(row , col))
                                       .collect(Collectors.toList());

        Point2D startPoint1 = new Point2D(row - 3, column + 3);
        List<Point2D> Diagonal1Points = IntStream.rangeClosed(0,6)
                       .mapToObj(i -> startPoint1.add( i ,-i) )
                       .collect(Collectors.toList());

        Point2D startPoint2 = new Point2D(row - 3, column - 3);
        List<Point2D> Diagonal2Points = IntStream.rangeClosed(0,6)
                      .mapToObj(i -> startPoint2.add( i , i) )
                      .collect(Collectors.toList());

        boolean isEnded = checkCombination(VerticalPoints) || checkCombination(HorizontalPoints)
                          || checkCombination(Diagonal1Points) || checkCombination(Diagonal2Points);

           return isEnded;
     }

     private boolean checkCombination(List<Point2D> points){

        int chain = 0;
         for (Point2D point: points) {

             int rowIndexForArray = (int) point.getX();
             int colIndexForArray = (int) point.getY();

             Disc disc = getDiscIfPresent(rowIndexForArray, colIndexForArray);

             if (disc != null && disc.isplayeroneMove == isPlayerOneTurn){
                 chain++;
                 if (chain == 4){
                     return true;
                 }
             }else {
                 chain = 0;
             }

         }
         return false;
     }

     private Disc getDiscIfPresent(int row, int column){ // To prevent ArrayIndexOfBoundException

        if(row >= ROWS || column >=COLUMNS || row < 0 || column < 0) // if row and column index is invalid
            return null;

        return InsertedDisc[row][column];
    }

     private void gameOver(){
         String winner = isPlayerOneTurn? Player_One : Player_Two;
         System.out.println("WINNER is: " + winner);

         Alert alert = new Alert(Alert.AlertType.INFORMATION);
         alert.setTitle("Connect4Game");
         alert.setHeaderText("The Winner is : " + winner);
         alert.setContentText(" Do you want to play again ? ");

         ButtonType yesBtn = new ButtonType("Yes");
         ButtonType noBtn = new ButtonType("No, Exit");
         alert.getButtonTypes().setAll(yesBtn , noBtn);

         Platform.runLater( () ->{

             Optional<ButtonType> btnClicked = alert.showAndWait();
             if(btnClicked.isPresent() && btnClicked.get() == yesBtn){

                 resetGame();

             } else {
                 Platform.exit();
                 System.exit(0);
             }
         });
    }

    public void resetGame() {
        InsertedDiscsPane.getChildren().clear(); // remove all the Inserted Disc from Pane

        for (int row = 0; row < InsertedDisc.length; row++) { // Structurally, make all the element InsertedDisc[][] to Null
            for (int col = 0; col < InsertedDisc[row].length; col++) {
                InsertedDisc[row][col] = null;
            }
        }
        isPlayerOneTurn = true; // Let player one start the game
        playerOnelabel.setText(Player_One);

        createPlayGround(); // prepare fresh game
    }

    private static class Disc extends Circle {
        private final boolean isplayeroneMove;

        public Disc(boolean isplayeroneMove) {

                  this.isplayeroneMove = isplayeroneMove;
                  setRadius(Circle_Diameter / 2);
                  setFill(isplayeroneMove ? Color.valueOf(DiscColor1): Color.valueOf(DiscColor2) );
                  setCenterX(Circle_Diameter / 2);
                  setCenterY(Circle_Diameter / 2);
              }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
