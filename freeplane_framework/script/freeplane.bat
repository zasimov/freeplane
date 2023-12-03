java ^
--add-exports=java.desktop/sun.awt.shell=ALL-UNNAMED ^
--add-exports=java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED ^
-Xmx512m -Dorg.freeplane.userfpdir="%APPDATA%\\Freeplane" ^
-Djava.security.manager=allow ^
-jar freeplanelauncher.jar %*
