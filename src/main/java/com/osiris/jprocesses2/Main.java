package com.osiris.jprocesses2;

public class Main {
    public static void main(String[] args) {
        if(args!=null && args.length != 0){
            new ProcessUtils().initCMDTool(args[0]);
        } else {
            new ProcessUtils().initCMDTool(null);
        }
    }
}
