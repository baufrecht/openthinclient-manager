package org.openthinclient.web.filebrowser;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.vaadin.data.Collapsible;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.server.Resource;
import com.vaadin.util.FileTypeResolver;

@SuppressWarnings("serial")
/**
 * https://dev.vaadin.com/svn/addons/TreeTable/demo/src/com/vaadin/addon/treetable/CollapsibleFileSystemContainer.java
 * Example of Collapsiple container, based on the std FileSystemContainer.
 */
public class CollapsibleFileSystemContainer implements Collapsible {
    /**
     * String identifier of a file's "name" property.
     */
    public static String PROPERTY_NAME = "Name";

    /**
     * String identifier of a file's "size" property.
     */
    public static String PROPERTY_SIZE = "Size";

    /**
     * String identifier of a file's "icon" property.
     */
    public static String PROPERTY_ICON = "Icon";

    /**
     * String identifier of a file's "last modified" property.
     */
    public static String PROPERTY_LASTMODIFIED = "Last Modified";

    /**
     * List of the string identifiers for the available properties.
     */
    public static Collection<String> FILE_PROPERTIES;

    private final static Method FILEITEM_LASTMODIFIED;

    private final static Method FILEITEM_NAME;

    private final static Method FILEITEM_ICON;

    private final static Method FILEITEM_SIZE;

    static {

        FILE_PROPERTIES = new ArrayList<String>();
        FILE_PROPERTIES.add(PROPERTY_NAME);
        FILE_PROPERTIES.add(PROPERTY_ICON);
        FILE_PROPERTIES.add(PROPERTY_SIZE);
        FILE_PROPERTIES.add(PROPERTY_LASTMODIFIED);
        FILE_PROPERTIES = Collections.unmodifiableCollection(FILE_PROPERTIES);
        try {
            FILEITEM_LASTMODIFIED = FileItem.class.getMethod("lastModified",
                    new Class[] {});
            FILEITEM_NAME = FileItem.class.getMethod("getName", new Class[] {});
            FILEITEM_ICON = FileItem.class.getMethod("getIcon", new Class[] {});
            FILEITEM_SIZE = FileItem.class.getMethod("getSize", new Class[] {});
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException(
                    "Internal error finding methods in FilesystemContainer");
        }
    }

    private File[] roots = new File[] {};

    private FilenameFilter filter = null;

    /**
     * Constructs a new <code>FileSystemContainer</code> with the specified file
     * as the root of the filesystem. The files are included recursively.
     * 
     * @param root
     *            the root file for the new file-system container. Null values
     *            are ignored.
     */
    public CollapsibleFileSystemContainer(File root) {
        if (root != null) {
            roots = new File[] { root };
        }
    }

    /**
     * Adds new root file directory. Adds a file to be included as root file
     * directory in the <code>FilesystemContainer</code>.
     * 
     * @param root
     *            the File to be added as root directory. Null values are
     *            ignored.
     */
    public void addRoot(File root) {
        if (root != null) {
            final File[] newRoots = new File[roots.length + 1];
            for (int i = 0; i < roots.length; i++) {
                newRoots[i] = roots[i];
            }
            newRoots[roots.length] = root;
            roots = newRoots;
        }
    }

    /**
     * Tests if the specified Item in the container may have children. Since a
     * <code>FileSystemContainer</code> contains files and directories, this
     * method returns <code>true</code> for directory Items only.
     * 
     * @param itemId
     *            the id of the item.
     * @return <code>true</code> if the specified Item is a directory,
     *         <code>false</code> otherwise.
     */
    public boolean areChildrenAllowed(Object itemId) {
        return itemId instanceof File && ((File) itemId).canRead()
                && ((File) itemId).isDirectory();
    }

    /*
     * Gets the ID's of all Items who are children of the specified Item. Don't
     * add a JavaDoc comment here, we use the default documentation from
     * implemented interface.
     */
    public Collection<File> getChildren(Object itemId) {

        if (!(itemId instanceof File)) {
            return Collections.unmodifiableCollection(new LinkedList<File>());
        }
        File[] f;
        if (filter != null) {
            f = ((File) itemId).listFiles(filter);
        } else {
            f = ((File) itemId).listFiles();
        }

        if (f == null) {
            return Collections.unmodifiableCollection(new LinkedList<File>());
        }

        final List<File> l = Arrays.asList(f);
        Collections.sort(l);

        return Collections.unmodifiableCollection(l);
    }

    /*
     * Gets the parent item of the specified Item. Don't add a JavaDoc comment
     * here, we use the default documentation from implemented interface.
     */
    public Object getParent(Object itemId) {

        if (!(itemId instanceof File)) {
            return null;
        }
        return ((File) itemId).getParentFile();
    }

    /*
     * Tests if the specified Item has any children. Don't add a JavaDoc comment
     * here, we use the default documentation from implemented interface.
     */
    public boolean hasChildren(Object itemId) {

        if (!(itemId instanceof File)) {
            return false;
        }
        String[] l;
        if (filter != null) {
            l = ((File) itemId).list(filter);
        } else {
            l = ((File) itemId).list();
        }
        return (l != null) && (l.length > 0);
    }

    /*
     * Tests if the specified Item is the root of the filesystem. Don't add a
     * JavaDoc comment here, we use the default documentation from implemented
     * interface.
     */
    public boolean isRoot(Object itemId) {

        if (!(itemId instanceof File)) {
            return false;
        }
        for (int i = 0; i < roots.length; i++) {
            if (roots[i].equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Gets the ID's of all root Items in the container. Don't add a JavaDoc
     * comment here, we use the default documentation from implemented
     * interface.
     */
    public Collection<File> rootItemIds() {

        File[] f;

        // in single root case we use children
        if (roots.length == 1) {
            if (filter != null) {
                f = roots[0].listFiles(filter);
            } else {
                f = roots[0].listFiles();
            }
        } else {
            f = roots;
        }

        if (f == null) {
            return Collections.unmodifiableCollection(new LinkedList<File>());
        }

        final List<File> l = Arrays.asList(f);
        Collections.sort(l);

        return Collections.unmodifiableCollection(l);
    }

    /**
     * Returns <code>false</code> when conversion from files to directories is
     * not supported.
     * 
     * @param itemId
     *            the ID of the item.
     * @param areChildrenAllowed
     *            the boolean value specifying if the Item can have children or
     *            not.
     * @return <code>true</code> if the operaton is successful otherwise
     *         <code>false</code>.
     * @throws UnsupportedOperationException
     *             if the setChildrenAllowed is not supported.
     */
    public boolean setChildrenAllowed(Object itemId, boolean areChildrenAllowed)
            throws UnsupportedOperationException {

        throw new UnsupportedOperationException(
                "Conversion file to/from directory is not supported");
    }

    /**
     * Returns <code>false</code> when moving files around in the filesystem is
     * not supported.
     * 
     * @param itemId
     *            the ID of the item.
     * @param newParentId
     *            the ID of the Item that's to be the new parent of the Item
     *            identified with itemId.
     * @return <code>true</code> if the operation is successful otherwise
     *         <code>false</code>.
     * @throws UnsupportedOperationException
     *             if the setParent is not supported.
     */
    public boolean setParent(Object itemId, Object newParentId)
            throws UnsupportedOperationException {

        throw new UnsupportedOperationException("File moving is not supported");
    }

    /*
     * Tests if the filesystem contains the specified Item. Don't add a JavaDoc
     * comment here, we use the default documentation from implemented
     * interface.
     */
    public boolean containsId(Object itemId) {

        if (!(itemId instanceof File)) {
            return false;
        }
        boolean val = false;

        // Try to match all roots
        for (int i = 0; !val & i < roots.length; i++) {
            try {
                val |= ((File) itemId).getCanonicalPath().startsWith(
                        roots[i].getCanonicalPath());
            } catch (final IOException e) {
                // Exception ignored
            }

        }
        if (val && filter != null) {
            val &= filter.accept(((File) itemId).getParentFile(),
                    ((File) itemId).getName());
        }
        return val;
    }

    /*
     * Gets the specified Item from the filesystem. Don't add a JavaDoc comment
     * here, we use the default documentation from implemented interface.
     */
    public Item getItem(Object itemId) {

        if (!(itemId instanceof File)) {
            return null;
        }
        return new FileItem((File) itemId);
    }

    /*
     * Gets the IDs of Items in the filesystem. Don't add a JavaDoc comment
     * here, we use the default documentation from implemented interface.
     */
    @Override
    public Collection<File> getItemIds() {

        File[] f;
        if (roots.length == 1) {
            if (filter != null) {
                f = roots[0].listFiles(filter);
            } else {
                f = roots[0].listFiles();
            }
        } else {
            f = roots;
        }

        if (f == null) {
            return Collections.unmodifiableCollection(new LinkedList<File>());
        }

        final List<File> l = Arrays.asList(f);
        Collections.sort(l);
        return Collections.unmodifiableCollection(l);

    }
    
    

    /**
     * Gets the specified property of the specified file Item. The available
     * file properties are "Name", "Size" and "Last Modified". If propertyId is
     * not one of those, <code>null</code> is returned.
     * 
     * @param itemId
     *            the ID of the file whose property is requested.
     * @param propertyId
     *            the property's ID.
     * @return the requested property's value, or <code>null</code>
     */
    public Property getContainerProperty(Object itemId, Object propertyId) {

        if (!(itemId instanceof File)) {
            return null;
        }

        if (propertyId.equals(PROPERTY_NAME)) {
            return new MethodProperty(getType(propertyId), new FileItem(
                    (File) itemId), FILEITEM_NAME, null);
        }

        if (propertyId.equals(PROPERTY_ICON)) {
            return new MethodProperty(getType(propertyId), new FileItem(
                    (File) itemId), FILEITEM_ICON, null);
        }

        if (propertyId.equals(PROPERTY_SIZE)) {
            return new MethodProperty(getType(propertyId), new FileItem(
                    (File) itemId), FILEITEM_SIZE, null);
        }

        if (propertyId.equals(PROPERTY_LASTMODIFIED)) {
            return new MethodProperty(getType(propertyId), new FileItem(
                    (File) itemId), FILEITEM_LASTMODIFIED, null);
        }

        return null;
    }

    /**
     * Gets the collection of available file properties.
     * 
     * @return Unmodifiable collection containing all available file properties.
     */
    public Collection<String> getContainerPropertyIds() {
        return FILE_PROPERTIES;
    }

    /**
     * Gets the specified property's data type. "Name" is a <code>String</code>,
     * "Size" is a <code>Long</code>, "Last Modified" is a <code>Date</code>. If
     * propertyId is not one of those, <code>null</code> is returned.
     * 
     * @param propertyId
     *            the ID of the property whose type is requested.
     * @return data type of the requested property, or <code>null</code>
     */
    public Class<?> getType(Object propertyId) {

        if (propertyId.equals(PROPERTY_NAME)) {
            return String.class;
        }
        if (propertyId.equals(PROPERTY_ICON)) {
            return com.vaadin.server.Resource.class;
        }
        if (propertyId.equals(PROPERTY_SIZE)) {
            return Long.class;
        }
        if (propertyId.equals(PROPERTY_LASTMODIFIED)) {
            return Date.class;
        }
        return null;
    }

    /**
     * Gets the number of Items in the container. In effect, this is the
     * combined amount of files and directories.
     * 
     * @return Number of Items in the container.
     */
    public int size() {
        return getPreorder().size();
    }

    private List<File> preorder;

    private List<File> getPreorder() {
        if (preorder == null) {
            preorder = new ArrayList<File>();
            for (File root : roots) {
                preorder.add(root);
                loadVisibleSubtree(root);
            }

        }
        return preorder;
    }

    private void loadVisibleSubtree(File root) {
        if (!isCollapsed(root)) {
            File[] f;
            if (filter != null) {
                f = root.listFiles(filter);
            } else {
                f = root.listFiles();
            }
            if (f != null) {
                for (File file : f) {
                    preorder.add(file);
                    loadVisibleSubtree(file);
                }
            }
        }
    }

    /**
     * A Item wrapper for files in a filesystem.
     * 
     * @author IT Mill Ltd.
     * @version 6.4.0.nightly-20100511-c13129
     * @since 3.0
     */
    public class FileItem implements Item {

        /**
         * The wrapped file.
         */
        private final File file;

        /**
         * Constructs a FileItem from a existing file.
         */
        private FileItem(File file) {
            this.file = file;
        }

        /*
         * Gets the specified property of this file. Don't add a JavaDoc comment
         * here, we use the default documentation from implemented interface.
         */
        public Property getItemProperty(Object id) {
            return getContainerProperty(file, id);
        }

        /*
         * Gets the IDs of all properties available for this item Don't add a
         * JavaDoc comment here, we use the default documentation from
         * implemented interface.
         */
        public Collection<String> getItemPropertyIds() {
            return getContainerPropertyIds();
        }

        /**
         * Calculates a integer hash-code for the Property that's unique inside
         * the Item containing the Property. Two different Properties inside the
         * same Item contained in the same list always have different
         * hash-codes, though Properties in different Items may have identical
         * hash-codes.
         * 
         * @return A locally unique hash-code as integer
         */
        @Override
        public int hashCode() {
            return file.hashCode()
                    ^ CollapsibleFileSystemContainer.this.hashCode();
        }

        /**
         * Tests if the given object is the same as the this object. Two
         * Properties got from an Item with the same ID are equal.
         * 
         * @param obj
         *            an object to compare with this object.
         * @return <code>true</code> if the given object is the same as this
         *         object, <code>false</code> if not
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof FileItem)) {
                return false;
            }
            final FileItem fi = (FileItem) obj;
            return fi.getHost() == getHost() && fi.file.equals(file);
        }

        /**
         * Gets the host of this file.
         */
        private CollapsibleFileSystemContainer getHost() {
            return CollapsibleFileSystemContainer.this;
        }

        /**
         * Gets the last modified date of this file.
         * 
         * @return Date
         */
        public Date lastModified() {
            return new Date(file.lastModified());
        }

        /**
         * Gets the name of this file.
         * 
         * @return file name of this file.
         */
        public String getName() {
            return file.getName();
        }

        /**
         * Gets the icon of this file.
         * 
         * @return the icon of this file.
         */
        public Resource getIcon() {
            return FileTypeResolver.getIcon(file);
        }

        /**
         * Gets the size of this file.
         * 
         * @return size
         */
        public long getSize() {
            if (file.isDirectory()) {
                return 0;
            }
            return file.length();
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            if ("".equals(file.getName())) {
                return file.getAbsolutePath();
            }
            return file.getName();
        }

        /**
         * Filesystem container does not support adding new properties.
         * 
         * @see com.vaadin.data.Item#addItemProperty(Object, Property)
         */
        public boolean addItemProperty(Object id, Property property)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException("Filesystem container "
                    + "does not support adding new properties");
        }

        /**
         * Filesystem container does not support removing properties.
         * 
         * @see com.vaadin.data.Item#removeItemProperty(Object)
         */
        public boolean removeItemProperty(Object id)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "Filesystem container does not support property removal");
        }

    }

    /**
     * Generic file extension filter for displaying only files having certain
     * extension.
     * 
     * @author IT Mill Ltd.
     * @version 6.4.0.nightly-20100511-c13129
     * @since 3.0
     */
    public static class FileExtensionFilter implements FilenameFilter,
            Serializable {

        private final String filter;

        /**
         * Constructs a new FileExtensionFilter using given extension.
         * 
         * @param fileExtension
         *            the File extension without the separator (dot).
         */
        public FileExtensionFilter(String fileExtension) {
            filter = "." + fileExtension;
        }

        /**
         * Allows only files with the extension and directories.
         * 
         * @see java.io.FilenameFilter#accept(File, String)
         */
        public boolean accept(File dir, String name) {
            if (name.endsWith(filter)) {
                return true;
            }
            return new File(dir, name).isDirectory();
        }

    }

    /**
     * Returns the file filter used to limit the files in this container.
     * 
     * @return Used filter instance or null if no filter is assigned.
     */
    public FilenameFilter getFilter() {
        return filter;
    }

    /**
     * Sets the file filter used to limit the files in this container.
     * 
     * @param filter
     *            The filter to set. <code>null</code> disables filtering.
     */
    public void setFilter(FilenameFilter filter) {
        this.filter = filter;
    }

    /**
     * Sets the file filter used to limit the files in this container.
     * 
     * @param extension
     *            the Filename extension (w/o separator) to limit the files in
     *            container.
     */
    public void setFilter(String extension) {
        filter = new FileExtensionFilter(extension);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.data.Container#addContainerProperty(java.lang.Object,
     * java.lang.Class, java.lang.Object)
     */
    public boolean addContainerProperty(Object propertyId, Class<?> type,
            Object defaultValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "File system container does not support this operation");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.data.Container#addItem()
     */
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "File system container does not support this operation");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.data.Container#addItem(java.lang.Object)
     */
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "File system container does not support this operation");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.data.Container#removeAllItems()
     */
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "File system container does not support this operation");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.data.Container#removeItem(java.lang.Object)
     */
    public boolean removeItem(Object itemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "File system container does not support this operation");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.data.Container#removeContainerProperty(java.lang.Object )
     */
    public boolean removeContainerProperty(Object propertyId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "File system container does not support this operation");
    }

    Set<File> openDirectories = new HashSet<File>();

    public boolean isCollapsed(Object itemId) {
        return !openDirectories.contains(itemId);
    }

    public void setCollapsed(Object itemId, boolean collapsed) {
        if (collapsed) {
            openDirectories.remove(itemId);
        } else {
            openDirectories.add((File) itemId);
        }
        preorder = null;
    }

    public Object addItemAfter(Object previousItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "File system container does not support this operation");
    }

    public Item addItemAfter(Object previousItemId, Object newItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "File system container does not support this operation");
    }

    public Object firstItemId() {
        return roots[0];
    }

    public boolean isFirstId(Object itemId) {
        return itemId.equals(firstItemId());
    }

    public boolean isLastId(Object itemId) {
        return itemId.equals(lastItemId());
    }

    public Object lastItemId() {
        return getPreorder().get(size() - 1);
    }

    public Object nextItemId(Object itemId) {
        int indexOf = getPreorder().indexOf(itemId) + 1;
        if (indexOf == size()) {
            return null;
        }
        return getPreorder().get(indexOf);
    }

    public Object prevItemId(Object itemId) {
        int indexOf = getPreorder().indexOf(itemId) - 1;
        if (indexOf < 0) {
            return null;
        }
        return getPreorder().get(indexOf);
    }

    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "File system container does not support this operation");
    }

    public Item addItemAt(int index, Object newItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "File system container does not support this operation");
    }

    public Object getIdByIndex(int index) {
        return getPreorder().get(index);
    }

    public int indexOfId(Object itemId) {
        return getPreorder().indexOf(itemId);
    }

}

