package com.paw.bettertrello.repositories.util;

import org.springframework.data.mapping.MappingException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

public class CascadeCallback implements ReflectionUtils.FieldCallback {

    private Object source;
    private MongoOperations mongoOperations;

    CascadeCallback(final Object source, final MongoOperations mongoOperations) {
        this.source = source;
        this.setMongoOperations(mongoOperations);
    }

    @Override
    public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
        ReflectionUtils.makeAccessible(field);

        /*
        leave it for the future..
        System.out.println(field.getName());
        Annotation[] tab = field.getDeclaredAnnotations();
        for (Annotation a : tab) {
            System.out.println(a.annotationType().getName());
        }
        */

        if (field.isAnnotationPresent(DBRef.class) && field.isAnnotationPresent(CascadeSave.class)) {
            final Object fieldValue = field.get(getSource());
            if (fieldValue != null) {
                final FieldCallback callback = new FieldCallback();

                ReflectionUtils.doWithFields(field.getDeclaringClass(), callback);

                if (!callback.isIdFound()) {
                    throw new MappingException("Cannot perform cascade save on child object without id set");
                }

                if(fieldValue instanceof List<?>){
                    for (Object item : (List<?>)fieldValue){
                        getMongoOperations().save(item);
                    }
                }
                else{
                    getMongoOperations().save(fieldValue);
                }
            }
        }

    }

    private Object getSource() {
        return source;
    }

    public void setSource(final Object source) {
        this.source = source;
    }

    private MongoOperations getMongoOperations() {
        return mongoOperations;
    }

    private void setMongoOperations(final MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }
}
