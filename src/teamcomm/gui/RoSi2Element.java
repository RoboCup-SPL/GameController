package teamcomm.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.media.opengl.GL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Felix Thielke
 */
public class RoSi2Element {

    private final List<RoSi2Element> children = new LinkedList<RoSi2Element>();
    private final Map<String, String> vars = new HashMap<String, String>();
    private final Map<String, String> attributes = new HashMap<String, String>();
    private final String name;
    private final String tag;

    private RoSi2Element(final String tag) {
        this(tag, null);
    }

    private RoSi2Element(final String tag, final String name) {
        this.tag = tag;
        this.name = name;
    }

    /**
     * Returns the child element with the given name from the element. Elements
     * are searched via breadth-first search.
     *
     * @param name Name of the child
     * @return Child element or null if no matching element was found
     */
    public RoSi2Element getElement(final String name) {
        if (name == null) {
            return null;
        }

        final LinkedList<RoSi2Element> elems = new LinkedList<RoSi2Element>(children);

        while (!elems.isEmpty()) {
            final RoSi2Element cur = elems.pollFirst();
            if (name.equals(cur.name)) {
                return cur;
            } else {
                elems.addAll(cur.children);
            }
        }

        return null;
    }

    /**
     * Creates a display list on the given GL object which renders the element.
     * The display list will not be destroyed, so this object may safely be
     * garbage collected afterwards.
     *
     * @param gl GL object
     * @return number of the created display list
     */
    public int createDisplayList(final GL gl) {
        return 0;
    }

    /**
     * Parses the given ros2 file and returns its scene element. In case no
     * scene element exists, the &lt;Simulation&gt; root element is returned.
     *
     * @param filename path to the file to parse
     * @return Element representing the scene
     */
    public static RoSi2Element parseFile(final String filename) throws RoSi2ParseException, XMLStreamException, FileNotFoundException, IOException {
        // XML parser factory
        final XMLInputFactory factory = XMLInputFactory.newFactory();

        // Stack containing the opened files
        final Deque<InputFileState> inputFileStack = new LinkedList<InputFileState>();

        // Stack containing the current element hierarchy
        final Deque<RoSi2Element> parentStack = new LinkedList<RoSi2Element>();

        // Map for references to named elements
        final Map<String, RoSi2Element> namedElements = new HashMap<String, RoSi2Element>();

        // Open the given file
        inputFileStack.addFirst(new InputFileState(factory, new File(filename)));

        // Create the root element
        parentStack.addFirst(new RoSi2Element("Simulation"));

        // Parse the file(s)
        while (!inputFileStack.isEmpty()) {
            while (inputFileStack.getFirst().reader.hasNext()) {
                final XMLEvent ev = inputFileStack.getFirst().reader.nextEvent();

                if (ev.isStartElement()) {
                    final StartElement e = ev.asStartElement();
                    final String tag = e.getName().getLocalPart();

                    if (tag.equals("Simulation")) {
                        // Start actual parsing after passing Simulation element
                        inputFileStack.getFirst().simulationTagPassed = true;
                    } else if (tag.equals("Include")) {
                        // Open the included file
                        inputFileStack.addFirst(new InputFileState(factory, new File(inputFileStack.getFirst().path.getParentFile(), getXmlAttribute(e, "href", true))));
                    } else if (inputFileStack.getFirst().simulationTagPassed) {
                        if (tag.equals("Set")) {
                            // Set variable binding
                            parentStack.getFirst().vars.putIfAbsent(getXmlAttribute(e, "name", true), getXmlAttribute(e, "value", true));
                        } else {
                            // Create and add element
                            final String name = getXmlAttribute(e, "name", false);
                            final RoSi2Element elem = new RoSi2Element(tag, name);
                            if (name != null) {
                                namedElements.put(name, elem);
                            }
                            parentStack.getFirst().children.add(elem);
                            parentStack.addFirst(elem);
                        }
                    }
                } else if (ev.isEndElement()) {
                    if (ev.asEndElement().getName().getLocalPart().equals(parentStack.getFirst().tag)) {
                        parentStack.removeFirst();
                    }
                }
            }

            // Close the current file
            inputFileStack.pollFirst().close();
        }

        if (!(parentStack.size() == 1 && parentStack.getFirst().tag.equals("Simulation"))) {
            throw new RoSi2ParseException("File ended before parsing was complete");
        }

        // TODO: Find the scene element and resolve the references and variable
        //       instantiations of its named children
        return null;
    }

    private static String getXmlAttribute(final StartElement e, final String name, boolean required) throws RoSi2ParseException {
        final Attribute attr = e.getAttributeByName(new QName(name));
        if (attr == null) {
            if (required) {
                throw new RoSi2ParseException("Missing attribute " + name + " on " + e.getName().getLocalPart() + " tag.");
            }
            return null;
        }

        return attr.getValue();
    }

    public static class RoSi2ParseException extends Exception {

        private static final long serialVersionUID = 439895799292899819L;

        public RoSi2ParseException() {
        }

        public RoSi2ParseException(final String message) {
            super(message);
        }

    }

    private static class InputFileState {

        public final XMLEventReader reader;
        public final File path;
        public final FileInputStream stream;
        public boolean simulationTagPassed;

        public InputFileState(final XMLInputFactory factory, final File path) throws XMLStreamException, FileNotFoundException {
            File p;
            try {
                p = path.getCanonicalFile();
            } catch (IOException e) {
                p = path.getAbsoluteFile();
            }
            this.path = p;
            stream = new FileInputStream(path);
            reader = factory.createXMLEventReader(stream);
            simulationTagPassed = false;
        }

        public void close() throws XMLStreamException, IOException {
            reader.close();
            stream.close();
        }
    }
}
