package web_scraper;

/**
 * This class contains the lengthy API URL's
 * as well as functionality for getting them with specified tickers.
 *
 * @author Owan Laizc
 */
public class ApiUrls {
  public static String getHistoricPricesUrl(String ticker){
    String x = HISTORIC_PRICES;
    x = x.replace("{TICKER}", ticker);
    return x;
  }
  public static String getFinancialInformationUrl(String ticker){
    String x = FINANCIAL_INFORMATION;
    x = x.replace("{TICKER}", ticker);
    return x;
  }
  public static String getNzCpi() {
    return NZ_CPI;
  }
  public static String getNzBusinessConfidence() {
    return NZ_BUSINESS_CONFIDENCE;
  }
  public static String getNzGdp() {
    return NZ_GDP;
  }
  public static String getNzVehicleRegistrations() {
    return NZ_VEHICLE_REGISTRATIONS;
  }

  // XML
  private static final String NZ_VEHICLE_REGISTRATIONS = "https://sdmx.oecd.org/public/rest/data/OECD.ITF,DSD_ST@DF_STREG,1.0/NZL.M...ROAD...";
  // XML
  private static final String NZ_GDP = "https://sdmx.oecd.org/public/rest/data/OECD.SDD.NAD,DSD_NAMAIN1@DF_QNA_EXPENDITURE_NATIO_CURR,1.1/Q..NZL.S13+S14.........?startPeriod=2000-Q1&dimensionAtObservation=AllDimensions&format=genericdata";
  // XML
  private static final String NZ_BUSINESS_CONFIDENCE = "https://sdmx.oecd.org/public/rest/data/OECD.SDD.STES,DSD_STES@DF_CLI,4.1/NZL.M.......?dimensionAtObservation=AllDimensions&format=genericdata";
  //JSON
  private static final String NZ_CPI = "https://sdmx.oecd.org/public/rest/data/OECD.SDD.STES,DSD_STES@DF_FINMARK,4.0/NZL.M..PA.....?dimensionAtObservation=AllDimensions&format=jsondata";
  //JSON
  private static final String HISTORIC_PRICES = "https://query1.finance.yahoo.com/v8/finance/chart/{TICKER}?interval=1d&period1=0&period2=99999999999&includeAdjustedClose=true";
  //JSON
  private static final String FINANCIAL_INFORMATION = "https://query1.finance.yahoo.com/ws/fundamentals-timeseries/v1/finance/timeseries/{TICKER}"
          + "?merge=false"
          + "&padTimeSeries=true"
          + "&period1=493590046"
          + "&period2=2750557599"
          + "&type=annualTaxEffectOfUnusualItems,trailingTaxEffectOfUnusualItems,annualTaxRateForCalcs,trailingTaxRateForCalcs,"
          + "annualNormalizedEBITDA,trailingNormalizedEBITDA,annualNormalizedDilutedEPS,trailingNormalizedDilutedEPS,"
          + "annualNormalizedBasicEPS,trailingNormalizedBasicEPS,annualTotalUnusualItems,trailingTotalUnusualItems,"
          + "annualTotalUnusualItemsExcludingGoodwill,trailingTotalUnusualItemsExcludingGoodwill,"
          + "annualNetIncomeFromContinuingOperationNetMinorityInterest,trailingNetIncomeFromContinuingOperationNetMinorityInterest,"
          + "annualReconciledDepreciation,trailingReconciledDepreciation,annualEBITDA,trailingEBITDA,annualEBIT,trailingEBIT,"
          + "annualTotalMoneyMarketInvestments,trailingTotalMoneyMarketInvestments,"
          + "annualContinuingAndDiscontinuedDilutedEPS,trailingContinuingAndDiscontinuedDilutedEPS,"
          + "annualContinuingAndDiscontinuedBasicEPS,trailingContinuingAndDiscontinuedBasicEPS,"
          + "annualNormalizedIncome,trailingNormalizedIncome,"
          + "annualNetIncomeFromContinuingAndDiscontinuedOperation,trailingNetIncomeFromContinuingAndDiscontinuedOperation,"
          + "annualInterestIncomeAfterProvisionForLoanLoss,trailingInterestIncomeAfterProvisionForLoanLoss,"
          + "annualRentExpenseSupplemental,trailingRentExpenseSupplemental,"
          + "annualReportedNormalizedDilutedEPS,trailingReportedNormalizedDilutedEPS,"
          + "annualReportedNormalizedBasicEPS,trailingReportedNormalizedBasicEPS,"
          + "annualDividendPerShare,trailingDividendPerShare,annualDilutedAverageShares,trailingDilutedAverageShares,"
          + "annualBasicAverageShares,trailingBasicAverageShares,annualDilutedEPS,trailingDilutedEPS,"
          + "annualDilutedEPSOtherGainsLosses,trailingDilutedEPSOtherGainsLosses,"
          + "annualTaxLossCarryforwardDilutedEPS,trailingTaxLossCarryforwardDilutedEPS,"
          + "annualDilutedAccountingChange,trailingDilutedAccountingChange,annualDilutedExtraordinary,"
          + "trailingDilutedExtraordinary,annualDilutedDiscontinuousOperations,trailingDilutedDiscontinuousOperations,"
          + "annualDilutedContinuousOperations,trailingDilutedContinuousOperations,annualBasicEPS,trailingBasicEPS,"
          + "annualBasicEPSOtherGainsLosses,trailingBasicEPSOtherGainsLosses,"
          + "annualTaxLossCarryforwardBasicEPS,trailingTaxLossCarryforwardBasicEPS,"
          + "annualBasicAccountingChange,trailingBasicAccountingChange,annualBasicExtraordinary,"
          + "trailingBasicExtraordinary,annualBasicDiscontinuousOperations,trailingBasicDiscontinuousOperations,"
          + "annualBasicContinuousOperations,trailingBasicContinuousOperations,"
          + "annualDilutedNIAvailtoComStockholders,trailingDilutedNIAvailtoComStockholders,"
          + "annualAverageDilutionEarnings,trailingAverageDilutionEarnings,"
          + "annualNetIncomeCommonStockholders,trailingNetIncomeCommonStockholders,"
          + "annualOtherThanPreferredStockDividend,trailingOtherThanPreferredStockDividend,"
          + "annualPreferredStockDividends,trailingPreferredStockDividends,"
          + "annualNetIncome,trailingNetIncome,annualMinorityInterests,trailingMinorityInterests,"
          + "annualNetIncomeIncludingNoncontrollingInterests,trailingNetIncomeIncludingNoncontrollingInterests,"
          + "annualNetIncomeFromTaxLossCarryforward,trailingNetIncomeFromTaxLossCarryforward,"
          + "annualNetIncomeExtraordinary,trailingNetIncomeExtraordinary,"
          + "annualNetIncomeDiscontinuousOperations,trailingNetIncomeDiscontinuousOperations,"
          + "annualNetIncomeContinuousOperations,trailingNetIncomeContinuousOperations,"
          + "annualEarningsFromEquityInterestNetOfTax,trailingEarningsFromEquityInterestNetOfTax,"
          + "annualTaxProvision,trailingTaxProvision,annualPretaxIncome,trailingPretatIncome,"
          + "annualOtherNonOperatingIncomeExpenses,trailingOtherNonOperatingIncomeExpenses,"
          + "annualSpecialIncomeCharges,trailingSpecialIncomeCharges,annualOtherSpecialCharges,trailingOtherSpecialCharges,"
          + "annualLossonExtinguishmentofDebt,trailingLossonExtinguishmentofDebt,"
          + "annualWriteOff,trailingWriteOff,annualImpairmentOfCapitalAssets,trailingImpairmentOfCapitalAssets,"
          + "annualRestructuringAndMergernAcquisition,trailingRestructuringAndMergernAcquisition,"
          + "annualGainOnSaleOfBusiness,trailingGainOnSaleOfBusiness,"
          + "annualIncomefromAssociatesandOtherParticipatingInterests,"
          + "trailingIncomefromAssociatesandOtherParticipatingInterests,"
          + "annualNonInterestExpense,trailingNonInterestExpense,annualOtherNonInterestExpense,"
          + "trailingOtherNonInterestExpense,annualSecuritiesAmortization,trailingSecuritiesAmortization,"
          + "annualDepreciationAmortizationDepletionIncomeStatement,trailingDepreciationAmortizationDepletionIncomeStatement,"
          + "annualDepletionIncomeStatement,trailingDepletionIncomeStatement,"
          + "annualDepreciationAndAmortizationInIncomeStatement,trailingDepreciationAndAmortizationInIncomeStatement,"
          + "annualAmortization,trailingAmortization,"
          + "annualAmortizationOfIntangiblesIncomeStatement,trailingAmortizationOfIntangiblesIncomeStatement,"
          + "annualDepreciationIncomeStatement,trailingDepreciationIncomeStatement,"
          + "annualSellingGeneralAndAdministration,trailingSellingGeneralAndAdministration,"
          + "annualSellingAndMarketingExpense,trailingSellingAndMarketingExpense,"
          + "annualGeneralAndAdministrativeExpense,trailingGeneralAndAdministrativeExpense,"
          + "annualOtherGandA,trailingOtherGandA,annualInsuranceAndClaims,trailingInsuranceAndClaims,"
          + "annualRentAndLandingFees,trailingRentAndLandingFees,annualSalariesAndWages,trailingSalariesAndWages,"
          + "annualProfessionalExpenseAndContractServicesExpense,trailingProfessionalExpenseAndContractServicesExpense,"
          + "annualOccupancyAndEquipment,trailingOccupancyAndEquipment,annualEquipment,trailingEquipment,"
          + "annualNetOccupancyExpense,trailingNetOccupancyExpense,annualCreditLossesProvision,trailingCreditLossesProvision,"
          + "annualTotalRevenue,trailingTotalRevenue,annualNonInterestIncome,trailingNonInterestIncome,"
          + "annualOtherNonInterestIncome,trailingOtherNonInterestIncome,"
          + "annualGainLossonSaleofAssets,trailingGainLossonSaleofAssets,"
          + "annualGainonSaleofInvestmentProperty,trailingGainonSaleofInvestmentProperty,"
          + "annualGainonSaleofLoans,trailingGainonSaleofLoans,annualGainOnSaleOfSecurity,trailingGainOnSaleOfSecurity,"
          + "annualForeignExchangeTradingGains,trailingForeignExchangeTradingGains,"
          + "annualTradingGainLoss,trailingTradingGainLoss,"
          + "annualInvestmentBankingProfit,trailingInvestmentBankingProfit,annualDividendIncome,trailingDividendIncome,"
          + "annualFeesAndCommissions,trailingFeesAndCommissions,"
          + "annualFeesandCommissionExpense,trailingFeesandCommissionExpense,"
          + "annualFeesandCommissionIncome,trailingFeesandCommissionIncome,"
          + "annualOtherCustomerServices,trailingOtherCustomerServices,"
          + "annualCreditCard,trailingCreditCard,annualSecuritiesActivities,trailingSecuritiesActivities,"
          + "annualTrustFeesbyCommissions,trailingTrustFeesbyCommissions,"
          + "annualServiceChargeOnDepositorAccounts,trailingServiceChargeOnDepositorAccounts,"
          + "annualTotalPremiumsEarned,trailingTotalPremiumsEarned,"
          + "annualNetInterestIncome,trailingNetInterestIncome,"
          + "annualInterestExpense,trailingInterestExpense,annualOtherInterestExpense,trailingOtherInterestExpense,"
          + "annualInterestExpenseForFederalFundsSoldAndSecuritiesPurchaseUnderAgreementsToResell,"
          + "trailingInterestExpenseForFederalFundsSoldAndSecuritiesPurchaseUnderAgreementsToResell,"
          + "annualInterestExpenseForLongTermDebtAndCapitalSecurities,"
          + "trailingInterestExpenseForLongTermDebtAndCapitalSecurities,"
          + "annualInterestExpenseForShortTermDebt,trailingInterestExpenseForShortTermDebt,"
          + "annualInterestExpenseForDeposit,trailingInterestExpenseForDeposit,"
          + "annualInterestIncome,trailingInterestIncome,annualOtherInterestIncome,trailingOtherInterestIncome,"
          + "annualInterestIncomeFromFederalFundsSoldAndSecuritiesPurchaseUnderAgreementsToResell,"
          + "trailingInterestIncomeFromFederalFundsSoldAndSecuritiesPurchaseUnderAgreementsToResell,"
          + "annualInterestIncomeFromDeposits,trailingInterestIncomeFromDeposits,"
          + "annualInterestIncomeFromSecurities,trailingInterestIncomeFromSecurities,"
          + "annualInterestIncomeFromLoansAndLease,trailingInterestIncomeFromLoansAndLease,"
          + "annualInterestIncomeFromLeases,trailingInterestIncomeFromLeases,"
          + "annualInterestIncomeFromLoans,trailingInterestIncomeFromLoans"
          + "&lang=en-NZ"
          + "&region=NZ";
}
