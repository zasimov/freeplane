package org.freeplane.plugin.markdown;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.JRestrictedSizeScrollPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.NoteModel;
import org.freeplane.features.text.AbstractContentTransformer;
import org.freeplane.features.text.DetailModel;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.TransformationException;
import org.freeplane.features.text.mindmapmode.EditNodeBase;
import org.freeplane.features.text.mindmapmode.EditNodeBase.IEditControl;
import org.freeplane.features.text.mindmapmode.EditNodeDialog;
import org.freeplane.features.text.mindmapmode.IEditBaseCreator;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.features.text.mindmapmode.SourceTextEditorUIConfigurator;

import io.github.gitbucket.markedj.Marked;
import io.github.gitbucket.markedj.Options;

public class MarkdownRenderer extends AbstractContentTransformer implements IEditBaseCreator {

	private static final String MARKDOWN_EDITOR_FONT_SIZE = "markdown_editor_font_size";
	private static final String MARKDOWN_EDITOR_FONT = "markdown_editor_font";
	private static final String MARKDOWN_EDITOR_DISABLE_INLINE = "markdown_disable_editor";
	private static final String MARKDOWN_EDITOR_INITIAL_WIDTH = "markdown_editor_initial_width";
	private static final String MARKDOWN_EDITOR_INITIAL_HEIGHT = "markdown_editor_initial_height";
	static final String MARKDOWN_CONTENT_TYPE = "markdown";
	static final String MARKDOWN_FORMAT = "markdownPatternFormat";
    private final Options options;

    private Options createMarkdownOptions() {
        Options options = new Options();
        options.setSafelist(null);
        return options;
    }

	public MarkdownRenderer() {
		super(30);
		options = createMarkdownOptions();
	}

	@Override
	public Object transformContent(NodeModel node,
			Object nodeProperty, Object content, TextController textController, Mode mode)
			throws TransformationException {
	    if(mode == Mode.TEXT)
	        return content;
        String text = getText(node, nodeProperty, content, textController);
		if(text == null)
        	return content;

        String markdown = text;
        String html = "<html>"
        		+ "<body>"
        		+ Marked.marked(markdown, options)
        		+ "</body></html>";
        return html;
	}

    @Override
    public EditNodeBase createEditor(final NodeModel node, Object nodeProperty,
            Object content, final EditNodeBase.IEditControl editControl, final boolean editLong) {
        JEditorPane textEditor = createTextEditorPane(this::createScrollPane, node, nodeProperty, content, ! editLong);
        return textEditor == null ? null :createEditor(node, editControl, textEditor);
    }

    private JRestrictedSizeScrollPane createScrollPane() {
        final JRestrictedSizeScrollPane scrollPane = new JRestrictedSizeScrollPane();
        UITools.setScrollbarIncrement(scrollPane);
        final int scrollPaneWidth = ResourceController.getResourceController().getIntProperty(MARKDOWN_EDITOR_INITIAL_WIDTH);
        final int scrollPaneHeight = ResourceController.getResourceController().getIntProperty(MARKDOWN_EDITOR_INITIAL_HEIGHT);
        scrollPane.setMinimumSize(new Dimension(scrollPaneWidth, scrollPaneHeight));
        return scrollPane;
    }

    @Override
    public JEditorPane createTextEditorPane(Supplier<JScrollPane> scrollPaneSupplier, final NodeModel node, Object nodeProperty,
            Object content, boolean editInline) {
		String text = getText(node, nodeProperty, content, MTextController.getController());
        if(text == null)
            return null;
        if (editInline && ResourceController.getResourceController().getBooleanProperty(MARKDOWN_EDITOR_DISABLE_INLINE))
        	return null;
		// this option has been added to work around bugs in JSyntaxPane with Chinese characters
		JEditorPane textEditor = new JEditorPane();
		scrollPaneSupplier.get().setViewportView(textEditor);
		textEditor.setContentType("text/markdown");
		textEditor.setText(text);
		SourceTextEditorUIConfigurator.configureColors(textEditor);
		final String fontName = ResourceController.getResourceController().getProperty(MARKDOWN_EDITOR_FONT);
		final int fontSize = ResourceController.getResourceController().getIntProperty(MARKDOWN_EDITOR_FONT_SIZE);
		final Font font = UITools.scaleUI(new Font(fontName, Font.PLAIN, fontSize));
		textEditor.setFont(font);
		return textEditor;
	}

    private EditNodeBase createEditor(NodeModel node, IEditControl editControl,
            JEditorPane textEditor) {
        final KeyEvent firstKeyEvent = MTextController.getController().getEventQueue().getFirstEvent();
		final EditNodeDialog editNodeDialog = new EditNodeDialog(node, firstKeyEvent, false, editControl, false, textEditor);
		editNodeDialog.setTitle(TextUtils.getText("markdown_editor"));
		return editNodeDialog;
    }

	private String getText(NodeModel node, Object nodeProperty, Object content, TextController textController) {
		if(! (content instanceof String))
			return null;
		NoteController noteController = NoteController.getController();
		if (!(nodeProperty instanceof NodeModel
				&& MarkdownRenderer.MARKDOWN_FORMAT.equals(textController.getNodeFormat(node))
				|| nodeProperty instanceof DetailModel && MarkdownRenderer.MARKDOWN_CONTENT_TYPE.equals(textController.getDetailsContentType(node))
		        || nodeProperty instanceof NoteModel && MarkdownRenderer.MARKDOWN_CONTENT_TYPE.equals(noteController.getNoteContentType(node))))
		    return  null;
		String plainOrHtmlText = (String) content;
		String text = HtmlUtils.htmlToPlain(plainOrHtmlText);
		return text;
	}
}
