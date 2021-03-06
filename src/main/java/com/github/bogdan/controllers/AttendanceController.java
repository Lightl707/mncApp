package com.github.bogdan.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.bogdan.deserializer.DeserializerForAddAttendance;
import com.github.bogdan.deserializer.DeserializerForChangeAttendance;
import com.github.bogdan.models.Attendance;
import com.github.bogdan.models.Role;
import com.github.bogdan.models.User;
import com.github.bogdan.serializer.UserForGroupSerializer;
import com.j256.ormlite.dao.Dao;
import io.javalin.http.Context;
import java.sql.SQLException;
import static com.github.bogdan.services.AttendanceService.checkDoesSuchAttendanceExist;
import static com.github.bogdan.services.AttendanceService.checkUniqueAttendance;
import static com.github.bogdan.services.AuthService.*;
import static com.github.bogdan.services.ContextService.*;
import static com.github.bogdan.services.PaginationService.getPage;
import static com.github.bogdan.services.UserService.*;

public class AttendanceController {
    public static void add(Context ctx, Dao<Attendance,Integer> attendanceDao) throws SQLException, JsonProcessingException {
        checkDoesBasicAuthEmpty(ctx);

        String login = ctx.basicAuthCredentials().getUsername();
        String password = ctx.basicAuthCredentials().getPassword();

        checkAuthorization(login,password,ctx);

        checkDoesRequestBodyEmpty(ctx);

        if(getUserByLogin(login).getRole()== Role.ADMIN){
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addDeserializer(Attendance.class,new DeserializerForAddAttendance());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(simpleModule);
            Attendance attendance = objectMapper.readValue(ctx.body(),Attendance.class);
            checkUniqueAttendance(attendance);
            attendanceDao.create(attendance);
            created(ctx);
        }else youAreNotAdmin(ctx);
    }
    public static void delete(Context ctx, Dao<Attendance,Integer> attendanceDao) throws SQLException {
        checkDoesBasicAuthEmpty(ctx);
        String login = ctx.basicAuthCredentials().getUsername();
        String password = ctx.basicAuthCredentials().getPassword();
        checkAuthorization(login,password,ctx);
        if(getUserByLogin(login).getRole()== Role.ADMIN){
            int id = Integer.parseInt(ctx.pathParam("id"));
            checkDoesSuchAttendanceExist(id);
            attendanceDao.deleteById(id);
            deleted(ctx);
        }else youAreNotAdmin(ctx);
    }
    public static void get(Context ctx, Dao<Attendance,Integer> attendanceDao) throws SQLException, JsonProcessingException {
        checkDoesBasicAuthEmpty(ctx);
        String login = ctx.basicAuthCredentials().getUsername();
        String password = ctx.basicAuthCredentials().getPassword();
        checkAuthorization(login,password,ctx);
        if(getUserByLogin(login).getRole()== Role.ADMIN){
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addSerializer(User.class, new UserForGroupSerializer());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(simpleModule);
            checkDoesQueryParamEmpty(ctx,"page");
            checkDoesQueryParamEmpty(ctx,"size");
            int page = Integer.parseInt(ctx.queryParam("page"));
            int size = Integer.parseInt(ctx.queryParam("size"));
            ctx.result(objectMapper.writeValueAsString(getPage(attendanceDao,page,size)));
            ctx.status(200);
        }else youAreNotAdmin(ctx);
    }
    public static void getById(Context ctx, Dao<Attendance,Integer> attendanceDao) throws SQLException, JsonProcessingException {
        checkDoesBasicAuthEmpty(ctx);
        String login = ctx.basicAuthCredentials().getUsername();
        String password = ctx.basicAuthCredentials().getPassword();
        checkAuthorization(login,password,ctx);
        if(getUserByLogin(login).getRole()== Role.ADMIN){
            ObjectMapper objectMapper = new ObjectMapper();
            int id = Integer.parseInt(ctx.pathParam("id"));
            checkDoesSuchAttendanceExist(id);
            ctx.result(objectMapper.writeValueAsString(attendanceDao.queryForId(id)));
            ctx.status(200);
        }else youAreNotAdmin(ctx);
    }
    public static void change(Context ctx, Dao<Attendance,Integer> attendanceDao) throws SQLException, JsonProcessingException {
        checkDoesBasicAuthEmpty(ctx);
        String login = ctx.basicAuthCredentials().getUsername();
        String password = ctx.basicAuthCredentials().getPassword();
        checkAuthorization(login,password,ctx);
        if(getUserByLogin(login).getRole()== Role.ADMIN){
            checkDoesRequestBodyEmpty(ctx);
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addDeserializer(Attendance.class,new DeserializerForChangeAttendance());
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(simpleModule);

            Attendance attendance = objectMapper.readValue(ctx.body(),Attendance.class);

            attendanceDao.update(attendance);
            updated(ctx);
        }else youAreNotAdmin(ctx);
    }
}
