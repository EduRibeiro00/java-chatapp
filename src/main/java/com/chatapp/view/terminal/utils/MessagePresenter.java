package com.chatapp.view.terminal.utils;

import com.chatapp.model.Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessagePresenter {
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void printMessage(LocalDateTime datetime, Message msg) {
        System.out.println(
            "[" +
            datetime.format(formatter) +
            "]" +
            " <" +
            msg.getNickname() +
            "> " +
            msg.getText()
        );
    }

    public static void printError(LocalDateTime datetime) {
        System.out.println(
            "[" +
            datetime.format(formatter) +
            "]" +
            " An error has occured. Message ignored."
        );
    }

    public static void printPrompt(String nickname) {
        System.out.print(nickname + "> ");
    }
}
