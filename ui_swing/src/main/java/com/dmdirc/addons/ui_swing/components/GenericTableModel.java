/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.ui_swing.components;

import com.dmdirc.util.functional.TriConsumer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.swing.table.AbstractTableModel;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Table model that display fields from a type of object.
 *
 * @param <T> Type of object to display
 */
public class GenericTableModel<T> extends AbstractTableModel {

    /**
     * Serialisation version.
     */
    private static final long serialVersionUID = 1L;
    /**
     * List of values in the model.
     */
    private final List<T> values;
    /**
     * List of getters for each value, used as columns.
     */
    private final Method[] getters;
    /**
     * List of header names for each column.
     */
    private final String[] headers;
    /**
     * Generic type this table contains, used to verify getters exist.
     */
    private final Class<T> type;
    /**
     * Cached return types for the getters.
     */
    private final Class<?>[] columnTypes;
    private final BiFunction<Integer, Integer, Boolean> isEditable;
    private final TriConsumer<Object, Integer, Integer> editFunction;

    /**
     * Creates a new generic table model.
     *
     * @param type         Type to be displayed, used to verify getters
     * @param getterValues Names of the getters to display in the table
     */
    public GenericTableModel(final Class<T> type, final String... getterValues) {
        this(type, (row, column) -> false, (value, row, column) -> {}, getterValues);
    }

    /**
     * Creates a new generic table model.
     *
     * @param type         Type to be displayed, used to verify getters
     * @param isEditable   Function to decide if a cell is editable or not
     *                          First parameter is the row
     *                          Second parameter is the column
     *                          Third parameter is the return type
     * @param editFunction The function to be called when editing a particular cell.
     *                          First parameter is the object
     *                          Second parameter is the row
     *                          Third parameter is the column
     * @param getterValues Names of the getters to display in the table
     */
    public GenericTableModel(final Class<T> type,
            final BiFunction<Integer, Integer, Boolean> isEditable,
            final TriConsumer<Object, Integer, Integer> editFunction,
            final String... getterValues) {
        checkArgument(getterValues.length > 0, "Getters must be set");
        checkNotNull(isEditable);
        checkNotNull(editFunction);
        this.isEditable = isEditable;
        this.editFunction = editFunction;
        this.type = type;
        values = new ArrayList<>();
        headers = new String[getterValues.length];
        getters = new Method[getterValues.length];
        columnTypes = new Class<?>[getterValues.length];

        for (int i = 0; i < getterValues.length; i++) {
            final String getterName = getterValues[i];
            checkNotNull(getterName, "Getter must not be null");
            checkArgument(!getterName.isEmpty(), "Getter must not be empty");

            final Optional<Method> method = getGetter(getterName);
            checkArgument(method.isPresent(), "Getter must exist in type");

            getters[i] = method.get();
            headers[i] = getterValues[i];
            columnTypes[i] = getGetter(getterValues[i]).get().getReturnType();
        }
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public int getColumnCount() {
        return getters.length;
    }

    @Override
    public String getColumnName(final int column) {
        checkElementIndex(column, headers.length, "Column must exist");
        return headers[column];
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        checkElementIndex(columnIndex, getters.length, "Column must exist");
        return columnTypes[columnIndex];
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        checkElementIndex(rowIndex, values.size(), "Row index must exist");
        checkElementIndex(columnIndex, getters.length, "Column index must exist");
        try {
            final T value = values.get(rowIndex);
            return getters[columnIndex].invoke(value);
        } catch (IndexOutOfBoundsException | ReflectiveOperationException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return isEditable.apply(rowIndex, columnIndex);
    }

    /**
     * Returns the value at the specified row.
     *
     * @param rowIndex Index to retrieve
     *
     * @return Value at the specified row
     */
    public T getValue(final int rowIndex) {
        checkElementIndex(rowIndex, values.size(), "Row index must exist");
        return values.get(rowIndex);
    }

    /**
     * Returns the index of the specified object, or -1 if the object does not exist.
     *
     * @param object Object to find
     *
     * @return Item index, or -1 if it did not exist
     */
    public int getIndex(final T object) {
        return values.indexOf(object);
    }

    /**
     * Sets all of the header names. Must include all the headers.
     *
     * @param headerValues Names for all headers in the table
     */
    public void setHeaderNames(final String... headerValues) {
        checkArgument(headerValues.length == getters.length,
                "There must be as many headers as columns");
        System.arraycopy(headerValues, 0, headers, 0, headerValues.length);
        fireTableStructureChanged();
    }

    /**
     * Adds the specified value at the specified index, the column is ignored.
     *
     * @param value       Value to add
     * @param rowIndex    Index of the row to add
     * @param columnIndex This is ignored
     */
    @Override
    public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
        checkElementIndex(rowIndex, values.size(), "Row index must exist");
        checkElementIndex(columnIndex, getters.length, "Column index must exist");
        try {
            editFunction.accept(value, rowIndex, columnIndex);
            fireTableRowsUpdated(rowIndex, rowIndex);
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("Value of incorrect type.", ex);
        }
    }

    /**
     * Replaces the value at the specified index with the specified value.
     *
     * @param value    New value to insert
     * @param rowIndex Index to replace
     */
    public void replaceValueAt(final T value, final int rowIndex) {
        checkNotNull(value, "Value must not be null");
        checkElementIndex(rowIndex, values.size(), "RowIndex must be valid");
        values.set(rowIndex, value);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    /**
     * Removes a value from the model.
     *
     * @param value Value to be removed.
     */
    public void removeValue(final T value) {
        checkArgument(values.contains(value), "Value must exist");
        final int index = values.indexOf(value);
        values.remove(value);
        fireTableRowsDeleted(index, index);
    }

    /**
     * Removes all the values from the model.
     */
    public void removeAll() {
        values.clear();
        fireTableDataChanged();
    }

    /**
     * Adds the specified value at the end of the model.
     *
     * @param value Value to add
     */
    public void addValue(final T value) {
        values.add(value);
        final int index = values.indexOf(value);
        fireTableRowsInserted(index, index);
    }

    /**
     * Returns a collection of all the elements in this model.
     *
     * @return All elements in the model
     */
    public Collection<T> elements() {
        return Collections.unmodifiableCollection(values);
    }

    /**
     * Returns an optional method for the specified name.
     *
     * @param name Name of the getter
     *
     * @return Optional method
     */
    private Optional<Method> getGetter(final String name) {
        try {
            return Optional.ofNullable(type.getMethod(name));
        } catch (NoSuchMethodException ex) {
            return Optional.empty();
        }
    }

}
