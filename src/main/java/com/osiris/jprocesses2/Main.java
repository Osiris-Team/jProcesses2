package com.osiris.jprocesses2;

public class Main {
    public static void main(String[] args) {
        if(args!=null && args.length != 0){
            String arg = "";
            for (String a : args) {
                arg += a+" ";
            }
            new ProcessUtils().initCMDTool(arg.trim());
        } else {
            new ProcessUtils().initCMDTool(null);
        }
    }
}
