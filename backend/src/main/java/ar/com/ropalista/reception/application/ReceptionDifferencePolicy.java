package ar.com.ropalista.reception.application;

import org.springframework.stereotype.Component;

@Component
public class ReceptionDifferencePolicy {
    private static final int ABSOLUTE_WEIGHT_TOLERANCE_GRAMS = 250;
    private static final double RELATIVE_WEIGHT_TOLERANCE = 0.10d;

    public Evaluation evaluate(int declaredPieces, int actualPieces,
                               Integer declaredWeightGrams, int actualWeightGrams,
                               boolean damageDetected) {
        int pieceDifference = actualPieces - declaredPieces;
        Integer weightDifference = declaredWeightGrams == null
                ? null
                : actualWeightGrams - declaredWeightGrams;
        boolean significantWeightDifference = weightDifference != null
                && Math.abs(weightDifference) > Math.max(
                        ABSOLUTE_WEIGHT_TOLERANCE_GRAMS,
                        Math.ceil(declaredWeightGrams * RELATIVE_WEIGHT_TOLERANCE));
        return new Evaluation(pieceDifference, weightDifference,
                pieceDifference != 0 || significantWeightDifference || damageDetected,
                significantWeightDifference);
    }

    public record Evaluation(int pieceDifference, Integer weightDifferenceGrams,
                             boolean requiresCustomerApproval,
                             boolean significantWeightDifference) {}
}
