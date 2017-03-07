# AndroidThingsKnightRider
A simple project for Android Things and the Rainbow HAT

This project uses the LED stip from the Rainbow HAT to display a _knigh rider_ style animation, or if you prefer it, a cylon.

It uses the alphanumeric LED display, the RGB LED strip, the buttons, the basic LEDS and the piezo buzzer.
It does not use the temperature sensor.

It works as follows:

* Button A starts / stops the LED Strip _Knight Rider_ style
* Button B increases speed
* Button C decreases speed
* The LEDS are linked to the buttons that are below them, so they light when pressed
* There is audio feedback from the piezo buzzer on click on each button
* The alphanumeric display shows "KITT" on it
