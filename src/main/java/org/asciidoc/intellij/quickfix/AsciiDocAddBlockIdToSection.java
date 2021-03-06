package org.asciidoc.intellij.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.asciidoc.intellij.AsciiDocBundle;
import org.asciidoc.intellij.psi.AsciiDocFileReference;
import org.asciidoc.intellij.psi.AsciiDocSection;
import org.asciidoc.intellij.psi.AsciiDocUtil;
import org.asciidoc.intellij.psi.HasAnchorReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexander Schwartz 2020
 */
public class AsciiDocAddBlockIdToSection implements LocalQuickFix {

  @Override
  public @IntentionFamilyName @NotNull String getFamilyName() {
    return AsciiDocBundle.message("asciidoc.quickfix.addBlockIdToSection");
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiElement element = descriptor.getPsiElement();
    if (element instanceof HasAnchorReference) {
      AsciiDocSection section = ((HasAnchorReference) element).resolveAnchorForSection();
      if (section != null) {
        if (section.getBlockId() == null) {
          PsiElement firstChild = section.getFirstChild();
          String id = section.getAutogeneratedId();
          AsciiDocFileReference reference = ((HasAnchorReference) element).getAnchorReference();
          if (reference != null && !reference.isPossibleRefText()) {
            id = reference.getRangeInElement().substring(element.getText());
          }
          for (PsiElement child : createBlockId(project,
            "[#" + id + "]").getChildren()) {
            section.addBefore(child,
              firstChild);
          }
        }
      }
    }
  }

  @NotNull
  private static PsiElement createBlockId(@NotNull Project project, @NotNull String text) {
    return AsciiDocUtil.createFileFromText(project, text);
  }
}
