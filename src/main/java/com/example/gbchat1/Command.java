package com.example.gbchat1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Command {
    AUTH("/auth"){
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(COMMAND_DELIMETER);
            return new String[]{split[1], split[2]};
        }
    }, // /auth login1 pass1
    AUTHOK("/authok"){
        @Override
        public String[] parse(String commandText) {
            return new String[]{commandText.split(COMMAND_DELIMETER)[1]};
        }
    }, // /authok nick1
    PRIVATE_MESSAGE("/w"){
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(COMMAND_DELIMETER, 3);
            return new String[] {split[1], split[2]};
        }
    }, // /w nick1 Сообщение
    END("/end"){
        @Override
        public String[] parse(String commandText) {
            return new String[0];
        }
    },
    ERROR("/error"){ // /error Сообщение об ошибке
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(COMMAND_DELIMETER, 2);
            return split;
        }
    },
    CLIENTS("/clients"){
        @Override
        public String[] parse(String commandText) {
            String[] split = commandText.split(COMMAND_DELIMETER);
            String[] nicks = new String[split.length-1];
            for (int i = 0; i < split.length; i++) {
                nicks[i-1] = split[i];
            }

            return nicks;
        }
    };// /end

    private static final Map<String, Command> map = Stream.of(Command.values()).collect(Collectors.toMap(Command::getCommand, Function.identity()));

    private static final String COMMAND_DELIMETER = "\\s ";
    private String command;
    private String[] params = new String[0];

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static boolean isCommand(String message) {
        return message.startsWith("/");
    }

    public String[] getParams() {
        return params;
    }

    public static Command getCommand(String message) {
        message = message.trim();
        if (!isCommand(message)) {
            throw new RuntimeException("'" + message + "'is not command");
        }
        int i = message.indexOf(" ");
        final String cmd = i > 0 ? message.substring(0, i) : message;
        return map.get(cmd);

    }

    public abstract String[] parse(String commandText);

    public String collectMessage(String... params) {
        String command = this.getCommand();
        return command + (params == null ? "" : " " + String.join(" ", params));
    }
}
