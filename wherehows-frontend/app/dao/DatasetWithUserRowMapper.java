/**
 * Copyright 2015 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package dao;

import models.Dataset;
import models.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import play.Play;
import play.Logger;

import java.sql.Time;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class DatasetWithUserRowMapper implements RowMapper<Dataset>
{
    private static final String HDFS_BROWSER_URL =
        Play.application().configuration().getString(DatasetsDAO.HDFS_BROWSER_URL_KEY);

    public static final String DATASET_ID_COLUMN = "id";
    public static final String DATASET_NAME_COLUMN = "name";
    public static final String DATASET_URN_COLUMN = "urn";
    public static final String DATASET_SCHEMA_COLUMN = "schema";
    public static final String DATASET_SOURCE_COLUMN = "source";
    public static final String DATASET_CREATED_TIME_COLUMN = "created";
    public static final String DATASET_MODIFIED_TIME_COLUMN = "modified";
    public static final String DATASET_SOURCE_MODIFIED_TIME_COLUMN = "source_modified_time";
    public static final String FAVORITE_DATASET_ID_COLUMN = "dataset_id";
    public static final String DATASET_WATCH_ID_COLUMN = "watch_id";
    public static final String DATASET_PROPERTIES_COLUMN = "properties";
    public static final String SCHEMA_HISTORY_ID_COLUMN = "schema_history_id";
    public static final String DATASET_OWNER_ID_COLUMN = "owner_id";
    public static final String DATASET_OWNER_NAME_COLUMN = "owner_name";
    public static final String DATASET_OWNER_EMAIL_COLUMN = "owner_email";
    public static final String HDFS_PREFIX = "hdfs";

    @Override
    public Dataset mapRow(ResultSet rs, int rowNum) throws SQLException
    {
        int id = rs.getInt(DATASET_ID_COLUMN);
        String name = rs.getString(DATASET_NAME_COLUMN);
        String urn = rs.getString(DATASET_URN_COLUMN);
        String source = rs.getString(DATASET_SOURCE_COLUMN);
        String schema = rs.getString(DATASET_SCHEMA_COLUMN);
        Time created = rs.getTime(DATASET_CREATED_TIME_COLUMN);
        Time modified = rs.getTime(DATASET_MODIFIED_TIME_COLUMN);
        Integer favoriteId = rs.getInt(FAVORITE_DATASET_ID_COLUMN);
        Integer schemaHistoryId = rs.getInt(SCHEMA_HISTORY_ID_COLUMN);
        Long watchId = rs.getLong(DATASET_WATCH_ID_COLUMN);
        Long sourceModifiedTime = rs.getLong(DATASET_SOURCE_MODIFIED_TIME_COLUMN);
        String strOwner = rs.getString(DATASET_OWNER_ID_COLUMN);
        String strOwnerName = rs.getString(DATASET_OWNER_NAME_COLUMN);
        String strOwnerEmail = rs.getString(DATASET_OWNER_EMAIL_COLUMN);

        Dataset dataset = new Dataset();
        dataset.id = id;
        dataset.name = name;
        dataset.urn = urn;
        dataset.schema = schema;
        String[] owners = StringUtils.isNotBlank(strOwner) ? strOwner.split(",") : null;
        String[] ownerNames = StringUtils.isNotBlank(strOwnerName) ? strOwnerName.split(",") : null;
        String[] ownerEmail = StringUtils.isNotBlank(strOwnerEmail) ? strOwnerEmail.split(",") : null;

        dataset.owners = new ArrayList<>();
        if (owners != null && ownerNames != null && ownerEmail != null && owners.length == ownerNames.length
            && owners.length == ownerEmail.length)
        {
            for (int i = 0; i < owners.length; i++)
            {
                User user = new User();
                user.userName = owners[i];
                user.name = ownerNames[i];
                user.email = ownerEmail[i];
                dataset.owners.add(user);
            }
        }

        if (StringUtils.isNotBlank(dataset.urn) && dataset.urn.substring(0, 4).equalsIgnoreCase(HDFS_PREFIX))
        {
            dataset.hdfsBrowserLink = HDFS_BROWSER_URL + dataset.urn.substring(DatasetRowMapper.HDFS_URN_PREFIX_LEN);
        }

        dataset.source = source;
        if (modified != null && sourceModifiedTime != null && sourceModifiedTime > 0)
        {
            dataset.modified = new java.util.Date(modified.getTime());
            dataset.formatedModified = dataset.modified.toString();
        }
        if (created != null)
        {
            dataset.created = new java.util.Date(created.getTime());
        } else if (modified != null) {
            dataset.created = new java.util.Date(modified.getTime());
        }

        dataset.isFavorite = favoriteId != null && favoriteId > 0;

        if (watchId != null && watchId > 0)
        {
            dataset.watchId = watchId;
            dataset.isWatched = true;
        }
        else
        {
            dataset.watchId = 0L;
            dataset.isWatched = false;
        }

        dataset.hasSchemaHistory = schemaHistoryId != null && schemaHistoryId > 0;

        return dataset;
    }
}
