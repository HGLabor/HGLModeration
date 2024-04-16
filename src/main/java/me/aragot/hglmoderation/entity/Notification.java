package me.aragot.hglmoderation.entity;

public enum Notification {
    GENERAL,
    REPORT_STATE,
    REPORT;

     public boolean requiresPermission() {
         switch (this) {
             case REPORT:
                 return true;
             default: return false;
         }
    }
}
