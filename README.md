# MG4 Dashcam Trigger

`MG4 Dashcam Trigger` is a small launcher app for the MG4 that does not open its own camera interface when tapped. Instead, it sends a direct external trigger to the [MG4-360-Camera-App](https://github.com/jamakr4/MG4-360-Camera-App).

The purpose of this app is to let you save a dashcam event with a single tap from the sidebar, without having to manually open the `MG4-360-Camera-App` first just to press the event button there.

## What this app does

This app acts purely as a lightweight trigger for dashcam event saving.

When launched from the sidebar or app icon:
- it does not open the full camera UI
- it does not start its own dashcam recording
- it does not process or store any video itself
- it only sends an external trigger to the `MG4-360-Camera-App`

If the dashcam is enabled in the `MG4-360-Camera-App` and its recording service is running, the main app will perform the same event-save action that would normally happen when pressing the event button inside the camera app.

## How it works

`MG4 Dashcam Trigger` does not contain any dashcam, camera, recording, or event-buffer logic of its own.

Instead, it uses a deliberately small integration point exposed by the `MG4-360-Camera-App`:
- `MG4 Dashcam Trigger` sends a broadcast intent
- the `MG4-360-Camera-App` receives that trigger
- the `MG4-360-Camera-App` runs its internal dashcam event-save function

Technically, this app is only an external launcher for functionality that already exists inside the `MG4-360-Camera-App`.

## Important note

All dashcam logic and actual recording functionality are **not** part of this app.

This app specifically does **not** include:
- no dashcam implementation
- no recording engine
- no camera control
- no event buffer logic
- no clip storage or file management
- no overlay or preview functionality

All of those features belong to the `MG4-360-Camera-App`.

As a result, `MG4 Dashcam Trigger` is only useful when used together with a compatible and properly installed version of the [MG4-360-Camera-App](https://github.com/jamakr4/MG4-360-Camera-App).

The trigger app also includes a warning/error sound used by the error banner when the external trigger cannot be completed.

## Requirements

For the trigger to work:
- the `MG4-360-Camera-App` must be installed
- dashcam functionality must be configured and enabled there
- the recording service of the `MG4-360-Camera-App` must be running

If those conditions are not met, the trigger may still be sent, but no dashcam event will be saved.

## Intended use

This app is meant for users who want a direct sidebar shortcut for dashcam event saving in the MG4 like with [SmartEdge](https://github.com/Imtiaz-Official/Smart-Edge).

Typical flow:
1. The dashcam is already running in the background via the `MG4-360-Camera-App`
2. The user taps `MG4 Dashcam Trigger` in the sidebar
3. The `MG4-360-Camera-App` saves the current dashcam event
4. The full camera app does not need to be brought to the foreground

## Credits

Sound credit for the trigger app error banner:

- `notification-sound-7062` by `HenryCena82595` — [Freesound](https://freesound.org/s/731783/) — License: `Attribution NonCommercial 4.0`

The notification sound file is licensed under CC-BY-NC-4.0 and therefore not tracked in this repository. To build, download it manually:
- Get the source from [Freesound — notification-sound-7062 by HenryCena82595](https://freesound.org/s/731783/)
- Place it at `app/src/main/res/raw/notification_sound_7062_henrycena82595.ogg`
- Convert to `.ogg` (Vorbis) if Freesound serves another format — the app loads the resource via `R.raw.notification_sound_7062_henrycena82595` and expects an `.ogg` extension

## Disclaimer

This app is only a companion utility for the `MG4-360-Camera-App` and does not replace it.
Without the main app, `MG4 Dashcam Trigger` does not provide any standalone dashcam functionality.

**Use at your own risk.**

This project involves modifying a production vehicle. The author(s) take **no responsibility** for any damage, malfunction, data loss, voided warranty, or any other consequences resulting from the use of these modifications. Modifying vehicle software may affect safety systems — always test in a safe environment.

This is an independent community project and is **not affiliated with SAIC, MG Motor, or any of their subsidiaries**.
