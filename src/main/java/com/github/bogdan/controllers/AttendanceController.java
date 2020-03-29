package com.github.bogdan.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.bogdan.deserializer.DeserializerForAddAttendance;
import com.github.bogdan.deserializer.ScheduleDeserializer;
import com.github.bogdan.modals.Attendance;
import com.github.bogdan.modals.Role;
import com.github.bogdan.modals.Schedule;
import com.j256.ormlite.dao.Dao;
import io.javalin.http.Context;

import java.sql.SQLException;

import static com.github.bogdan.services.AuthService.*;
import static com.github.bogdan.services.ContextService.*;
import static com.github.bogdan.services.UserService.*;

public class AttendanceController {
    public static void add(Context ctx, Dao<Attendance,Integer> attendanceDao) throws SQLException, JsonProcessingException {
        String login = ctx.basicAuthCredentials().getUsername();
        String password = ctx.basicAuthCredentials().getPassword();
        checkAuthorization(login,password,ctx);
        if(getUserByLogin(login).getRole()== Role.ADMIN){
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addDeserializer(Attendance.class,new DeserializerForAddAttendance());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(simpleModule);
            Attendance attendance = objectMapper.readValue(ctx.body(),Attendance.class);

            attendanceDao.create(attendance);
            created(ctx);
        }else youAreNotAdmin(ctx);

    }
}