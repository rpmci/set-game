package com.setgame.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen implements Screen, InputProcessor {

    final SetGame game;

    static final int cardWidth = 97;
    static final int cardHeight = 55;
    static final int gap = 10;
    static final int vpWidth = ((cardWidth + gap) * 3 + gap);
    static final int vpHeight = ((cardHeight + gap) * 8 + gap);

    private Texture spriteSheet;
    private Array<Card> cardDeck;
    private Array<Card> revealedCards;
    private Array<Card> selectedCards;
    private Array<Card> existingSet;

    Sprite restartButton;
    Sprite hintButton;
    String hintText;
    Sprite add3Button;
    Sprite revealButton;

    OrthographicCamera camera;
    Viewport viewport;

    int setsFound;
    int cardsRemaining;

    public GameScreen(final SetGame game) {
        this.game = game;


        // create the camera, viewport, and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, vpWidth, vpHeight);
        viewport = new ScreenViewport(camera);

        initDeck();

        // create buttons
        restartButton = new Sprite(new Texture("restart-button.png"));
        restartButton.setBounds(gap, gap, 61, 25);
        hintButton = new Sprite(new Texture("hint-button.png"));
        hintButton.setBounds(gap + (gap + 61), gap, 61, 25);
        add3Button = new Sprite(new Texture("add3-button.png"));
        add3Button.setBounds(gap + 2*(gap+61), gap, 61, 25);
        revealButton = new Sprite(new Texture("reveal-button.png"));
        revealButton.setBounds(gap + 3*(gap+61), gap, 61, 25);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.5f, 0.5f, 0.5f, 1);
        camera.update();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        for (Card card : revealedCards) {
            card.getSprite().draw(game.batch);
        }
        restartButton.draw(game.batch);
        hintButton.draw(game.batch);
        add3Button.draw(game.batch);
        revealButton.draw(game.batch);
        game.font.draw(game.batch, "Cards Remaining: " + cardsRemaining, gap, cardHeight);
        game.font.draw(game.batch, hintText, 3*(gap+61) + gap, cardHeight);
        game.batch.end();
    }

    public void initDeck() {
        if (spriteSheet == null) {
            spriteSheet = new Texture("card-sheet.png");
            // Image source: https://www.gamesforyoungminds.com/blog/2019/5/16/set-extensions
            selectedCards = new Array<Card>();
            cardDeck = new Array<Card>(81);
            revealedCards = new Array<Card>(12);
            existingSet = new Array<Card>();
        } else {
            selectedCards.clear();
            cardDeck.clear();
            revealedCards.clear();
            existingSet.clear();
        }

        int row = 0;
        int col = 0;
        int x;
        int y;

        for (Card.Colour colour : Card.Colour.values()) {
            for (Card.Shading shading : Card.Shading.values()) {
                for (Card.Shape shape : Card.Shape.values()) {
                    for (Card.Number number : Card.Number.values()) {
                        x = col * cardWidth;
                        y = row * cardHeight;
                        cardDeck.add(new Card(new Sprite(new TextureRegion(spriteSheet, x, y, cardWidth, cardHeight)),
                                colour, shading, shape, number));
                        col += 1;
                    }
                }
                col = 0;
                row += 1;
            }
        }
        cardDeck.shuffle();

        for (int xPos = 0 ; xPos < 3; xPos++) {
            for (int yPos = 0; yPos < 4; yPos++) {
                Card card = cardDeck.pop();
                card.setPosition(gap + xPos * (cardWidth + gap), vpHeight - ((1 + yPos) * (cardHeight + gap)));
                revealedCards.add(card);
            }
        }

        cardsRemainingUpdate();
        hintText = "";
    }

    public void clearSelectedCards() {
        for (Card c : selectedCards) {
            c.toggleHighlight();
        }
        selectedCards.clear();
    }

    public void addThree() {
        if (!cardDeck.isEmpty() && revealedCards.size<=18) {
            int col = (revealedCards.size / 3) + 1;

            for (int row = 0; row < 3; row++) {
                Card card = cardDeck.pop();
                card.setPosition(row * (cardWidth + gap) + gap, vpHeight - (col * (cardHeight + gap)));
                revealedCards.add(card);
            }
            cardsRemainingUpdate();
            hintText = "Added 3 cards";
        } else if (!cardDeck.isEmpty()) {
            hintText = "Too many cards!";
        } else {
            hintText = "No cards remaining";
        }
    }

    public void checkSet() {
        Card c1 = selectedCards.get(0);
        Card c2 = selectedCards.get(1);
        Card c3 = selectedCards.get(2);

        int mainRows = vpHeight - (4 * (gap + cardHeight));

        if (Card.isSet(c1, c2, c3)) {
            boolean deckEmpty = cardDeck.isEmpty();
            boolean needRearrange = revealedCards.size > 12;
            for (Card c : selectedCards) {
                if (needRearrange) {
                    if (c.getSprite().getY() >= mainRows) {
                        for (Card revealedCard : revealedCards) {
                            if (!selectedCards.contains(revealedCard, true)) {
                                if (revealedCard.getSprite().getY() < mainRows) {
                                    revealedCard.setPosition(c.getSprite().getX(), c.getSprite().getY());
                                    break;
                                }
                            }
                        }
                    }
                } else if (!deckEmpty) {
                    Card card = cardDeck.pop();
                    card.setPosition(c.getSprite().getX(), c.getSprite().getY());
                    revealedCards.add(card);
                }
                revealedCards.removeValue(c, true);
            }
            setsFound += 1;
            cardsRemainingUpdate();
        }
        clearSelectedCards();
        existingSet.clear();
        hintText = "";
    }

    public boolean findExistingSet() {
        int n = revealedCards.size;

        for (int first = 0; first < (n-2); first++) {
            for (int second = first + 1; second < (n-1); second++) {
                for (int third = second + 1; third < n; third++) {
                    if (Card.isSet(revealedCards.get(first), revealedCards.get(second), revealedCards.get(third))) {
                        existingSet.add(revealedCards.get(first), revealedCards.get(second), revealedCards.get(third));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void hintTextUpdate(Boolean setExists) {
        if (!setExists) {
            hintText = "No sets exist";
        } else {
            hintText = "There is a set";
        }
    }

    public void cardsRemainingUpdate() {
        cardsRemaining = cardDeck.size;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        spriteSheet.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 touchPos = new Vector3(screenX, screenY, 0);
        camera.unproject(touchPos);

        if (restartButton.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
            initDeck();
            return true;
        } else if (hintButton.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
            hintTextUpdate((findExistingSet()));
            return true;
        } else if (add3Button.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
            if (findExistingSet()) {
                hintTextUpdate(true);
            } else {
                addThree();
            }
            return true;
        } else if (revealButton.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
            boolean setExists = findExistingSet();
            if (setExists) {
                clearSelectedCards();
                Card card = existingSet.first();
                selectedCards.add(card);
                card.toggleHighlight();
            }
            hintTextUpdate(setExists);
            return true;
        }
        for (Card card : revealedCards) {
            if (card.getBounds().contains(touchPos.x, touchPos.y)) {
                card.toggleHighlight();
                if (!selectedCards.contains(card, true)) {
                    selectedCards.add(card);
                    if (selectedCards.size == 3) {
                        checkSet();
                    }
                } else {
                    selectedCards.removeValue(card, true);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector3 mousePos = new Vector3(screenX, screenY, 0);
        camera.unproject(mousePos);

        for (Card card : revealedCards) {
            card.hoverHighlight(card.getBounds().contains(mousePos.x, mousePos.y));
        }
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}