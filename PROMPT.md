Blutooth Keyboard & Mouse

a professional Blutooth Keyboard & Mouse android app to control through Bluetooth connection anything, for example another mobile, raspberry pi, pc, laptop, macbook, anything that will work with a bt  kb & mouse

must work for all os and all hardware and all mobiles, tabs, etc 

project location /home/jay/Documents/Scripts/AI/OpenCode/blutooth-kb-and-mouse

add a connection setup mode, to pair with a device or select an already paired device to use as a keyboard and mouse and other emulated device simulation.

add a settings page where user can choose what devices to emulate, for example keyboard, mouse, speakers, mic, video, other compatible device.
(only have initiailyy keyboard and mouse enabled with toggle buttons and allow to toggle emulating other above named devices.

primary functions:
a mousepad for a mouse and a kb input text input (allow different input text modes, 1 for pressing a key / button / letter / number and it send instantly to the host connected device, mode 2 copy and past from clipboard and send all together as a string, mode 2 a preset, history string to resend some saved prase already prepered or sent before as a string)

allow sensitivaty settings for mouse, delay settings for keyboard (repeat delay etc), 
allow horizontal slider volume level control setting for volume level for emulated speakers.

allow horizontal slider volume level control setting for mic gain

allow saving settings, allow button for reverting to previous saved settings
allow profiles so user can switch between different devices and have different profile settings for those different devices, allow custom name for each stored profile,
allow adding and removing stored profiles

allow toggle option to prevent lock screen
allow using app in background

add any other setting I may of missed.

use github workflows to build the app and put finally release in apk folder in the project location

Dont edit this file

Never change anything in Backup folders (if it exists) but you can use them as a read-only reference if a mistake is made and you need to fix something

save changes to file(s) in question

then after files are added / edited then save any changes made to changes.txt

Implement persistent error handling and debugging throughout the project. Every failure, exception, or unexpected state should generate a clear error code, detailed debug output, and useful diagnostic information to help identify the exact cause quickly.
Do not remove debugging systems after issues are fixed — keep all error codes, logging, stack traces, validation checks, and diagnostic tools permanently integrated so that any future bugs, crashes, or unexpected behaviour can be traced and resolved efficiently.

always use same key-store for each app made via github workflows so it can update correctly without requiring uninstallation

Save changes to changes.txt (create if not exists)

tell me when ready to test (stay quiet after acknowledging you got the message / request / mission every time and stay quiet till its ready to test and respond only if fully complete  or if you need input from me or if I ask for an update)!

when giving final github release link (where applicable), make sure it points to the newest release but without the tag or filename so I can see the correct location without direct downloading the file as thats best practice!

each app needs an About section showing
in about section it should say Made by jnetai.com 
The full version number (same as github release version tag) also add a Check for update button (so internet permissions required) to check latest release version (tag in full)
add a Share App button so users can share the app.
 
each update should use same key store so the app can update and not require uninstall of the app to update it.

each app should be dark centered themed and allow space at bottom so buttons or elements at the bottom of the app should not be cut off, it should look professional.

in releases on github a meaningful name should be used for example Tetris.apk (no need for a debug version of any app or game for android just put the debug version as the main version!

github api tokens / passwords etc can be found in /home/jay/Documents/Scripts/AI/openclaw/password-vault/

build the releases via github actions / workflows (not locally)

