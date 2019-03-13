PixelFlut Challenge
===================

Markus Poeschl & Tobias Schaffner

Sponsor
-------

Pixel Group GmbH

Simon Ashdown

What is PixelFlut?
------------------

 * A digital canvas
 * The pixels can be set and retreived by everybody connected to the server

What is this about
------------------

 * One 4k PixelFlut Server
 * Five challanges
 * Try to solve the challanges faster than the other Teams
 * Use what ever language you like

API
---

'\n' terminated ASCII commands over TCP.

 * SIZE: Returns the size of the visible canvas.
 * PX <x> <y> Return the current color of a pixel.
 * PX <x> <y> <rrggbb(aa)>  Set the color of a pixel.

https://github.com/defnull/pixelflut

Server
------

 * IP:
 * Port:

Areas
-----

The canvas is split into nine equal areas of Size
(3840 / 3) x (2160 / 3)

There is a labirith in the middle of the canvas. This will be the last challange.

The other areas are again split into four for your first challanges.

Rules
-----

 * Use the areas with your Teamnumber.
 * There is a one pixel width boarder around your areas. Do not overrite this.
 * Open one TCP connection and reuse it.

Grouping
--------

Please Group in teams of 2-3 people.

Have Fun
--------

Pizza will arive at about 9PM.

Feel free to ask us if you have any problems. :)

Happy Hacking

Challenges
----------

 * Draw a moving color spectrum
 * Pacman
 * Copy and resize the labirinth in the middle
 * Create a deal with it glasses animation
 * Solve the labirinth

Color Spectrum
--------------

 * Draw a color spectrum of the RGB range
 * Rotate it

Pacman
------

 * Draw a pacman that opens and closes its mouth
 * Every second open mouth show a dot

Labirinth Copy
--------------

 * Copy the labirinth in the middle of the screen.
 * Copy it to the size of your target area.
 * Solution may not be blurry

Deal with it
------------

 * Search a random animal picture and draw it
 * Use the provided glasses
 * animate them moving down to the eyes of the animal
 * Background has to be restored behind the moving animation

Solving the Labirinth
---------------------

 * Solve the labirinth before a new one is rendered (60s)
 * Draw the line from start left top to end right bottom
 * Use the number of your group as offset from left top
