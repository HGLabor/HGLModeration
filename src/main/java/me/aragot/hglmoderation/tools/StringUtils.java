package me.aragot.hglmoderation.tools;

public class StringUtils {

    public static String capitalize(String input){
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static String prettyEnum(Enum rawEnum){
        String [] splitEnumName = rawEnum.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();

        for(String part : splitEnumName)
            builder.append(capitalize(part)).append(" ");

        builder.replace(builder.length() - 1, builder.length(), "");
        return builder.toString();
    }
}
