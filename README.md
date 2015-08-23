FretsGraphics
==========

Frets is an application to help find guitar chords and patterns.

FretsGraphics is a graphical version that allows interacting with windows and mouse.

Demonstration
==========

1. Build using Maven or importing the Maven project into your development environment.
2. Run the application FretsApplication as a Java application.
3. Click menu item "Entry > Add random" to add a random chord to the list. 
4. Select the chord and notice the details below. For example formula and playability score.
5. Select a chord and click menu item "Entry > Variations (ten)" to generate similar chords.
6. Notice that the similar chords might not be as easy to play as the selection.
7. Also look at the large display tab to see all locations that fit the chord formula.


TODOs
==========
1. Complete Display Option editor panel.
2. Break up of properties into ascii, graphic, user options.
4. Slider or button control of enharmonic and octave transparency.
5. Look up of note/formula, find nearest name, place in comments.
6. Chart making page. 
   Allow spreadsheet-like grid to display multiple fretboard displays
   Each grid entry can display complete model (fretboard, root, formula, notes/locations, variation, score)
   Each grid entry can display incremental changes base on info from another cell. For example:
   Page default data at cell 0,0. Cell 1,1 contains G root, Cell 1,2 modifies $1,$1 root with +2 steps.
   Some cells can be blank or contain text or contain colors or images.
   Page data can be saved and read from property file.
7. More ToDos in frets.swing.ui package.


Contributors	
==========
   <a href="mailto:dan@danbecker.info">Dan Becker</a>	