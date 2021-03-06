package com.samsistemas.timesheet.mapper;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.samsistemas.timesheet.entity.JobLogEntity;
import com.samsistemas.timesheet.mapper.base.EntityMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author jonatan.salas
 */
public class JobLogEntityMapper implements EntityMapper<JobLogEntity, Cursor> {
    private static final String LOG_TAG = JobLogEntityMapper.class.getSimpleName();
    private static final String ID_JOBLOG = "id";
    private static final String ID_PROJECT = "id_project";
    private static final String ID_PERSON = "id_person";
    private static final String ID_TASK_TYPE = "id_tasktype";
    private static final String HOURS = "hours";
    private static final String WORK_DATE = "work_date";
    private static final String SOLICITUDE_NUMBER = "solicitude_number";
    private static final String OBSERVATIONS = "observations";

    @Override
    public ContentValues asContentValues(@NonNull JobLogEntity objectToMap) {
        ContentValues values = new ContentValues(8);

        values.put(ID_JOBLOG, objectToMap.getId());
        values.put(ID_PROJECT, objectToMap.getProjectId());
        values.put(ID_PERSON, objectToMap.getPersonId());
        values.put(ID_TASK_TYPE, objectToMap.getTaskTypeId());
        values.put(HOURS, objectToMap.getHours());
        values.put(WORK_DATE, objectToMap.getWorkDate().getTime());
        values.put(SOLICITUDE_NUMBER, objectToMap.getSolicitude());
        values.put(OBSERVATIONS, objectToMap.getObservations());

        return values;
    }

    @Override
    public JobLogEntity asEntity(@Nullable Cursor cursor) {
        JobLogEntity entity = new JobLogEntity();

        try {
            if (null != cursor && cursor.getCount() == 0) {
                return entity;
            }

            if (null != cursor && cursor.moveToFirst()) {
                long millis = cursor.getLong(cursor.getColumnIndexOrThrow(WORK_DATE));

                entity.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ID_JOBLOG)));
                entity.setProjectId(cursor.getLong(cursor.getColumnIndexOrThrow(ID_PROJECT)))
                        .setPersonId(cursor.getLong(cursor.getColumnIndexOrThrow(ID_PERSON)))
                        .setTaskTypeId(cursor.getLong(cursor.getColumnIndexOrThrow(ID_TASK_TYPE)))
                        .setHours(cursor.getString(cursor.getColumnIndexOrThrow(HOURS)))
                        .setWorkDate(new Date(millis))
                        .setSolicitude(cursor.getInt(cursor.getColumnIndexOrThrow(SOLICITUDE_NUMBER)))
                        .setObservations(cursor.getString(cursor.getColumnIndexOrThrow(OBSERVATIONS)));
            }

        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex.getCause());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return entity;
    }

    @Override
    public List<JobLogEntity> asEntityList(@Nullable Cursor cursor) {
        List<JobLogEntity> entityList = new ArrayList<>();

        try {
            if (null != cursor && cursor.getCount() == 0) {
                return entityList;
            }

            if (null != cursor && cursor.moveToFirst()) {
                do {
                    long millis = cursor.getLong(cursor.getColumnIndexOrThrow(WORK_DATE));

                    JobLogEntity entity = new JobLogEntity();

                    entity.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ID_JOBLOG)));
                    entity.setProjectId(cursor.getLong(cursor.getColumnIndexOrThrow(ID_PROJECT)))
                          .setPersonId(cursor.getLong(cursor.getColumnIndexOrThrow(ID_PERSON)))
                          .setTaskTypeId(cursor.getLong(cursor.getColumnIndexOrThrow(ID_TASK_TYPE)))
                          .setHours(cursor.getString(cursor.getColumnIndexOrThrow(HOURS)))
                          .setWorkDate(new Date(millis))
                          .setSolicitude(cursor.getInt(cursor.getColumnIndexOrThrow(SOLICITUDE_NUMBER)))
                          .setObservations(cursor.getString(cursor.getColumnIndexOrThrow(OBSERVATIONS)));

                    entityList.add(entity);

                } while (cursor.moveToNext());
            }
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex.getCause());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return entityList;
    }
}
