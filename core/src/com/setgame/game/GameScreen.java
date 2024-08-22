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

public class GameScreen implements Screen, InputProcessor {

    final SetGame game;

    // Easy adjusting the size and card layout // TODO improve resizing
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
    Sprite hintButton;    // TODO maybe show number of sets available
    String hintText;
    Sprite add3Button;
    Sprite revealButton; // TODO maybe randomize highlighted card

//    Sound dropSound;
//    Music rainMusic;
    OrthographicCamera camera;

    int setsFound;
    int cardsRemaining;

    public GameScreen(final SetGame game) {
        this.game = game;

        // TODO sound effect and background "music"
//        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
//        rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
//        rainMusic.setLooping(true);

        // create the camera and the SpriteBatch
        camera = new OrthographicCamera();
        camera.setToOrtho(false, vpWidth, vpHeight);

        initDeck();

        // Button creator
        restartButton = new Sprite(new Texture("restart-button.png"));
        restartButton.setBounds(gap, gap, 61, 25);
        hintButton = new Sprite(new Texture("hint-button.png"));
        hintButton.setBounds(gap + (gap + 61), gap, 61, 25);
        add3Button = new Sprite(new Texture("add3-button.png"));
        add3Button.setBounds(gap + 2*(gap+61), gap, 61, 25);
        revealButton = new Sprite(new Texture("reveal-button.png"));
        revealButton.setBounds(gap + 3*(gap+61), gap, 61, 25);

        hintText = "";

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        // clear the screen with a dark blue color. The
        // arguments to clear are the red, green
        // blue and alpha component in the range [0,1]
        // of the color to be used to clear the screen.
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
//        game.font.draw(game.batch, "Sets found: " + setsFound, 3*(gap+61) + gap, 2 * gap);
//        game.font.draw(game.batch, "Deck: " + cardsRemaining + " cards", 3*(gap+61) + gap, 2 * gap); // draw far right
//        game.font.draw(game.batch, hintText, 3*(gap+61) + gap, (2 * gap) + 15); // draw far right
        game.font.draw(game.batch, "Cards Remaining: " + cardsRemaining, gap, cardHeight);
        game.font.draw(game.batch, hintText, 3*(gap+61) + gap, cardHeight);
        game.batch.end();


        // TODO add keyboard input
        // // Gdx.input.isKeyPressed(Keys.LEFT)
        // TODO add timer
        // // TimeUtils
        // // game.font.draw(game.batch, "Sets Collected: " + setsFound, 0, 480);
        // TODO check how many sets exist
    }

    public void initDeck() {
        if (spriteSheet == null) {
            spriteSheet = new Texture("card-sheet.png");
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
//                        if (cardDeck.size == 12) { break; } // debugging
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
    }

    public void clearSelectedCards() {
        for (Card c : selectedCards) {
            c.toggleHighlight();
        }
        selectedCards.clear();
    }

    public void addThree() {
        if (!cardDeck.isEmpty()) {
            int yPos = revealedCards.size / 3;

            for (int row = 0; row < 3; row++) {
                int col = yPos + 1; // need to replace hard coded number. I'm assuming this is 12 going to 15 cards
                Card card = cardDeck.pop();
                card.setPosition(row * (cardWidth + gap) + gap, vpHeight - (col * (cardHeight + gap)));
                revealedCards.add(card);
            }
            cardsRemainingUpdate();
        }
        hintText = "Added 3 cards"; //TODO different text if deck is empty
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
            System.out.println("Set found!"); // debugging
            setsFound += 1;
            cardsRemainingUpdate();
        }

        clearSelectedCards();
        existingSet.clear();
        System.out.println("Cards: cleared"); // debugging
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
            System.out.println("No sets exist"); // debugging
        } else {
            hintText = "There is a set";
            System.out.println("There is a set"); // debugging
        }
    }

    public void cardsRemainingUpdate() {
        cardsRemaining = cardDeck.size;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
//        rainMusic.play();
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
//        dropSound.dispose();
//        rainMusic.dispose();
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

        for (Card card : revealedCards) {
            if (card.getBounds().contains(touchPos.x, touchPos.y)) {
                System.out.println("Card: " + card.printCard()); // debugging

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
        if (restartButton.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
            initDeck();
            System.out.println("Restarting"); // debugging
            return true;
        } else if (hintButton.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
            System.out.println("Hint"); // debugging
            hintTextUpdate((findExistingSet()));
            return true;
        } else if (add3Button.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
            System.out.println("Add3"); // debugging
            if (findExistingSet()) {
                hintTextUpdate(true);
            } else {
                addThree();
            }
            return true;
        } else if (revealButton.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
            System.out.println("Reveal"); // debugging
            boolean setExists = findExistingSet();
            if (setExists) {
                clearSelectedCards();
                Card card = existingSet.first();
                selectedCards.add(card);
                card.toggleHighlight();
            }
            hintTextUpdate(setExists);

            // todo add a blinking animation
            return true;
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