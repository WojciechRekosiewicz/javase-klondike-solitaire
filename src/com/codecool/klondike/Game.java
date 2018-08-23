package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.*;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();  //draggedCards for view purpose
    private List<Card> drCards = FXCollections.observableArrayList(); //draggedCards for model purpose

    private static double STOCK_GAP = 0;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private Label label;

    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        draggedCards.add(card);

        card.getDropShadow().setRadius(20);
        card.getDropShadow().setOffsetX(10);
        card.getDropShadow().setOffsetY(10);

        card.toFront();
        card.setTranslateX(offsetX);
        card.setTranslateY(offsetY);
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();
        Pile pile = getValidIntersectingPile(card, tableauPiles);
     //   Pile pile = getValidIntersectingPile(card, foundationPiles);
        //TODO ????????
        if (pile != null) {
            handleValidMove(card, pile);
        } else {

            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear(); // zmiast draggedcards = null
        }
    };

    public boolean isGameWon() {
        for (Pile pile : foundationPiles) {
            if (pile.numOfCards() != 13) return false;
        } restartGame();
        return true;
    }

    public void restartGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Kongratulejszyn");
        alert.setHeaderText("You WIN!");
        alert.setContentText("Are you want to restart?");
        Optional<ButtonType> option = alert.showAndWait();

        if (option.get() == null) {
            this.label.setText("No selection!");
        } else if (option.get() == ButtonType.OK) {
            clearBoard();
            deck = Card.createNewDeck();
            initPiles();
            dealCards();
        } else if (option.get() == ButtonType.CANCEL) {
            System.exit(0);
        } else {
            this.label.setText("-");
        }
    }

    public void clearBoard(){
        stockPile.clear();
        discardPile.clear();
        foundationPiles.clear();
        tableauPiles.clear();
        this.getChildren().clear();
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();

    }

    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        List<Card> discardedCards = discardPile.getCards();
        Collections.reverse(discardedCards);
        for (Card card : discardedCards){
            if (!card.isFaceDown()) card.flip();
            stockPile.addCard(card);
            }
        discardPile.clear();
        System.out.println("Stock refilled from discard pile.");
        }



    public boolean isMoveValid(Card card, Pile destPile) {
        Card topCard = destPile.getTopCard();
        switch (destPile.getPileType()) {
            case STOCK:
                return false;
            case DISCARD:
                return false;
            case FOUNDATION:
                if (topCard != null) {
                    System.out.println("NO");
                    return(Card.isSameSuit(card, topCard) && card.getRank() == topCard.getRank() + 1);}
                else{
                    System.out.println("YES");
                    return(card.getRankName().equals(RankEnum.ACE));}
            case TABLEAU:
                if (topCard != null) return(Card.isOppositeColor(card, topCard) && card.getRank() == topCard.getRank() - 1);
                else return(card.getRankName().equals(RankEnum.KING));
            default:
                    return false;
        }

    }



    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }


    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }


        // ###DRAG CARDS  - przenosi kilka kart naraz###
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.TABLEAU && destPile.getPileType() == Pile.PileType.TABLEAU) { // dziala tylko dla kart przenoszonych z tableu na tableu
            int start = activePile.getCards().indexOf(card);
            int stop = activePile.getCards().size();
            drCards = activePile.getCards().subList(start, stop); // wycina kawalek z listy kart od indexu przenoszonej karty do konca listy.
        }
        System.out.println(msg);
        if (drCards.size() != 0) MouseUtil.slideToDest(drCards, destPile);
        else MouseUtil.slideToDest(draggedCards, destPile);
        drCards.clear();
        //######

        System.out.println(msg);
        draggedCards.clear();
       isGameWon();
       System.out.println(isGameWon());
       if (isGameWon() == true){
            restartGame();
        }
        System.out.println(isGameWon());
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        //TODO

        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
