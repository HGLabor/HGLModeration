package me.aragot.hglmoderation.data;

public enum Notification {
    GENERAL,
    REPORT_STATE,
    REPORT;

     public boolean requiresPermission(){
         switch(this){
             case REPORT:
                 return true;
             default: return false;
         }
    }
}
