package me.aragot.hglmoderation.commands.exceptions;

public class InvalidCommandException extends Exception {

    private final String response;

    public InvalidCommandException(String response){
        this.response = response;
    }

    public String getRawResponse(){
        return this.response;
    }
}
