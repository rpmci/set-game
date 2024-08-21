package com.setgame.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;

public class Card {

    public interface Feature {

    }

    public enum Colour implements Feature {
        RED, GREEN, PURPLE
    }
    public enum Shape implements Feature {
        DIAMOND, OVAL, SQUIGGLE
    }
    public enum Shading implements Feature {
        OPEN, STRIPED, SOLID
    }
    public enum Number implements Feature {
        ONE, TWO, THREE
    }

    private final Sprite cardSprite;
    private final Colour cardColour;
    private final Shape cardShape;
    private final Shading cardShading;
    private final Number cardNumber;
    private Rectangle bounds;
    private boolean isHighlighted;

    public Card(Sprite sprite, Colour colour, Shading shading, Shape shape, Number number) {
        cardSprite = sprite;
        cardColour = colour;
        cardShading = shading;
        cardShape = shape;
        cardNumber = number;
        bounds = new Rectangle();
        bounds.setSize(sprite.getWidth(), sprite.getHeight());
    }

    public Sprite getSprite() {
        return cardSprite;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setPosition(float x, float y) {
        cardSprite.setPosition(x, y);
        bounds.setPosition(x, y);
    }

    private void showHighlight() {
        if (isHighlighted) {
            cardSprite.setColor(Color.YELLOW); // Change the color to yellow when highlighted
        } else {
            cardSprite.setColor(Color.WHITE); // Reset the color when not highlighted
        }
    }

    public void toggleHighlight() {
        isHighlighted = !isHighlighted;
        showHighlight();
    }

    public void borderHighlight(boolean highlight) {
        if (highlight) {
            cardSprite.setColor(Color.SKY);
        } else {
            showHighlight();
        }
    }

    public boolean isHighlighted() {
        return isHighlighted;
    }

    private static boolean singleFeatureSet(Feature f1, Feature f2, Feature f3) {
        // Returns true if all features are the same or all different
        return ((f1 == f2 && f2 == f3) || (f1 != f2 && f2 != f3 && f3 != f1));
    }

    public static boolean isSet(Card c1, Card c2, Card c3) {
        return singleFeatureSet(c1.cardColour, c2.cardColour, c3.cardColour) &&
                singleFeatureSet(c1.cardShading, c2.cardShading, c3.cardShading) &&
                singleFeatureSet(c1.cardShape, c2.cardShape, c3.cardShape) &&
                singleFeatureSet(c1.cardNumber, c2.cardNumber, c3.cardNumber);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return cardNumber == card.cardNumber &&
                cardShading == card.cardShading &&
                cardShape == card.cardShape &&
                cardColour == card.cardColour;
    }

    public String printCard(){
        return cardNumber + " " + cardShading + " " + cardColour  + " " + cardShape;
    }
}