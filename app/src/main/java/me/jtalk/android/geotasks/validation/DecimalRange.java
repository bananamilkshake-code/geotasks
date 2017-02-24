package me.jtalk.android.geotasks.validation;


import com.mobsandgeeks.saripaar.annotation.ValidateUsing;
import com.mobsandgeeks.saripaar.rule.NotEmptyRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ValidateUsing(DecimalRangeRule.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DecimalRange {
    double min();
    double max();

    int sequence()          default -1;
    int messageResId()      default -1;
    String message()        default "This field must be within a range";
}
