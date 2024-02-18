package me.aragot.hglmoderation.data;

public enum Notification {
    GENERAL,
    REPORT_STATE,
    REPORT,
    BAN,
    MUTE;

     public boolean requiresPermission(){
         switch(this){
             case REPORT:
             case BAN:
             case MUTE:
                 return true;
             default: return false;
         }
    }
}
