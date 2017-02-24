package me.jtalk.android.geotasks.validation;


import android.util.Range;

import com.mobsandgeeks.saripaar.AnnotationRule;
import com.mobsandgeeks.saripaar.Validator;

import java.lang.annotation.Annotation;

public class DecimalRangeRule extends AnnotationRule<DecimalRange, Double> {

    /**
     * Constructor. It is mandatory that all subclasses MUST have a constructor with the same
     * signature.
     *
     * @param decimalRange The rule {@link Annotation} instance to which
     *                     this rule is paired.
     */
    protected DecimalRangeRule(DecimalRange decimalRange) {
        super(decimalRange);
    }

    @Override
    public boolean isValid(Double number) {
        double min = mRuleAnnotation.min();
        double max = mRuleAnnotation.max();
        return Range.create(min, max)
                .contains(number);
    }
}
