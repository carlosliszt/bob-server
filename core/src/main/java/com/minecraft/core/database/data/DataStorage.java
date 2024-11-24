/*
 * Copyright (C) BlazeMC, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.minecraft.core.database.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minecraft.core.Constants;
import com.minecraft.core.database.enums.Columns;
import com.minecraft.core.database.enums.Tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DataStorage {

    private final Map<Columns, Data> columnsDataMap;
    private final Map<Columns, Boolean> columnsLoaded;

    protected UUID uniqueId;
    protected String username;

    public DataStorage(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;

        columnsDataMap = new ConcurrentHashMap<>();
        columnsLoaded = new ConcurrentHashMap<>();

        for (Columns column : Columns.values()) {
            getColumnsLoaded().put(column, false);
        }

        for (Columns columns : Columns.values()) {
            if (columns == Columns.UNIQUE_ID) {
                columnsDataMap.put(columns, new Data(columns, this.uniqueId));
            } else if (columns.getClassExpected().equals("JsonArray")) {
                columnsDataMap.put(columns, new Data(columns, new JsonArray()));
            } else if (columns.getClassExpected().equals("JsonObject")) {
                columnsDataMap.put(columns, new Data(columns, new JsonObject()));
            } else {
                columnsDataMap.put(columns, new Data(columns, columns.getDefaultValue()));
            }
        }
    }

    /**
     * Utility method for loading tables.
     *
     * @param tables The tables that will be loaded.
     * @return if it was a success.
     */
    @Deprecated
    public boolean load(Tables... tables) {
        List<Columns> columns = new ArrayList<>();

        for (Tables table : tables) {
            columns.addAll(Arrays.asList(table.getColumns()));
        }
        return loadColumns(columns);
    }

    /**
     * Utility method to save all columns from a table.
     */
    public void saveTable(Tables... tables) {
        try {
            for (Tables table : tables) {
                String s = update(table, table.getColumns());
                if (!s.isEmpty()) {
                    PreparedStatement statement = Constants.getMySQL().getConnection().prepareStatement(s);
                    statement.executeUpdate();
                    statement.close();
                } else {
                    System.out.println("Nothing to save to " + table.getName() + " from " + username + "'s account.");
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Utility method to save one column from a table.
     */
    public void saveColumn(Columns column) {

        if (!getData(column).hasChanged())
            return;

        try {
            String s = update(column.getTable(), column);
            if (!s.isEmpty()) {
                PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(s);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Utility method to save multiples columns from a table.
     */
    public void saveColumnsFromSameTable(Columns... columns) {
        try {
            String s = update(columns[0].getTable(), columns);
            if (!s.isEmpty()) {
                PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(s);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }


    public boolean loadColumns(List<Columns> columns) {
        try {

            if (columns.isEmpty())
                return true;

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("SELECT accounts.unique_id, ");

            List<Tables> tables = removeDuplicates(columns.stream().map(Columns::getTable).filter(c -> c != Tables.ACCOUNT).collect(Collectors.toList()));

            for (int i = 0; i < columns.size(); i++) {
                Columns column = columns.get(i);
                stringBuilder.append(column.getTable().getName().toLowerCase()).append(".").append(column.getField());
                if (i != columns.size() - 1)
                    stringBuilder.append(",");
                stringBuilder.append(" ");
            }

            stringBuilder.append("FROM `accounts` accounts ");

            for (Tables table : tables) {
                stringBuilder.append("LEFT JOIN `").append(table.getName()).append("` ").append(table.getName()).append(" ON accounts.unique_id = ").append(table.getName()).append(".unique_id ");
            }
            stringBuilder.append("WHERE accounts.unique_id='").append(uniqueId.toString()).append("'").append(" LIMIT 1;");

            PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(stringBuilder.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                for (Columns column : columns) {
                    Object value = loadData(column, resultSet, column.getField());
                    getData(column).setData(value);
                    getColumnsLoaded().put(column, true);
                }
            }
            resultSet.close();
            preparedStatement.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean loadIfUnloaded(Columns... columns) {
        List<Columns> toLoad = new ArrayList<>();

        for (Columns column : columns) {
            if (!isLoaded(column))
                toLoad.add(column);
        }

        System.out.println("(" + username + ")" + "The method loadIfUnloaded loaded " + toLoad.size() + "/" + columns.length + ". (" + Arrays.toString(toLoad.toArray()) + ")");

        return loadColumns(toLoad);
    }

    public void loadColumns(boolean createIfNotExists, Columns... columns) {
        try {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("SELECT ");

            int inx = 0;
            int max = columns.length;

            while (inx < max) {
                Columns current = columns[inx];
                if (inx == 0)
                    stringBuilder.append("`").append(current.getField()).append("`");
                else
                    stringBuilder.append(", `").append(current.getField()).append("`");
                inx++;
            }

            Tables table = columns[0].getTable();

            stringBuilder.append(" FROM `").append(table.getName()).append("`");
            stringBuilder.append(" WHERE `unique_id`='").append(uniqueId.toString()).append("' LIMIT 1;");

            PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(stringBuilder.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                for (Columns column : columns) {
                    Object value = loadData(column, resultSet, column.getField());
                    getData(column).setData(value);
                }
            } else {
                if (createIfNotExists) {
                    PreparedStatement statement = Constants.getMySQL().getConnection().prepareStatement((insert(table, columns)));
                    statement.execute();
                    statement.close();
                }
            }

            resultSet.close();
            preparedStatement.close();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private String insert(Tables table, Columns... columns) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("INSERT INTO `").append(table.getName()).append("` (");

        int inx = 0;
        int max = columns.length;

        stringBuilder.append("`unique_id`");

        while (inx < max) {
            Columns current = columns[inx];
            stringBuilder.append(", `").append(current.getField()).append("`");
            inx++;
        }

        stringBuilder.append(") VALUES (");

        inx = 0;

        stringBuilder.append("'").append(uniqueId.toString()).append("'");

        while (inx < max) {
            Columns current = columns[inx];
            Object value = current.getDefaultValue();

            if (value == null) value = "";

            stringBuilder.append(", '").append(value).append("'");
            inx++;
        }

        stringBuilder.append(");");

        return stringBuilder.toString();
    }

    private String update(Tables table, Columns... columns) {

        StringBuilder first = new StringBuilder();
        StringBuilder second = new StringBuilder();
        StringBuilder third = new StringBuilder();

        first.append("INSERT INTO `").append(table.getName()).append("` (unique_id, ");
        second.append(" VALUES('").append(this.uniqueId.toString()).append("', ");
        third.append(" ON DUPLICATE KEY UPDATE ");

        Iterator<Columns> iterator = Arrays.stream(columns).filter(c -> getData(c).hasChanged()).iterator();

        if (!iterator.hasNext())
            return "";

        while (iterator.hasNext()) {

            Columns column = iterator.next();

            first.append(column.getField());

            String toString = getData(column).getAsObject().toString();

            if (iterator.hasNext())
                first.append(", ");
            else
                first.append(")");

            second.append("'").append(toString).append("'");

            if (iterator.hasNext())
                second.append(", ");
            else
                second.append(")");

            third.append("`").append(column.getField()).append("`='").append(toString).append("'");

            if (iterator.hasNext())
                third.append(", ");

            getData(column).setChanged(false);
        }

        return (first.toString() + second + third);
    }

    public static void createTables() {
        try {

            PreparedStatement logs = Constants.getMySQL().getConnection().prepareStatement("  CREATE TABLE IF NOT EXISTS `logs` (`index` INT UNSIGNED NOT NULL AUTO_INCREMENT,`unique_id` VARCHAR(36) NOT NULL,`nickname` VARCHAR(16) NOT NULL,`server` VARCHAR(20) NOT NULL,`content` TINYTEXT NOT NULL,`type` VARCHAR(20) NOT NULL,`created_at` DATETIME NOT NULL DEFAULT NOW(),PRIMARY KEY(`index`));");
            logs.execute();
            logs.close();

            for (Tables tables : Tables.values()) {
                PreparedStatement preparedStatement = Constants.getMySQL().getConnection().prepareStatement(DataStorage.createTable(tables, tables.getColumns()));
                preparedStatement.execute();
                preparedStatement.close();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static String createTable(Tables tables, Columns... columns) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("CREATE TABLE IF NOT EXISTS `").append(tables.getName()).append("` (");
        stringBuilder.append("`index` INT UNSIGNED NOT NULL AUTO_INCREMENT,");

        int inx = 0;
        int max = columns.length;

        stringBuilder.append("`unique_id` VARCHAR (36) UNIQUE");

        while (inx < max) {
            Columns current = columns[inx];

            stringBuilder.append(", `").append(current.getField()).append("` ").append(current.getColumnType());
            inx++;
        }

        stringBuilder.append(", PRIMARY KEY (`index`));");
        return stringBuilder.toString();
    }

    public static Object loadData(Columns columns, ResultSet resultSet, String fieldName) {
        try {
            Object obj = resultSet.getObject(fieldName);

            if (obj == null) {
                obj = columns.getDefaultValue();
            }

            switch (columns.getClassExpected()) {
                case "JsonArray":
                    return Constants.GSON.fromJson(obj.toString(), JsonArray.class);
                case "JsonObject":
                    return Constants.GSON.fromJson(obj.toString(), JsonObject.class);
                case "String":
                    return obj.toString();
                case "Int":
                    return Integer.valueOf(obj.toString());
                case "Long":
                    return Long.valueOf(obj.toString());
                case "Boolean":
                    return Boolean.valueOf(obj.toString());
                default:
                    throw new IllegalStateException();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public Data getData(Columns columns) {
        return getColumnsDataMap().get(columns);
    }

    public Map<Columns, Boolean> getColumnsLoaded() {
        return columnsLoaded;
    }

    public Map<Columns, Data> getColumnsDataMap() {
        return columnsDataMap;
    }

    private <T> ArrayList<T> removeDuplicates(List<T> list) {
        Set<T> set = new LinkedHashSet<>(list);
        list.clear();
        list.addAll(set);
        return new ArrayList<>(list);
    }

    public boolean isLoaded(Columns column) {
        return getColumnsLoaded().get(column);
    }

}