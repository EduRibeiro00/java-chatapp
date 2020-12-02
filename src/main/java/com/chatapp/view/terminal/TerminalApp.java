package com.chatapp.view.terminal;

import com.chatapp.controller.ChatClientReceiver;
import com.chatapp.controller.ChatClientSender;
import com.chatapp.model.Message;
import com.chatapp.view.KafkaPropertiesLoader;
import com.chatapp.view.ReceiverBaseView;
import com.chatapp.view.SenderBaseView;
import com.chatapp.view.terminal.utils.MessagePresenter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

public class TerminalApp implements ReceiverBaseView, SenderBaseView {

    public static void main(final String[] args) {
        Options options = new Options();
        options.addOption(Option.builder("c").longOpt("channel").required(true).hasArg().argName("topic")
                .desc("Channel to connect to.").build());
        options.addOption(Option.builder("n").longOpt("nick").required(false).hasArg().argName("nickname")
                .desc("Nickname to use in the chat.").build());

        String topic = null;
        String nickname = null;

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            topic = cmd.getOptionValue("c", null);
            nickname = cmd.getOptionValue("n", null);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("chat", options, true);
        }

        System.out.println("Started.");

        if (StringUtils.isNotBlank(topic) && StringUtils.isBlank(nickname)) {
            new TerminalApp().consume(topic);
        } else if (StringUtils.isNotBlank(topic) && StringUtils.isNotBlank(nickname)) {
            new TerminalApp().produce(topic, nickname);
        }
    }

    @Override
    public void consume(String topic) {
        ChatClientReceiver receiver = null;
        try {
            receiver = new ChatClientReceiver(KafkaPropertiesLoader.loadProperties());
        }
        catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        ObjectMapper objectMapper = new ObjectMapper();

        receiver.poll(topic, (datetime, payload) -> {
            try {
                Message receivedMsg = objectMapper.readValue(payload, Message.class);
                if (receivedMsg.getText() == null) return;
                MessagePresenter.printMessage(datetime, receivedMsg);

            } catch (Exception e) {
                e.printStackTrace();
                MessagePresenter.printError(datetime);
            }
        });
    }

    @Override
    public void produce(String topic, String nickname) {
        boolean chatting = true;
        Scanner scanner = new Scanner(System.in);
        ChatClientSender sender = null;
        try {
            sender = new ChatClientSender(KafkaPropertiesLoader.loadProperties(), topic);
        }
        catch(IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Type /exit to end.");

        while(chatting) {
            MessagePresenter.printPrompt(nickname);
            String text = scanner.nextLine();

            if (StringUtils.compareIgnoreCase(text, "/exit") == 0) {
                chatting = false;

            } else if (StringUtils.isNotBlank(text)) {
                sender.send(new Message(nickname, text));
            }
        }

        System.out.println("Exiting sender...");
    }

}