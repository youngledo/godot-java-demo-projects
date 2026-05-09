package demos.gui.regex;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.collection.GodotArray;
import org.godot.math.Color;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.node.LineEdit;
import org.godot.node.Node;
import org.godot.node.RegEx;
import org.godot.node.RegExMatch;
import org.godot.node.TextEdit;
import org.godot.node.VBoxContainer;
import org.godot.singleton.OS;

@GodotClass(name = "Regex", parent = "VBoxContainer")
public class Regex extends VBoxContainer {

    private RegEx regex;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        regex = RegEx.create();
        setText("HBoxContainer2/Text", "They asked me \"What's going on \\\"in the manor\\\"?\"");
        updateExpression(getText("HBoxContainer/Expression"));
    }

    private void updateExpression(String text) {
        if (regex != null) {
            regex.compile(text);
        }
        updateText();
    }

    private void updateText() {
        Node list = getNode("ScrollContainer/List");
        if (list == null) return;

        for (Node child : list.getChildren()) {
            child.queueFree();
        }

        Control hbox = getNodeAs("HBoxContainer", Control.class);
        if (regex != null && regex.isValid()) {
            if (hbox != null) hbox.setModulate(new Color(1, 1, 1));

            RegExMatch[] matches = regex.searchAll(getText("HBoxContainer2/Text"));
            if (matches.length >= 1) {
                int matchNumber = 0;
                for (RegExMatch match : matches) {
                    matchNumber++;
                    Label matchLabel = Label.create();
                    matchLabel.setText("RegEx match #" + matchNumber + ":");
                    matchLabel.setModulate(new Color(0.6, 0.9, 1.0));
                    list.addChild(matchLabel);

                    GodotArray strings = match.getStrings();
                    int captureNumber = 0;
                    for (int i = 0; i < strings.size(); i++) {
                        captureNumber++;
                        Label captureLabel = Label.create();
                        captureLabel.setText("    Capture group #" + captureNumber + ": " + strings.get(i));
                        list.addChild(captureLabel);
                    }
                }
            }
        } else {
            if (hbox != null) hbox.setModulate(new Color(1, 0.2, 0.1));
            Label label = Label.create();
            label.setText("Error: Invalid regular expression. Check if the expression is correctly escaped and terminated.");
            list.addChild(label);
        }
    }

    @GodotMethod
    public void OnHelpMetaClicked(Object meta) {
        OS.singleton().shellOpen("https://regexr.com");
    }

    private String getText(String path) {
        Node node = getNode(path);
        if (node instanceof LineEdit lineEdit) return lineEdit.getText();
        if (node instanceof TextEdit textEdit) return textEdit.getText();
        if (node instanceof Label label) return label.getText();
        return "";
    }

    private void setText(String path, String text) {
        Node node = getNode(path);
        if (node instanceof LineEdit lineEdit) lineEdit.setText(text);
        if (node instanceof TextEdit textEdit) textEdit.setText(text);
        if (node instanceof Label label) label.setText(text);
    }
}
