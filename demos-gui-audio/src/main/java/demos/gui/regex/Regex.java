package demos.gui.regex;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.Label;
import org.godot.node.VBoxContainer;
import org.godot.node.Node;

@GodotClass(name = "Regex", parent = "VBoxContainer")
public class Regex extends VBoxContainer {

    private org.godot.node.RegEx regex;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        regex = (org.godot.node.RegEx) org.godot.singleton.ClassDB.singleton().call("instantiate", "RegEx"); // Create RegEx object
        org.godot.node.Node textNode = getNode("HBoxContainer2/Text");
        if (textNode != null) {
            textNode.call("set_text", "They asked me \"What's going on \\\"in the manor\\\"?\"");
        }
        org.godot.node.Node expressionNode = getNode("HBoxContainer/Expression");
        if (expressionNode != null) {
            String exprText = (String) expressionNode.getProperty("text");
            updateExpression(exprText);
        }
    }

    private void updateExpression(String text) {
        if (regex != null) {
            regex.compile(text);
        }
        updateText();
    }

    private void updateText() {
        org.godot.node.Node list = getNode("ScrollContainer/List");
        if (list == null) return;

        // Clear children
        org.godot.node.Node[] children = list.getChildren();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                org.godot.node.Node child = children[i];
                if (child instanceof org.godot.Godot) {
                    ((org.godot.Godot) child).call("queue_free");
                }
            }
        }

        if (regex != null && (boolean) regex.call("is_valid")) {
            org.godot.node.Node hbox = getNode("HBoxContainer");
            if (hbox != null) hbox.setProperty("modulate", new Color(1, 1, 1));

            org.godot.node.Node textNode = getNode("HBoxContainer2/Text");
            String text = textNode != null ? (String) textNode.call("get_text") : "";

            org.godot.collection.GodotArray matches = (org.godot.collection.GodotArray) regex.call("search_all", text);
            if (matches != null && matches.size() >= 1) {
                int matchNumber = 0;
                for (int mi = 0; mi < matches.size(); mi++) {
                    Object matchObj = matches.get(mi);
                    matchNumber++;
                    org.godot.Godot matchLabel = (org.godot.Godot) Label.create();
                    matchLabel.setProperty("text", "RegEx match #" + matchNumber + ":");
                    matchLabel.setProperty("modulate", new Color(0.6, 0.9, 1.0));
                    list.addChild((org.godot.node.Node) matchLabel);

                    org.godot.collection.GodotArray strings = (org.godot.collection.GodotArray) ((org.godot.Godot) matchObj).call("get_strings");
                    if (strings != null) {
                        int captureNumber = 0;
                        for (int si = 0; si < strings.size(); si++) {
                            Object result = strings.get(si);
                            captureNumber++;
                            org.godot.Godot captureLabel = (org.godot.Godot) Label.create();
                            captureLabel.setProperty("text", "    Capture group #" + captureNumber + ": " + result);
                            list.addChild((org.godot.node.Node) captureLabel);
                        }
                    }
                }
            }
        } else {
            org.godot.node.Node hbox = getNode("HBoxContainer");
            if (hbox != null) hbox.setProperty("modulate", new Color(1, 0.2, 0.1));
            org.godot.Godot label = (org.godot.Godot) Label.create();
            label.setProperty("text", "Error: Invalid regular expression. Check if the expression is correctly escaped and terminated.");
            list.addChild((org.godot.node.Node) label);
        }
    }

    @GodotMethod
    public void OnHelpMetaClicked(Object meta) {
        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        os.shellOpen("https://regexr.com");
    }
}
