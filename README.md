circuit-simulator
=================

This is a version of Paul Falstad's circuit simulator (original available at <http://www.falstad.com/circuit/>) with the following improvements:

* fixed import/export when running as an applet
* it is now possible to save/load to/from a file when running as an application
* the circuits that appear in the "Circuit" menu may now be bundled with the jar file.
* many interface improvements: double click to edit, Delete key to delete a component, cursor changes to crosshairs when in place mode, Esc goes back to move/interact mode, ...
* separated keyboard shortcuts from dump types (this makes it easier to configure shortcuts without changing the file format)
* WireElm is now a subclass of ResistorElm (without this change, whenever you use two switches in parallel, you get the error "voltage source/wire loop with no resistance")
* some cosmetic improvements (like starting with a white background as default)

compiling and building
----------------------

If you have make installed, just `cd` to the *src* directory and run `make`, followed by a `make jar`

If not, compile with `javac *.java` and build the jar file with `jar cfm circuit.jar Manifest.txt *.class *.txt circuits/`

running
-------

As an application: `java -jar circuit.jar`

If you want to use the circuit simulator as an applet, create an html file and load the applet with:

    <applet code=Circuit.class archive=circuit.jar width=600 height=50>
        Sorry, you need a Java-enabled browser to see the simulation.
        <param name=pause value=20>
    </applet>

If you want to enable import/export of circuit files, you must define two javascript helper functions in the same page:

* `exportCircuit(dump)`, whose first parameter is a string describing the circuit in the same format used for the circuit file.

*  `importCircuit()`, which must return a string describing the circuit in the same format used for the circuit file.

The definition of those functions is up to you (for instance, you may copy the string to/from a textarea).

terms and conditions
--------------------

The terms and conditions for the original code still apply. Check <http://www.falstad.com/licensing.html> before redistributing or modifying the code. You must always consult the original licensing information but, in case the link is unavailable, here is a copy of the original license (as of 2013-05-08):

    You have permission to use these applets in a classroom setting or take
    screenshots as long as the applets are unmodified. Modification or
    redistribution for non-commercial purposes is allowed, as long as you
    credit me (Paul Falstad) and provide a link to my page (the page you
    found the applet(s) on, or http://www.falstad.com/mathphysics.html).

    Contact me for any other uses. The source code for each applet is
    generally available on that applet's web page, but some of the applets
    use third-party source code that has restrictions.

    THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.

Rodrigo Hausen's changes are in the public domain (but it would be nice if you credited me alongside Paul if you use this version).
