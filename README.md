# VenturaRTP

Kleines RTP-GUI-Plugin für **Paper 26.2 / Java 25**.

## Was es macht

- `/rtp` öffnet ein 3-Button-Menü.
- Overworld = CustomModelData-Float `10001`
- Nether = `10002`
- The End = `10003`
- Klick führt den jeweiligen BetterRTP-Konsolenbefehl aus.
- Die Item-Model-Daten werden über Paper 26.2 `CustomModelDataComponent#setFloats(...)` gesetzt.

## GitHub-Build

1. Neues GitHub-Repository erstellen, z. B. `VenturaRTP`.
2. Inhalt **dieses Projekts** hochladen.
3. Auf GitHub oben auf **Actions**.
4. Workflow **Build VenturaRTP** auswählen.
5. Falls nötig **Run workflow** drücken.
6. Nach erfolgreichem Build den Workflow öffnen.
7. Unter **Artifacts** `VenturaRTP` herunterladen.
8. ZIP entpacken; darin liegt `VenturaRTP-1.0.0.jar`.
9. JAR in den Serverordner `plugins/` hochladen.
10. Server starten.

## Voraussetzungen

- Paper 26.2
- Java 25
- BetterRTP
- Das Ventura-Kingdom-RTP-Resource-Pack mit den Modellen `10001`, `10002`, `10003`.

## config.yml

Nach dem ersten Start liegt unter `plugins/VenturaRTP/config.yml` die Menü-Konfiguration.
Dort können Slots, Texte und BetterRTP-Befehle geändert werden.
