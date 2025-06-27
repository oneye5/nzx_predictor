package pojos.yahoo.financials;

import javax.print.attribute.standard.NumberUp;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Result {
    public static NullValue NULL_VALUE = new NullValue();
    static {
        NULL_VALUE.reportedValue = new ReportedValue();
        NULL_VALUE.asOfDate = "";
        NULL_VALUE.currencyCode = "NULL";
        NULL_VALUE.dataId = -1;
        NULL_VALUE.periodType = "NULL";
        NULL_VALUE.reportedValue.raw = Double.NaN;
        NULL_VALUE.reportedValue.fmt = "NaN";
    }

    public List<FinancialFeatureBase> getApplicableInfo(long time) {
        List<FinancialFeatureBase> applicableInfo = new ArrayList<>();

        if(timestamp == null) {
            return applicableInfo;
        }

        // Find the most recent timestamp that is <= the given time
        int applicableIndex = -1;
        for (int i = 0; i < timestamp.size(); i++) {
            if (timestamp.get(i) <= time) {
                if (applicableIndex == -1 || timestamp.get(i) > timestamp.get(applicableIndex)) {
                    applicableIndex = i;
                }
            }
        }

        // If no applicable timestamp found, return empty list
        if (applicableIndex == -1) {
            //for(int i = 0; i < 47; i++){ // return equivalent number of null values to maintain data width
            //    applicableInfo.add(NULL_VALUE);
            //}
            return applicableInfo;
        }

        // Helper method to safely add non-null items from lists
        addIfExists(applicableInfo, annualTaxEffectOfUnusualItems, applicableIndex);
        addIfExists(applicableInfo, annualNetIncomeContinuousOperations, applicableIndex);
        addIfExists(applicableInfo, annualOtherNonInterestExpense, applicableIndex);
        addIfExists(applicableInfo, annualAmortization, applicableIndex);
        addIfExists(applicableInfo, trailingTaxProvision, applicableIndex);
        addIfExists(applicableInfo, trailingTaxEffectOfUnusualItems, applicableIndex);
        addIfExists(applicableInfo, trailingRentAndLandingFees, applicableIndex);
        addIfExists(applicableInfo, trailingTotalPremiumsEarned, applicableIndex);
        addIfExists(applicableInfo, annualTotalRevenue, applicableIndex);
        addIfExists(applicableInfo, annualAverageDilutionEarnings, applicableIndex);
        addIfExists(applicableInfo, trailingSellingGeneralAndAdministration, applicableIndex);
        addIfExists(applicableInfo, annualDilutedNIAvailtoComStockholders, applicableIndex);
        addIfExists(applicableInfo, trailingFeesandCommissionExpense, applicableIndex);
        addIfExists(applicableInfo, trailingNetIncomeContinuousOperations, applicableIndex);
        addIfExists(applicableInfo, annualTotalPremiumsEarned, applicableIndex);
        addIfExists(applicableInfo, annualTaxRateForCalcs, applicableIndex);
        addIfExists(applicableInfo, trailingTaxRateForCalcs, applicableIndex);
        addIfExists(applicableInfo, annualWriteOff, applicableIndex);
        addIfExists(applicableInfo, trailingNetIncomeCommonStockholders, applicableIndex);
        addIfExists(applicableInfo, annualGainOnSaleOfSecurity, applicableIndex);
        addIfExists(applicableInfo, annualNetIncomeCommonStockholders, applicableIndex);
        addIfExists(applicableInfo, annualTotalUnusualItems, applicableIndex);
        addIfExists(applicableInfo, annualAmortizationOfIntangiblesIncomeStatement, applicableIndex);
        addIfExists(applicableInfo, annualDepreciationIncomeStatement, applicableIndex);
        addIfExists(applicableInfo, annualNonInterestExpense, applicableIndex);
        addIfExists(applicableInfo, trailingInterestIncome, applicableIndex);
        addIfExists(applicableInfo, annualNetIncome, applicableIndex);
        addIfExists(applicableInfo, annualFeesandCommissionExpense, applicableIndex);
        addIfExists(applicableInfo, trailingAverageDilutionEarnings, applicableIndex);
        addIfExists(applicableInfo, annualNetOccupancyExpense, applicableIndex);
        addIfExists(applicableInfo, trailingGeneralAndAdministrativeExpense, applicableIndex);
        addIfExists(applicableInfo, annualSellingGeneralAndAdministration, applicableIndex);
        addIfExists(applicableInfo, annualIncomefromAssociatesandOtherParticipatingInterests, applicableIndex);
        addIfExists(applicableInfo, trailingReconciledDepreciation, applicableIndex);
        addIfExists(applicableInfo, annualTaxProvision, applicableIndex);
        addIfExists(applicableInfo, annualGeneralAndAdministrativeExpense, applicableIndex);
        addIfExists(applicableInfo, trailingDepreciationIncomeStatement, applicableIndex);
        addIfExists(applicableInfo, trailingNetIncomeFromContinuingOperationNetMinorityInterest, applicableIndex);
        addIfExists(applicableInfo, annualOtherSpecialCharges, applicableIndex);
        addIfExists(applicableInfo, annualBasicEPS, applicableIndex);
        addIfExists(applicableInfo, trailingNetIncomeIncludingNoncontrollingInterests, applicableIndex);
        addIfExists(applicableInfo, annualNetIncomeIncludingNoncontrollingInterests, applicableIndex);
        addIfExists(applicableInfo, trailingNonInterestIncome, applicableIndex);
        addIfExists(applicableInfo, annualReconciledDepreciation, applicableIndex);
        addIfExists(applicableInfo, annualBasicAverageShares, applicableIndex);
        addIfExists(applicableInfo, annualNetIncomeFromContinuingOperationNetMinorityInterest, applicableIndex);
        addIfExists(applicableInfo, annualOccupancyAndEquipment, applicableIndex);

        return applicableInfo;
    }

    // Helper method to safely add items from lists, handling both FinancialFeatureBase and Object types
    private void addIfExists(List<FinancialFeatureBase> result, List<?> sourceList, int index) {
        if (sourceList != null && index >= 0
                && index < sourceList.size()
                && sourceList.get(index) != null
                && sourceList.get(index) instanceof FinancialFeatureBase
                && sourceList.get(index) != NULL_VALUE) {

            Object item = sourceList.get(index);
            result.add((FinancialFeatureBase) item);
        }
        else { // handle missing values
            result.add(NULL_VALUE);
        }
    }
    public Meta meta;
    public List<Integer> timestamp;
    public List<AnnualTaxEffectOfUnusualItem> annualTaxEffectOfUnusualItems;
    public List<AnnualNetIncomeContinuousOperation> annualNetIncomeContinuousOperations;
    public List<Object> annualOtherNonInterestExpense;
    public List<Object> annualAmortization;
    public List<TrailingTaxProvision> trailingTaxProvision;
    public List<TrailingTaxEffectOfUnusualItem> trailingTaxEffectOfUnusualItems;
    public List<TrailingRentAndLandingFee> trailingRentAndLandingFees;
    public List<TrailingTotalPremiumsEarned> trailingTotalPremiumsEarned;
    public List<AnnualTotalRevenue> annualTotalRevenue;
    public List<AnnualAverageDilutionEarning> annualAverageDilutionEarnings;
    public List<TrailingSellingGeneralAndAdministration> trailingSellingGeneralAndAdministration;
    public List<AnnualDilutedNIAvailtoComStockholder> annualDilutedNIAvailtoComStockholders;
    public List<TrailingFeesandCommissionExpense> trailingFeesandCommissionExpense;
    public List<TrailingNetIncomeContinuousOperation> trailingNetIncomeContinuousOperations;
    public List<AnnualTotalPremiumsEarned> annualTotalPremiumsEarned;
    public List<AnnualTaxRateForCalc> annualTaxRateForCalcs;
    public List<TrailingTaxRateForCalc> trailingTaxRateForCalcs;
    public List<AnnualWriteOff> annualWriteOff;
    public List<TrailingNetIncomeCommonStockholder> trailingNetIncomeCommonStockholders;
    public List<Object> annualGainOnSaleOfSecurity;
    public List<AnnualNetIncomeCommonStockholder> annualNetIncomeCommonStockholders;
    public List<AnnualTotalUnusualItem> annualTotalUnusualItems;
    public List<Object> annualAmortizationOfIntangiblesIncomeStatement;
    public List<AnnualDepreciationIncomeStatement> annualDepreciationIncomeStatement;
    public List<Object> annualNonInterestExpense;
    public List<TrailingInterestIncome> trailingInterestIncome;
    public List<AnnualNetIncome> annualNetIncome;
    public List<AnnualFeesandCommissionExpense> annualFeesandCommissionExpense;
    public List<TrailingAverageDilutionEarning> trailingAverageDilutionEarnings;
    public List<Object> annualNetOccupancyExpense;
    public List<TrailingGeneralAndAdministrativeExpense> trailingGeneralAndAdministrativeExpense;
    public List<AnnualSellingGeneralAndAdministration> annualSellingGeneralAndAdministration;
    public List<AnnualIncomefromAssociatesandOtherParticipatingInterest> annualIncomefromAssociatesandOtherParticipatingInterests;
    public List<TrailingReconciledDepreciation> trailingReconciledDepreciation;
    public List<AnnualTaxProvision> annualTaxProvision;
    public List<AnnualGeneralAndAdministrativeExpense> annualGeneralAndAdministrativeExpense;
    public List<TrailingDepreciationIncomeStatement> trailingDepreciationIncomeStatement;
    public List<TrailingNetIncomeFromContinuingOperationNetMinorityInterest> trailingNetIncomeFromContinuingOperationNetMinorityInterest;
    public List<AnnualOtherSpecialCharge> annualOtherSpecialCharges;
    public List<AnnualBasicEP> annualBasicEPS;
    public List<TrailingNetIncomeIncludingNoncontrollingInterest> trailingNetIncomeIncludingNoncontrollingInterests;
    public List<AnnualNetIncomeIncludingNoncontrollingInterest> annualNetIncomeIncludingNoncontrollingInterests;
    public List<TrailingNonInterestIncome> trailingNonInterestIncome;
    public List<AnnualReconciledDepreciation> annualReconciledDepreciation;
    public List<AnnualBasicAverageShare> annualBasicAverageShares;
    public List<AnnualNetIncomeFromContinuingOperationNetMinorityInterest> annualNetIncomeFromContinuingOperationNetMinorityInterest;
    public List<Object> annualOccupancyAndEquipment;
}
