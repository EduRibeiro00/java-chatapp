package com.chatapp.view.javafx;

import com.chatapp.controller.ChatClientSender;
import com.chatapp.model.Message;
import com.chatapp.view.KafkaPropertiesLoader;
import com.chatapp.view.SenderBaseView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import javafx.scene.control.Button;
import java.io.IOException;


public class SenderApp extends Application implements SenderBaseView {
    private static int SCENE_WIDTH = 300;
    private static int SCENE_HEIGHT = 250;
    private static String topic;
    private static String nickname;
    private static Scene scene;

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("c").longOpt("channel").required(true).hasArg().argName("topic")
                .desc("Channel to connect to.").build());
        options.addOption(Option.builder("n").longOpt("nick").required(false).hasArg().argName("nickname")
                .desc("Nickname to use in the chat.").build());

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            topic = cmd.getOptionValue("c", null);
            nickname = cmd.getOptionValue("n", null);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("chat", options, true);
        }

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chatapp Sender Application");

        produce(topic, nickname);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void produce(final String topic, final String nickname) {
        try {
            final ChatClientSender sender = new ChatClientSender(KafkaPropertiesLoader.loadProperties(), topic);

            Label msgLabel = new Label("Message:");
            TextField msgTextField = new TextField();
            Button sendMsgButton = new Button("Send");

            GridPane root = new GridPane();
            root.addRow(0, msgLabel, msgTextField);
            root.addRow(1, sendMsgButton);

            sendMsgButton.setOnAction(e -> {
                String text = msgTextField.getText();
                msgTextField.clear();

                if (StringUtils.compareIgnoreCase(text, "/exit") == 0) {
                    System.exit(0);
                } else if (StringUtils.isNotBlank(text)) {
                    sender.send(new Message(nickname, text));
                }
            });

            scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        }
        catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
