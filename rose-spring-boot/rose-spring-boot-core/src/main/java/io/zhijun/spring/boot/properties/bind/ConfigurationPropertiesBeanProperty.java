package io.zhijun.spring.boot.properties.bind;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.beans.Introspector.decapitalize;
import static org.springframework.core.ResolvableType.NONE;
import static org.springframework.core.ResolvableType.forField;
import static org.springframework.core.ResolvableType.forMethodParameter;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} Bean 属性
 */
public class ConfigurationPropertiesBeanProperty {

    private String name;

    private ResolvableType declaringClassType;

    private Method getter;

    private Method setter;

    private Field field;

    private Object value;

    public void setName(String name) {
        this.name = name;
    }

    public void setDeclaringClassType(ResolvableType declaringClassType) {
        this.declaringClassType = declaringClassType;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public void setSetter(Method setter) {
        this.setter = setter;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ResolvableType getDeclaringClassType() {
        return declaringClassType;
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    public Field getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public String getName() {
        if (this.name == null) {
            return resolveName();
        }
        return this.name;
    }

    String resolveName() {
        Field field = this.field;
        if (field != null) {
            return field.getName();
        }
        Method setterOrGetter = this.setter;
        if (setterOrGetter == null) {
            setterOrGetter = this.getter;
        }
        if (setterOrGetter != null) {
            String methodName = setterOrGetter.getName();
            return decapitalize(methodName.substring(3));
        }
        return this.name;
    }

    public ResolvableType getType() {
        if (this.setter != null) {
            MethodParameter methodParameter = new MethodParameter(this.setter, 0);
            return forMethodParameter(methodParameter, this.declaringClassType);
        }
        if (this.getter != null) {
            MethodParameter methodParameter = new MethodParameter(this.getter, -1);
            return forMethodParameter(methodParameter, this.declaringClassType);
        }
        if (this.field != null) {
            return forField(this.field, this.declaringClassType);
        }
        return NONE;
    }

    @Override
    public String toString() {
        return getType() + " " + getDeclaringClassType() + "." + getName() + " = " + getValue();
    }
}
