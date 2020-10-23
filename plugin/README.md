###Arterion Plugin

How to set up your test server:

1. Add a Run Configuration "Export Plugin" as Maven task with maven goals clean and package
2. Add a Run Configuration "Run Spigot" as JAR Application with the spigot jar in the test_server folder and same working dir. (Use Java 8 or the DB will fail)
3. Run the export configuration, then run/reload the server

Enjoy :)