package com.github.bogdan.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.github.bogdan.databaseConfiguration.DatabaseConfiguration;
import com.github.bogdan.exceptions.WebException;
import com.github.bogdan.models.Group;
import com.github.bogdan.models.User;
import com.github.bogdan.models.UserGroup;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;

import java.io.IOException;
import java.sql.SQLException;

import static com.github.bogdan.services.DeserializerService.*;
import static com.github.bogdan.services.LocalDateService.checkLocalDateFormat;
import static com.github.bogdan.services.LocalDateService.checkValidDate;

public class UserGroupDeserializer extends StdDeserializer<UserGroup> {
    public UserGroupDeserializer() { super(UserGroup.class); }

    protected UserGroupDeserializer(JavaType valueType) {
        super(valueType);
    }

    protected UserGroupDeserializer(StdDeserializer<?> src) {
        super(src);
    }

    @Override
    public UserGroup deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Dao<User, Integer> userDao = null;
        Dao<Group, Integer> groupDao = null;
        try {



            String dateOfDrop;
            userDao = DaoManager.createDao(DatabaseConfiguration.connectionSource, User.class);
            groupDao = DaoManager.createDao(DatabaseConfiguration.connectionSource, Group.class);

            int userId = getIntFieldValue(node,"userId");
            if(userDao.queryForId(userId)==null){
                throw new WebException("User with such id doesn't exist",400);
            }

            int groupId = getIntFieldValue(node,"groupId");
            if(groupDao.queryForId(groupId)==null){
                throw new WebException("Group with such id doesn't exist",400);
            }

            String dateOfEnrollment = getDateFieldValue(node,"dateOfEnrollment");

            if (node.get("dateOfDrop") == null ) {
                dateOfDrop = null;
            }else if(node.get("dateOfDrop").asText() == ""){
                dateOfDrop = null;
            }else {
                dateOfDrop = node.get("dateOfDrop").asText();
                checkLocalDateFormat(dateOfDrop);
                checkValidDate(dateOfEnrollment,dateOfDrop);
            }



            UserGroup userGroup = new UserGroup(userDao.queryForId(userId),groupDao.queryForId(groupId),dateOfEnrollment,dateOfDrop);
            return userGroup;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
