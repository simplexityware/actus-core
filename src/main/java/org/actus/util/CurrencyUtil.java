package org.actus.util;

import org.actus.attributes.ContractModelProvider;
import org.actus.externals.RiskFactorModelProvider;

import java.time.LocalDateTime;

/**
 * A utility class for Currency
 * <p>
 */
public class CurrencyUtil {
    //This is a pure utility class
    private CurrencyUtil(){}

    /**
     * Calculate the Settlement Currency Rate
     * <p>
     * @param model the model carrying the contract attributes
     *@param riskFactorModel an external market model
     * @return the fx-rate
     */
    public static double settlmentCurrencyFxRate(RiskFactorModelProvider riskFactorModel, ContractModelProvider model, LocalDateTime time){
        String settlementCurrency = model.getAs("SettlementCurrency");
        String currency = model.getAs("Currency");
        if(CommonUtils.isNull(settlementCurrency)  || currency.equals(settlementCurrency)){
            return 1;
        }else {
            return riskFactorModel.stateAt(currency + "/" + settlementCurrency, time);
        }
    }
}
