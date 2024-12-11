package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import nocah.spacebattles.netevents.SpawnEvent;

public class MainMenuScreen extends ScreenAdapter {
    private final SpaceBattles game;
    private Stage stage;

    public MainMenuScreen(SpaceBattles game) {
        this.game = game;
    }

    @Override
    public void show() {

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextField ipField = new TextField("", SpaceBattles.skin);
        ipField.setMessageText("Enter IP Address");
        ipField.setAlignment(Align.center);

        TextButton hostButton = new TextButton("Host", SpaceBattles.skin);
        hostButton.setSize(200, 50);
        hostButton.getLabel().setFontScale(1.5f);
        hostButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                System.out.println("Host button clicked!");
                game.server = new Server();
                game.server.startServer(game);
                //spawn in a new client
                game.handlers.handleClientEvent(new SpawnEvent((byte)0));
                game.connected = true;
                game.playClick();
            }
        });

        TextButton joinButton = new TextButton("Join", SpaceBattles.skin);
        joinButton.setSize(200, 50);
        joinButton.getLabel().setFontScale(1.5f);
        joinButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                System.out.println("Join button clicked!");
                String ip = ipField.getText();
                game.client = new Client(ip);
                game.playClick();
            }
        });

        table.add(hostButton).pad(20).width(300).height(80).row();
        table.add(joinButton).pad(20).width(300).height(80).row();
        table.add(ipField).pad(10).width(300).height(50);
    }

    public void update(float delta) {
        game.handleNetworkEvents();
        if (game.connected) {
            game.setScreen(new LobbyScreen(game));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        stage.dispose();
    }
}
