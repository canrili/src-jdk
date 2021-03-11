/*
 * Copyright (c) 2003, 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.sun.tools.doclets.internal.toolkit.builders;

import java.util.*;
import com.sun.tools.doclets.internal.toolkit.util.*;
import com.sun.tools.doclets.internal.toolkit.*;
import com.sun.javadoc.*;
import java.text.MessageFormat;

/**
 * Builds the member summary.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 *
 * @author Jamie Ho
 * @author Bhavesh Patel (Modified)
 * @since 1.5
 */
public class MemberSummaryBuilder extends AbstractMemberBuilder {

    /**
     * The XML root for this builder.
     */
    public static final String NAME = "MemberSummary";

    /**
     * The visible members for the given class.
     */
    private VisibleMemberMap[] visibleMemberMaps;

    /**
     * The member summary writers for the given class.
     */
    private MemberSummaryWriter[] memberSummaryWriters;

    /**
     * The type being documented.
     */
    private ClassDoc classDoc;

    private MemberSummaryBuilder(Configuration configuration) {
        super(configuration);
    }

    /**
     * Construct a new MemberSummaryBuilder.
     *
     * @param classWriter   the writer for the class whose members are being
     *                      summarized.
     * @param configuration the current configuration of the doclet.
     */
    public static MemberSummaryBuilder getInstance(
            ClassWriter classWriter, Configuration configuration)
            throws Exception {
        MemberSummaryBuilder builder = new MemberSummaryBuilder(configuration);
        builder.classDoc = classWriter.getClassDoc();
        builder.init(classWriter);
        return builder;
    }

    /**
     * Construct a new MemberSummaryBuilder.
     *
     * @param annotationTypeWriter the writer for the class whose members are
     *                             being summarized.
     * @param configuration the current configuration of the doclet.
     */
    public static MemberSummaryBuilder getInstance(
            AnnotationTypeWriter annotationTypeWriter, Configuration configuration)
            throws Exception {
        MemberSummaryBuilder builder = new MemberSummaryBuilder(configuration);
        builder.classDoc = annotationTypeWriter.getAnnotationTypeDoc();
        builder.init(annotationTypeWriter);
        return builder;
    }

    private void init(Object writer) throws Exception {
        visibleMemberMaps =
                new VisibleMemberMap[VisibleMemberMap.NUM_MEMBER_TYPES];
        for (int i = 0; i < VisibleMemberMap.NUM_MEMBER_TYPES; i++) {
            visibleMemberMaps[i] =
                    new VisibleMemberMap(
                    classDoc,
                    i,
                    configuration.nodeprecated);
        }
        memberSummaryWriters =
                new MemberSummaryWriter[VisibleMemberMap.NUM_MEMBER_TYPES];
        for (int i = 0; i < VisibleMemberMap.NUM_MEMBER_TYPES; i++) {
            if (classDoc.isAnnotationType()) {
                memberSummaryWriters[i] =
                    visibleMemberMaps[i].noVisibleMembers()?
                        null :
                        configuration.getWriterFactory().getMemberSummaryWriter(
                        (AnnotationTypeWriter) writer, i);
            } else {
                memberSummaryWriters[i] =
                    visibleMemberMaps[i].noVisibleMembers()?
                        null :
                        configuration.getWriterFactory().getMemberSummaryWriter(
                        (ClassWriter) writer, i);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return NAME;
    }

    /**
     * Return the specified visible member map.
     *
     * @param type the type of visible member map to return.
     * @return the specified visible member map.
     * @throws ArrayIndexOutOfBoundsException when the type is invalid.
     * @see VisibleMemberMap
     */
    public VisibleMemberMap getVisibleMemberMap(int type) {
        return visibleMemberMaps[type];
    }

    /**
     * Return the specified member summary writer.
     *
     * @param type the type of member summary writer to return.
     * @return the specified member summary writer.
     * @throws ArrayIndexOutOfBoundsException when the type is invalid.
     * @see VisibleMemberMap
     */
    public MemberSummaryWriter getMemberSummaryWriter(int type) {
        return memberSummaryWriters[type];
    }

    /**
     * Returns a list of methods that will be documented for the given class.
     * This information can be used for doclet specific documentation
     * generation.
     *
     * @param classDoc the {@link ClassDoc} we want to check.
     * @param type the type of members to return.
     * @return a list of methods that will be documented.
     * @see VisibleMemberMap
     */
    public List<ProgramElementDoc> members(int type) {
        return visibleMemberMaps[type].getLeafClassMembers(configuration);
    }

    /**
     * Return true it there are any members to summarize.
     *
     * @return true if there are any members to summarize.
     */
    public boolean hasMembersToDocument() {
        if (classDoc instanceof AnnotationTypeDoc) {
            return ((AnnotationTypeDoc) classDoc).elements().length > 0;
        }
        for (int i = 0; i < VisibleMemberMap.NUM_MEMBER_TYPES; i++) {
            VisibleMemberMap members = visibleMemberMaps[i];
            if (!members.noVisibleMembers()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Build the summary for the enum constants.
     *
     * @param node the XML element that specifies which components to document
     * @param memberSummaryTree the content tree to which the documentation will be added
     */
    public void buildEnumConstantsSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.ENUM_CONSTANTS];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.ENUM_CONSTANTS];
        addSummary(writer, visibleMemberMap, false, memberSummaryTree);
    }

    /**
     * Build the summary for the optional members.
     *
     * @param node the XML element that specifies which components to document
     * @param memberSummaryTree the content tree to which the documentation will be added
     */
    public void buildAnnotationTypeOptionalMemberSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_OPTIONAL];
        addSummary(writer, visibleMemberMap, false, memberSummaryTree);
    }

    /**
     * Build the summary for the optional members.
     *
     * @param node the XML element that specifies which components to document
     * @param memberSummaryTree the content tree to which the documentation will be added
     */
    public void buildAnnotationTypeRequiredMemberSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.ANNOTATION_TYPE_MEMBER_REQUIRED];
        addSummary(writer, visibleMemberMap, false, memberSummaryTree);
    }

    /**
     * Build the summary for the fields.
     *
     * @param node the XML element that specifies which components to document
     * @param memberSummaryTree the content tree to which the documentation will be added
     */
    public void buildFieldsSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.FIELDS];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.FIELDS];
        addSummary(writer, visibleMemberMap, true, memberSummaryTree);
    }

    /**
     * Build the summary for the fields.
     */
    public void buildPropertiesSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.PROPERTIES];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.PROPERTIES];
        addSummary(writer, visibleMemberMap, true, memberSummaryTree);
    }

    /**
     * Build the summary for the nested classes.
     *
     * @param node the XML element that specifies which components to document
     * @param memberSummaryTree the content tree to which the documentation will be added
     */
    public void buildNestedClassesSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.INNERCLASSES];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.INNERCLASSES];
        addSummary(writer, visibleMemberMap, true, memberSummaryTree);
    }

    /**
     * Build the method summary.
     *
     * @param node the XML element that specifies which components to document
     * @param memberSummaryTree the content tree to which the documentation will be added
     */
    public void buildMethodsSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.METHODS];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.METHODS];
        addSummary(writer, visibleMemberMap, true, memberSummaryTree);
    }

    /**
     * Build the constructor summary.
     *
     * @param node the XML element that specifies which components to document
     * @param memberSummaryTree the content tree to which the documentation will be added
     */
    public void buildConstructorsSummary(XMLNode node, Content memberSummaryTree) {
        MemberSummaryWriter writer =
                memberSummaryWriters[VisibleMemberMap.CONSTRUCTORS];
        VisibleMemberMap visibleMemberMap =
                visibleMemberMaps[VisibleMemberMap.CONSTRUCTORS];
        addSummary(writer, visibleMemberMap, false, memberSummaryTree);
    }

    /**
     * Build the member summary for the given members.
     *
     * @param writer the summary writer to write the output.
     * @param visibleMemberMap the given members to summarize.
     * @param summaryTreeList list of content trees to which the documentation will be added
     */
    private void buildSummary(MemberSummaryWriter writer,
            VisibleMemberMap visibleMemberMap, LinkedList<Content> summaryTreeList) {
        List<ProgramElementDoc> members = new ArrayList<ProgramElementDoc>(visibleMemberMap.getLeafClassMembers(
                configuration));
        if (members.size() > 0) {
            Collections.sort(members);
            Content tableTree = writer.getSummaryTableTree(classDoc);
            for (int i = 0; i < members.size(); i++) {
                ProgramElementDoc member = members.get(i);
                final ProgramElementDoc propertyDoc =
                            visibleMemberMap.getPropertyMemberDoc(member);
                if (propertyDoc != null) {
                    processProperty(visibleMemberMap, member, propertyDoc);
                }
                Tag[] firstSentenceTags = member.firstSentenceTags();
                if (member instanceof MethodDoc && firstSentenceTags.length == 0) {
                    //Inherit comments from overriden or implemented method if
                    //necessary.
                    DocFinder.Output inheritedDoc =
                            DocFinder.search(new DocFinder.Input((MethodDoc) member));
                    if (inheritedDoc.holder != null &&
                            inheritedDoc.holder.firstSentenceTags().length > 0) {
                        firstSentenceTags = inheritedDoc.holder.firstSentenceTags();
                    }
                }
                writer.addMemberSummary(classDoc, member, firstSentenceTags, tableTree, i);
            }
            summaryTreeList.add(tableTree);
        }
    }

    /**
     * Process the property method, property setter and/or property getter
     * comment text so that it contains the documentation from
     * the property field. The method adds the leading sentence,
     * copied documentation including the defaultValue tag and
     * the see tags if the appropriate property getter and setter are
     * available.
     *
     * @param visibleMemberMap the members information.
     * @param member the member which is to be augmented.
     * @param propertyDoc the original property documentation.
     */
    private void processProperty(VisibleMemberMap visibleMemberMap,
                                 ProgramElementDoc member,
                                 ProgramElementDoc propertyDoc) {
        StringBuilder commentTextBuilder = new StringBuilder();
        final boolean isSetter = isSetter(member);
        final boolean isGetter = isGetter(member);
        if (isGetter || isSetter) {
            //add "[GS]ets the value of the property PROPERTY_NAME."
            if (isSetter) {
                commentTextBuilder.append(
                        MessageFormat.format(
                                Util.RESOURCE_BUNDLE.getString("doclet.PropertySetterWithName"),
                                Util.propertyNameFromMethodName(member.name())));
            }
            if (isGetter) {
                commentTextBuilder.append(
                        MessageFormat.format(
                                Util.RESOURCE_BUNDLE.getString("doclet.PropertyGetterWithName"),
                                Util.propertyNameFromMethodName(member.name())));
            }
            if (propertyDoc.commentText() != null
                        && !propertyDoc.commentText().isEmpty()) {
                commentTextBuilder.append(" \n @propertyDescription ");
            }
        }
        commentTextBuilder.append(propertyDoc.commentText());

        Tag[] tags = propertyDoc.tags("@defaultValue");
        if (tags != null) {
            for (Tag tag: tags) {
                commentTextBuilder.append("\n")
                                  .append(tag.name())
                                  .append(" ")
                                  .append(tag.text());
            }
        }

        //add @see tags
        if (!isGetter && !isSetter) {
            MethodDoc getter = (MethodDoc) visibleMemberMap.getGetterForProperty(member);
            MethodDoc setter = (MethodDoc) visibleMemberMap.getSetterForProperty(member);

            if ((null != getter)
                    && (commentTextBuilder.indexOf("@see #" + getter.name()) == -1)) {
                commentTextBuilder.append("\n @see #")
                                  .append(getter.name())
                                  .append("() ");
            }

            if ((null != setter)
                    && (commentTextBuilder.indexOf("@see #" + setter.name()) == -1)) {
                String typeName = setter.parameters()[0].typeName();
                // Removal of type parameters and package information.
                typeName = typeName.split("<")[0];
                if (typeName.contains(".")) {
                    typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
                }
                commentTextBuilder.append("\n @see #").append(setter.name());

                if (setter.parameters()[0].type().asTypeVariable() == null) {
                    commentTextBuilder.append("(").append(typeName).append(")");
                }
                commentTextBuilder.append(" \n");
            }
        }
        member.setRawCommentText(commentTextBuilder.toString());
    }
    /**
     * Test whether the method is a getter.
     * @param ped property method documentation. Needs to be either property
     * method, property getter, or property setter.
     * @return true if the given documentation belongs to a getter.
     */
    private boolean isGetter(ProgramElementDoc ped) {
        final String pedName = ped.name();
        return pedName.startsWith("get") || pedName.startsWith("is");
    }

    /**
     * Test whether the method is a setter.
     * @param ped property method documentation. Needs to be either property
     * method, property getter, or property setter.
     * @return true if the given documentation belongs to a setter.
     */
    private boolean isSetter(ProgramElementDoc ped) {
        return ped.name().startsWith("set");
    }

    /**
     * Build the inherited member summary for the given methods.
     *
     * @param writer the writer for this member summary.
     * @param visibleMemberMap the map for the members to document.
     * @param summaryTreeList list of content trees to which the documentation will be added
     */
    private void buildInheritedSummary(MemberSummaryWriter writer,
            VisibleMemberMap visibleMemberMap, LinkedList<Content> summaryTreeList) {
        for (Iterator<ClassDoc> iter = visibleMemberMap.getVisibleClassesList().iterator();
                iter.hasNext();) {
            ClassDoc inhclass = iter.next();
            if (! (inhclass.isPublic() ||
                    Util.isLinkable(inhclass, configuration))) {
                continue;
            }
            if (inhclass == classDoc) {
                continue;
            }
            List<ProgramElementDoc> inhmembers = visibleMemberMap.getMembersFor(inhclass);
            if (inhmembers.size() > 0) {
                Collections.sort(inhmembers);
                Content inheritedTree = writer.getInheritedSummaryHeader(inhclass);
                Content linksTree = writer.getInheritedSummaryLinksTree();
                for (int j = 0; j < inhmembers.size(); ++j) {
                    writer.addInheritedMemberSummary(
                            inhclass.isPackagePrivate() &&
                            ! Util.isLinkable(inhclass, configuration) ?
                            classDoc : inhclass,
                            inhmembers.get(j),
                            j == 0,
                            j == inhmembers.size() - 1, linksTree);
                }
                inheritedTree.addContent(linksTree);
                summaryTreeList.add(writer.getMemberTree(inheritedTree));
            }
        }
    }

    /**
     * Add the summary for the documentation.
     *
     * @param writer the writer for this member summary.
     * @param visibleMemberMap the map for the members to document.
     * @param showInheritedSummary true if inherited summary should be documented
     * @param memberSummaryTree the content tree to which the documentation will be added
     */
    private void addSummary(MemberSummaryWriter writer,
            VisibleMemberMap visibleMemberMap, boolean showInheritedSummary,
            Content memberSummaryTree) {
        LinkedList<Content> summaryTreeList = new LinkedList<Content>();
        buildSummary(writer, visibleMemberMap, summaryTreeList);
        if (showInheritedSummary)
            buildInheritedSummary(writer, visibleMemberMap, summaryTreeList);
        if (!summaryTreeList.isEmpty()) {
            Content memberTree = writer.getMemberSummaryHeader(
                    classDoc, memberSummaryTree);
            for (int i = 0; i < summaryTreeList.size(); i++) {
                memberTree.addContent(summaryTreeList.get(i));
            }
            memberSummaryTree.addContent(writer.getMemberTree(memberTree));
        }
    }
}