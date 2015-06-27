package org.asciidoc.intellij.toolbar;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import org.asciidoc.intellij.AsciiDocLanguage;
import org.jetbrains.annotations.NotNull;

public class AsciiDocToolbarLoaderComponent implements ProjectComponent {

  public static final Key<AsciiDocToolbarPanel> ASCII_DOC_TOOLBAR = Key.create("AsciiDocToolbar");

  private Project myProject;

  public AsciiDocToolbarLoaderComponent(Project project) {
    this.myProject = project;
  }

  @Override
  public void initComponent() {
    myProject.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new AsciiDocFileEditorManagerListener());
  }

  @Override
  public void disposeComponent() {
  }

  @Override
  @NotNull
  public String getComponentName() {
    return "AsciiDocProjectComponent";
  }

  @Override
  public void projectOpened() {
    // called when project is opened
  }

  @Override
  public void projectClosed() {
    // called when project is being closed
  }


  /** inspired by {@link com.intellij.xml.breadcrumbs.BreadcrumbsLoaderComponent.MyFileEditorManagerListener} */
  private static class AsciiDocFileEditorManagerListener extends FileEditorManagerAdapter {

    @Override
    /** called on EDT */
    public void fileOpened(@NotNull final FileEditorManager source, @NotNull final VirtualFile file) {
      if (isSuitable(source.getProject(), file)) {
        final FileEditor[] fileEditors = source.getAllEditors(file);
        for (final FileEditor fileEditor : fileEditors) {
          if (fileEditor instanceof TextEditor) {
            Editor editor = ((TextEditor)fileEditor).getEditor();
            if (editor.getUserData(ASCII_DOC_TOOLBAR) != null) {
              continue;
            }

            final AsciiDocToolbarPanel asciiDocToolbarPanel = new AsciiDocToolbarPanel(editor);

            source.addTopComponent(fileEditor, asciiDocToolbarPanel);
            Disposer.register(fileEditor, asciiDocToolbarPanel);
            Disposer.register(fileEditor, new Disposable() {
              @Override
              public void dispose() {
                source.removeTopComponent(fileEditor, asciiDocToolbarPanel);
              }
            });
          }
        }
      }
    }

    private static boolean isSuitable(final Project project, final VirtualFile file) {
      final FileViewProvider provider = PsiManager.getInstance(project).findViewProvider(file);
      return provider != null && provider.getBaseLanguage().isKindOf(AsciiDocLanguage.INSTANCE);
    }
  }
}