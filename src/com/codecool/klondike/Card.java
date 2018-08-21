package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private SuitEnum suit;
    private RankEnum rank;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(SuitEnum suit, RankEnum rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public String getSuit() {
        return suit.name;
    }

    public int getRank() {
        return rank.value;
    }

    public RankEnum getRankName() {
        return rank;
    }


    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "S" + suit.name + "R" + rank.value;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + "Rank " + rank.value + " of " + "Suit " + suit.name;
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        return (!card1.suit.color.equals(card2.suit.color));
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.suit.name.equals(card2.suit.name);
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (SuitEnum cardSuit : SuitEnum.values()) {
            for (RankEnum cardRank : RankEnum.values()) {
                result.add(new Card(cardSuit, cardRank, true));
            }
        }
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        SuitEnum suit;
        RankEnum rank;
        for (SuitEnum suitEn : SuitEnum.values()) {
            suit = suitEn;

            for (RankEnum rankEn : RankEnum.values()) {
                rank = rankEn;
                String cardName = suit.name + rank.value;
                String cardId = "S" + suit.name + "R" + rank.value;
                String imageFileName = "card_images/" + cardName + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));

            }
        }
    }
}


