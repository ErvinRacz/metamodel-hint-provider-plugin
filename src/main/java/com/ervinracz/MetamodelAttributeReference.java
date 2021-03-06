package com.ervinracz;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.model.SymbolResolveResult;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;

public class MetamodelAttributeReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    private Project project;
    private String searchKey;
    private String metamodelClassName;

    public MetamodelAttributeReference(@NotNull PsiElement element, TextRange rangeInElement, @NotNull String metamodelClassName) {
        super(element, rangeInElement);
        this.searchKey = element.getText().substring(rangeInElement.getStartOffset(), rangeInElement.getEndOffset());
        this.project = element.getProject();
        this.metamodelClassName = metamodelClassName;
    }

    public static PsiField[] findFieldsOfClass(Project project, @NotNull String className, @NotNull String searchKey) {
        try {
            return Arrays.stream(
                    MetamodelClassReference.findClasses(project, className)
                            .stream()
                            .findFirst()
                            .orElseThrow(NoSuchElementException::new)
                            .getAllFields())
                    .filter(psiField -> searchKey.isEmpty() || searchKey.equals(psiField.getName()))
                    .toArray(PsiField[]::new);
        } catch (NoSuchElementException e) {
            return new PsiField[]{};
        }
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return Arrays.stream(findFieldsOfClass(project, metamodelClassName, searchKey))
                .map(PsiElementResolveResult::new)
                .toArray(ResolveResult[]::new);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return Arrays.stream(findFieldsOfClass(project, metamodelClassName, StringUtils.EMPTY))
                .map(LookupElementBuilder::create)
                .toArray(LookupElement[]::new);
    }

    @NotNull
    @Override
    public Collection<? extends SymbolResolveResult> resolveReference() {
        return null;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        return getElement();
    }
}
