package cm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Rate {
    private CarParkKind kind;
    private BigDecimal hourlyNormalRate;
    private BigDecimal hourlyReducedRate;
    private ArrayList<Period> reduced = new ArrayList<>();
    private ArrayList<Period> normal = new ArrayList<>();

    public Rate(CarParkKind kind, ArrayList<Period> reducedPeriods, ArrayList<Period> normalPeriods, BigDecimal normalRate, BigDecimal reducedRate) {
        // Ensures Car Park Kind cannot be null
        if (kind == null) {
            throw new IllegalArgumentException("CarParkKind cannot be null");
        }
        if (reducedPeriods == null || normalPeriods == null) {
            throw new IllegalArgumentException("periods cannot be null");
        }
        if (normalRate == null || reducedRate == null) {
            throw new IllegalArgumentException("The rates cannot be null");
        }
        // Ensures rate values do not exceed 10
        if (normalRate.compareTo(BigDecimal.TEN) > 0 || reducedRate.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("Rates cannot exceed 10");
        }
        if (normalRate.compareTo(BigDecimal.ZERO) < 0 || reducedRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("A rate cannot be negative");
        }
        if (normalRate.compareTo(reducedRate) <= 0) {
            throw new IllegalArgumentException("The normal rate cannot be less or equal to the reduced rate");
        }
        if (!isValidPeriods(reducedPeriods) || !isValidPeriods(normalPeriods)) {
            throw new IllegalArgumentException("The periods are not valid individually");
        }
        if (!isValidPeriods(reducedPeriods, normalPeriods)) {
            throw new IllegalArgumentException("The periods overlaps");
        }
        this.kind = kind;
        this.hourlyNormalRate = normalRate;
        this.hourlyReducedRate = reducedRate;
        this.reduced = reducedPeriods;
        this.normal = normalPeriods;
    }

    /**
     * Checks if two collections of periods are valid together
     * @param periods1
     * @param periods2
     * @return true if the two collections of periods are valid together
     */
    private boolean isValidPeriods(ArrayList<Period> periods1, ArrayList<Period> periods2) {
        Boolean isValid = true;
        int i = 0;
        while (i < periods1.size() && isValid) {
            isValid = isValidPeriod(periods1.get(i), periods2);
            i++;
        }
        return isValid;
    }

    /**
     * checks if a collection of periods is valid
     * @param list the collection of periods to check
     * @return true if the periods do not overlap
     */
    private Boolean isValidPeriods(ArrayList<Period> list) {
        Boolean isValid = true;
        if (list.size() >= 2) {
            Period secondPeriod;
            int i = 0;
            int lastIndex = list.size()-1;
            while (i < lastIndex && isValid) {
                isValid = isValidPeriod(list.get(i), ((List<Period>)list).subList(i + 1, lastIndex+1));
                i++;
            }
        }
        return isValid;
    }

    /**
     * checks if a period is a valid addition to a collection of periods
     * @param period the Period addition
     * @param list the collection of periods to check
     * @return true if the period does not overlap in the collecton of periods
     */
    private Boolean isValidPeriod(Period period, List<Period> list) {
        Boolean isValid = true;
        int i = 0;
        while (i < list.size() && isValid) {
            isValid = !period.overlaps(list.get(i));
            i++;
        }
        return isValid;
    }
    
    public BigDecimal calculate(Period periodStay) {
        int normalRateHours = periodStay.occurences(normal);
        int reducedRateHours = periodStay.occurences(reduced);
    
        BigDecimal total = (this.hourlyNormalRate.multiply(BigDecimal.valueOf(normalRateHours)))
                .add(this.hourlyReducedRate.multiply(BigDecimal.valueOf(reducedRateHours)));
    
        switch (this.kind) {
            case VISITOR:
                if (total.compareTo(BigDecimal.TEN) <= 0) {
                    return BigDecimal.ZERO; // Free if below or equal to 10
                } else {
                    return total.subtract(BigDecimal.TEN).multiply(new BigDecimal("0.50")); // 50% reduction above 10
                }
            case MANAGEMENT:
                return total.compareTo(new BigDecimal("4.00")) < 0 ? new BigDecimal("4.00") : total; // Minimum payable 4.00
            case STUDENT:
                if (total.compareTo(new BigDecimal("5.50")) <= 0) {
                    return total; // No reduction if below or equal to 5.50
                } else {
                    BigDecimal reduction = total.subtract(new BigDecimal("5.50")).multiply(new BigDecimal("0.25"));
                    return new BigDecimal("5.50").add(reduction); // 25% reduction above 5.50
                }
            case STAFF:
                return total.compareTo(new BigDecimal("16.00")) > 0 ? new BigDecimal("16.00") : total; // Max payable 16.00
            default:
                throw new IllegalArgumentException("Invalid CarParkKind");
        }
}
