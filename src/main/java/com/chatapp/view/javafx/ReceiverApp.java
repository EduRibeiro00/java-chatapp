package com.chatapp.view.javafx;

import com.chatapp.controller.ChatClientReceiver;
import com.chatapp.model.Message;
import com.chatapp.view.KafkaPropertiesLoader;
import com.chatapp.view.ReceiverBaseView;
import com.chatapp.view.terminal.TerminalApp;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.cli.*;

import java.io.IOException;

public class ReceiverApp extends Application implements ReceiverBaseView {
    private static int SCENE_WIDTH = 600;
    private static int SCENE_HEIGHT = 1000;
    private static String topic;
    private static VBox root;

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("c").longOpt("channel").required(true).hasArg().argName("topic")
                .desc("Channel to connect to.").build());

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            topic = cmd.getOptionValue("c", null);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("chat", options, true);
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chatapp Receiver Application");

        ScrollPane sp = new ScrollPane();
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        sp.setContent(root);

        Scene scene = new Scene(sp, SCENE_WIDTH, SCENE_HEIGHT);

        primaryStage.setScene(scene);
        primaryStage.show();

        consume(topic);
    }

    @Override
    public void consume(final String topic) {
        try {
            ChatClientReceiver receiver = new ChatClientReceiver(KafkaPropertiesLoader.loadProperties());
            ObjectMapper objectMapper = new ObjectMapper();

            receiver.poll(topic, (datetime, payload) -> {
                try {
                    Message receivedMsg = objectMapper.readValue(payload, Message.class);
                    if (receivedMsg.getText() == null) return;
                    // MessagePresenter.printMessage(datetime, receivedMsg);
                    Text t = new Text();
                    t.setText("olaaaa");
                    root.getChildren().add(t);

                } catch (Exception e) {
                    e.printStackTrace();
                    Text t = new Text();
                    t.setText("erro bro");
                    root.getChildren().add(t);
                }
            });


        }
        catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
