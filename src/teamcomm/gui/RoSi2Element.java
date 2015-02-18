package teamcomm.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.media.opengl.GL2;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Felix Thielke
 */
public class RoSi2Element {

    /**
     * References to all named elements
     */
    private final Map<String, RoSi2Element> namedElements;

    /**
     * Immediate children of this element.
     */
    private final List<RoSi2Element> children = new LinkedList<RoSi2Element>();

    /**
     * Set variable bindings for this element and its children.
     */
    private final Map<String, String> vars = new HashMap<String, String>();

    /**
     * Attributes of this element.
     */
    private final Map<String, String> attributes = new HashMap<String, String>();

    /**
     * Name of this element or null if it is unnamed.
     */
    private final String name;

    /**
     * Tag of this element.
     */
    private final String tag;

    /**
     * Textual content of this element.
     */
    private final StringBuilder content = new StringBuilder();

    /**
     * Drawable instance of this element. Only set if the element is constant,
     * i.e. it and its children reference no variables, and it was instantiated
     * as a drawable at least once.
     */
    private RoSi2Drawable constantInstance;

    private RoSi2Element(final String tag, final Map<String, RoSi2Element> namedElements) {
        this(tag, null, null, namedElements);
    }

    private RoSi2Element(final String tag, final String name, final Iterator<Attribute> iter, final Map<String, RoSi2Element> namedElements) {
        this.tag = tag;
        this.name = name;
        this.namedElements = namedElements;

        if (iter != null) {
            while (iter.hasNext()) {
                final Attribute attr = iter.next();
                attributes.put(attr.getName().getLocalPart(), attr.getValue());
            }
        }
    }

    /**
     * Returns the child element with the given name from the element. Elements
     * are searched via breadth-first search.
     *
     * @param name Name of the child
     * @return Child element or null if no matching element was found
     */
    public RoSi2Element findElement(final String name) {
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

    public RoSi2Drawable instantiate() throws RoSi2ParseException {
        return instantiate(null);
    }

    public RoSi2Drawable instantiate(final Map<String, String> vars) throws RoSi2ParseException {
        // The instantiation is constant unless it references a variable
        boolean constant = true;

        // Return the constant instance if it exists
        if (constantInstance != null) {
            return constantInstance;
        }

        // Merge the given variable bindings with those of the element
        final Map<String, String> varBindings;
        if (vars != null) {
            varBindings = vars;
            for (final Map.Entry<String, String> entry : this.vars.entrySet()) {
                varBindings.putIfAbsent(entry.getKey(), entry.getValue());
            }
        } else {
            varBindings = new HashMap<String, String>(this.vars);
        }

        // Check if this element references another and in that case instantiate
        // the referenced element
        final String ref = attributes.get("ref");
        if (ref != null) {
            final RoSi2Element referenced = namedElements.get(tag + "#" + ref);
            if (referenced == null) {
                throw new RoSi2ParseException("Referenced element cannot be found: " + ref);
            }
            return referenced.instantiate(varBindings);
        }

        // Instantiate all child elements
        final List<RoSi2Drawable> childInstances = new LinkedList<RoSi2Drawable>();
        for (final RoSi2Element child : children) {
            final RoSi2Drawable childInst = child.instantiate(varBindings);
            if (childInst != null) {
                // If a child instance is not constant, the instance of this
                // element is neither
                if (childInst != child.constantInstance) {
                    constant = false;
                }

                childInstances.add(childInst);
            }
        }

        // Instantiate this element
        final RoSi2Drawable instance;
        if (tag.equals("Compound")) {
            instance = new Compound(childInstances);
        } else if (tag.equals("Body")) {
            instance = new Body(childInstances);
        } else if (tag.equals("Translation")) {
            instance = new Translation(new float[]{
                getLength(varBindings, "x", false, 0.0f),
                getLength(varBindings, "y", false, 0.0f),
                getLength(varBindings, "z", false, 0.0f)
            });
        } else if (tag.equals("Rotation")) {
            instance = new Rotation(new float[]{
                getAngle(varBindings, "x", false, 0.0f),
                getAngle(varBindings, "y", false, 0.0f),
                getAngle(varBindings, "z", false, 0.0f)
            });
        } else if (tag.equals("Appearance")) {
            instance = new Appearance(childInstances);
        } else if (tag.equals("BoxAppearance")) {
            instance = new BoxAppearance(childInstances,
                    getLength(varBindings, "width", true, 0.0f),
                    getLength(varBindings, "height", true, 0.0f),
                    getLength(varBindings, "depth", true, 0.0f));
        } else if (tag.equals("SphereAppearance")) {
            instance = new SphereAppearance(childInstances,
                    getLength(varBindings, "radius", true, 0.0f));
        } else if (tag.equals("CylinderAppearance")) {
            instance = new CylinderAppearance(childInstances,
                    getLength(varBindings, "height", true, 0.0f),
                    getLength(varBindings, "radius", true, 0.0f));
        } else if (tag.equals("CapsuleAppearance")) {
            instance = new CapsuleAppearance(childInstances,
                    getLength(varBindings, "height", true, 0.0f),
                    getLength(varBindings, "radius", true, 0.0f));
        } else if (tag.equals("ComplexAppearance")) {
            instance = new ComplexAppearance(childInstances);
        } else if (tag.equals("Vertices")) {
            instance = new Vertices();
        } else if (tag.equals("Normals")) {
            instance = new Normals();
        } else if (tag.equals("TexCoords")) {
            instance = new TexCoords();
        } else if (tag.equals("Triangles")) {
            instance = new Triangles();
        } else if (tag.equals("Quads")) {
            instance = new Quads();
        } else if (tag.equals("Surface")) {
            instance = new Surface();
        } else {
            return null;
        }

        // Store the instance if it is constant
        if (constant) {
            constantInstance = instance;
        }

        return instance;
    }

    private float getLength(final Map<String, String> varBindings, final String key, final boolean required, float defaultValue) throws RoSi2ParseException {
        float[] value = new float[1];
        String[] unit = new String[1];
        if (!getFloatAndUnit(varBindings, key, required, value, unit)) {
            return defaultValue;
        }

        if (unit[0].isEmpty() || unit[0].equals("m")) {
            return value[0];
        } else if (unit[0].equals("mm")) {
            return value[0] * 0.001f;
        } else if (unit[0].equals("cm")) {
            return value[0] * 0.01f;
        } else if (unit[0].equals("dm")) {
            return value[0] * 0.1f;
        } else if (unit[0].equals("km")) {
            return value[0] * 1000.0f;
        }

        throw new RoSi2ParseException("Unexpected unit \"" + unit[0] + " (expected one of \"mm, cm, dm, m, km\")");
    }

    private float getAngle(final Map<String, String> varBindings, final String key, final boolean required, float defaultValue) throws RoSi2ParseException {
        float[] value = new float[1];
        String[] unit = new String[1];
        if (!getFloatAndUnit(varBindings, key, required, value, unit)) {
            return defaultValue;
        }

        if (unit[0].isEmpty() || unit[0].equals("radian")) {
            return value[0];
        } else if (unit[0].equals("degree")) {
            return value[0] * (float) Math.PI / 180.0f;
        }

        throw new RoSi2ParseException("Unexpected unit \"" + unit[0] + " (expected one of \"degree, radian\")");
    }

    private boolean getFloatAndUnit(final Map<String, String> varBindings, final String key, final boolean required, final float[] value, final String[] unit) throws RoSi2ParseException {
        final String val = getAttributeValue(varBindings, key, required);
        if (val == null) {
            return false;
        }

        // Regex matching the parsing behaviour of Double.valueOf()
        // (see Java Platform SE API Documentation)
        final String Digits = "(\\p{Digit}+)";
        final String HexDigits = "(\\p{XDigit}+)";
        final String Exp = "[eE][+-]?" + Digits;
        final String fpRegex
                = ("[\\x00-\\x20]*"
                + "[+-]?("
                + "NaN|"
                + "Infinity|"
                + "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|"
                + "(\\.(" + Digits + ")(" + Exp + ")?)|"
                + "(("
                + "(0[xX]" + HexDigits + "(\\.)?)|"
                + "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")"
                + ")[pP][+-]?" + Digits + "))"
                + "[fFdD]?))"
                + "[\\x00-\\x20]*");
        final Matcher matcher = Pattern.compile(fpRegex).matcher(val);

        if (matcher.matches()) {
            value[0] = Double.valueOf(val.substring(0, matcher.end())).floatValue();
            unit[0] = val.substring(matcher.end()).trim();
            return true;
        }

        return false;
    }

    private String getAttributeValue(final Map<String, String> varBindings, final String key, final boolean required) throws RoSi2ParseException {
        final String raw = attributes.get(key);
        if (raw == null) {
            if (required) {
                throw new RoSi2ParseException("Missing attribute: " + key);
            }
            return null;
        }
        int varStart = raw.indexOf('$');
        if (varStart < 0) {
            return raw;
        }

        final StringBuilder value = new StringBuilder(raw.length());
        int varEnd = 0;
        while (varStart >= 0) {
            value.append(raw.substring(varEnd, varStart));
            if (varStart + 1 == raw.length()) {
                varEnd = varStart;
                break;
            }

            final char c = raw.charAt(varStart + 1);
            final String varName;
            if (c == '(' || c == '{') {
                final char cEnd = c == '(' ? ')' : '}';
                varEnd = raw.indexOf(cEnd, varStart + 2);
                if (varEnd < 0) {
                    throw new RoSi2ParseException("Invalid attribute format: missing " + cEnd);
                }
                varName = raw.substring(varStart + 2, varEnd);
                varEnd++;
            } else {
                varEnd = varStart + 1;
                while (varEnd < raw.length() && Character.isLetterOrDigit(raw.charAt(varEnd))) {
                    varEnd++;
                }
                varName = raw.substring(varStart + 1, varEnd);
            }

            final String binding = varBindings.get(varName);
            if (binding == null) {
                value.append(raw.substring(varStart, varEnd));
            } else {
                value.append(binding);
            }

            if (varEnd == raw.length()) {
                break;
            } else {
                varStart = raw.indexOf('$', varEnd);
            }
        }
        if (varEnd < raw.length()) {
            value.append(raw.substring(varEnd));
        }

        return value.toString();
    }

    public static abstract class RoSi2Drawable {

        protected final List<RoSi2Drawable> children;
        private final FloatBuffer transformation;

        public RoSi2Drawable() {
            children = new LinkedList<RoSi2Drawable>();
            transformation = null;
        }

        public RoSi2Drawable(final List<RoSi2Drawable> children) throws RoSi2ParseException {
            this.children = children;

            // Find children defining a transformation
            Translation translation = null;
            Rotation rotation = null;
            ListIterator<RoSi2Drawable> iter = children.listIterator();
            while (iter.hasNext()) {
                final RoSi2Drawable cur = iter.next();
                if (cur instanceof Translation) {
                    if (translation != null) {
                        throw new RoSi2ParseException("More than one Translation element");
                    }
                    iter.remove();
                    translation = (Translation) cur;
                } else if (cur instanceof Rotation) {
                    if (rotation != null) {
                        throw new RoSi2ParseException("More than one Rotation element");
                    }
                    iter.remove();
                    rotation = (Rotation) cur;
                }
            }

            // Compute a GL matrix for the transformation
            if (rotation == null && translation == null) {
                transformation = null;
            } else {
                transformation = FloatBuffer.allocate(16);
                if (rotation != null) {
                    final double sx = Math.sin(rotation.rotation[0]);
                    final double sy = Math.sin(rotation.rotation[1]);
                    final double sz = Math.sin(rotation.rotation[2]);
                    final double cx = Math.cos(rotation.rotation[0]);
                    final double cy = Math.cos(rotation.rotation[1]);
                    final double cz = Math.cos(rotation.rotation[2]);
                    transformation.put((float) (cy * cz)) // c0x
                            .put((float) (cy * sz)) // c0y
                            .put((float) (-sy)) // c0z
                            .put(0.0f)
                            .put((float) (sx * sy * cz - cx * sz)) // c1x
                            .put((float) (cx * cz + sx * sy * sz)) // c1y
                            .put((float) (sx * cy)) // c1z
                            .put(0.0f)
                            .put((float) (sx * sz + cx * sy * cz)) // c2x
                            .put((float) (cx * sy * sz - sx * cz)) // c2y
                            .put((float) (cx * cy)) // c2z
                            .put(0.0f);
                } else {
                    transformation.put(new float[]{1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f});
                }
                if (translation != null) {
                    transformation.put(translation.translation);
                } else {
                    transformation.put(new float[]{0.0f, 0.0f, 0.0f});
                }
                transformation.put(1.0f);
            }
        }

        /**
         * Draws this element and its children using the given GL object.
         *
         * @param gl GL object
         */
        public final void draw(final GL2 gl) {
            // Apply transformation
            if (transformation != null) {
                gl.glPushMatrix();
                gl.glMultMatrixf(transformation);
            }

            // Draw this element
            render(gl);

            // Draw children
            for (final RoSi2Drawable child : children) {
                child.draw(gl);
            }

            // Reset transformation
            if (transformation != null) {
                gl.glPopMatrix();
            }
        }

        /**
         * Draws this element using the given GL object.
         *
         * @param gl GL object
         */
        protected abstract void render(final GL2 gl);

        /**
         * Creates a display list on the given GL object which renders the
         * element. The display list will not be destroyed, so this object may
         * safely be garbage collected afterwards.
         *
         * @param gl GL object
         * @return number of the created display list
         */
        public final int createDisplayList(final GL2 gl) {
            final int listId = gl.glGenLists(1);

            gl.glNewList(listId, GL2.GL_COMPILE);
            draw(gl);
            gl.glEndList();

            return listId;
        }
    }

    public static class Compound extends RoSi2Drawable {

        public Compound(final List<RoSi2Drawable> children) throws RoSi2ParseException {
            super(children);
        }

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class Body extends RoSi2Drawable {

        public Body(final List<RoSi2Drawable> children) throws RoSi2ParseException {
            super(children);
        }

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }
    }

    public static class Translation extends RoSi2Drawable {

        public final float[] translation;

        public Translation(final float[] translation) {
            this.translation = translation;
        }

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class Rotation extends RoSi2Drawable {

        public final float[] rotation;

        public Rotation(final float[] rotation) {
            this.rotation = rotation;
        }

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class Appearance extends RoSi2Drawable {

        protected final Surface surface;

        public Appearance(final List<RoSi2Drawable> children) throws RoSi2ParseException {
            super(children);

            Surface s = null;
            ListIterator<RoSi2Drawable> iter = this.children.listIterator();
            while (iter.hasNext()) {
                final RoSi2Drawable cur = iter.next();
                if (cur instanceof Surface) {
                    if (s != null) {
                        throw new RoSi2ParseException("More than one Surface element");
                    }
                    s = (Surface) cur;
                    iter.remove();
                }
            }
            surface = s;
        }

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class BoxAppearance extends Appearance {

        /**
         * The width of the box (cy).
         */
        private final float width;

        /**
         * The height of the box (cz).
         */
        private final float height;

        /**
         * The depth of the box (cx).
         */
        private final float depth;

        public BoxAppearance(final List<RoSi2Drawable> children, final float width, final float height, final float depth) throws RoSi2ParseException {
            super(children);

            this.width = width;
            this.height = height;
            this.depth = depth;
        }

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class SphereAppearance extends Appearance {

        /**
         * The radius of the sphere.
         */
        private final float radius;

        public SphereAppearance(final List<RoSi2Drawable> children, final float radius) throws RoSi2ParseException {
            super(children);

            this.radius = radius;
        }

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class CylinderAppearance extends Appearance {

        /**
         * The height of the cylinder.
         */
        private final float height;

        /**
         * The radius.
         */
        private final float radius;

        public CylinderAppearance(final List<RoSi2Drawable> children, final float height, final float radius) throws RoSi2ParseException {
            super(children);

            this.height = height;
            this.radius = radius;
        }

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class CapsuleAppearance extends Appearance {

        /**
         * The height of the capsule.
         */
        private final float height;

        /**
         * The radius.
         */
        private final float radius;

        public CapsuleAppearance(final List<RoSi2Drawable> children, final float height, final float radius) throws RoSi2ParseException {
            super(children);

            this.height = height;
            this.radius = radius;
        }

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class ComplexAppearance extends Appearance {

        public ComplexAppearance(final List<RoSi2Drawable> children) throws RoSi2ParseException {
            super(children);
        }

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class Vertices extends RoSi2Drawable {

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class Normals extends RoSi2Drawable {

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class TexCoords extends RoSi2Drawable {

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class Triangles extends RoSi2Drawable {

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class Quads extends RoSi2Drawable {

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

    }

    public static class Surface extends RoSi2Drawable {

        @Override
        protected void render(final GL2 gl) {
            // Do nothing
        }

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
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

        // Stack containing the opened files
        final Deque<InputFileState> inputFileStack = new LinkedList<InputFileState>();

        // Stack containing the current element hierarchy
        final Deque<RoSi2Element> parentStack = new LinkedList<RoSi2Element>();

        // Map with mappings of all named elements
        final Map<String, RoSi2Element> namedElements = new HashMap<String, RoSi2Element>();

        // Open the given file
        inputFileStack.addFirst(new InputFileState(factory, new File(filename)));

        // Create the root element
        parentStack.addFirst(new RoSi2Element("Simulation", namedElements));

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
                            final RoSi2Element elem = new RoSi2Element(tag, name, e.getAttributes(), namedElements);
                            if (name != null) {
                                namedElements.put(tag + '#' + name, elem);
                            }

                            parentStack.getFirst().children.add(elem);
                            parentStack.addFirst(elem);
                        }
                    }
                } else if (ev.isAttribute()) {
                    parentStack.getFirst().attributes.put(((Attribute) ev).getName().getLocalPart(), ((Attribute) ev).getValue());
                } else if (ev.isCharacters()) {
                    final Characters e = ev.asCharacters();
                    if (!e.isWhiteSpace()) {
                        parentStack.getFirst().content.append(e.getData());
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

        // Find the Scene element
        for (final RoSi2Element cur : parentStack.getFirst().children) {
            if (cur.tag.equals("Scene")) {
                return cur;
            }
        }

        // If no Scene element exists, return the root instead
        return parentStack.getFirst();
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
