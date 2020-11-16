package org.actus.types;

import org.actus.attributes.ContractModel;
import org.actus.contracts.ContractType;
import org.actus.events.ContractEvent;
import org.actus.externals.RiskFactorModelProvider;
import org.actus.states.StateSpace;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ContractReference {
    public ReferenceRole referenceRole;
    public ReferenceType referenceType;
    public Object object;
    public ContractReference(Map<String, Object> attributes, ContractRole contractRole) {
        this.referenceRole = ReferenceRole.valueOf((String)attributes.get("referenceRole"));
        this.referenceType = ReferenceType.valueOf((String)attributes.get("referenceType"));
        switch (referenceType){
            case CNT:
                Map<String, Object> childModel = (Map<String, Object>)attributes.get("object");
                if(contractRole.equals(ContractRole.RFL)){
                    if(ReferenceRole.FIL.equals(referenceRole)){
                        childModel.put("contractRole", "RPA");
                    } else {
                        childModel.put("contractRole", "RPL");
                    }
                } else{
                    if(ReferenceRole.FIL.equals(referenceRole)){
                        childModel.put("contractRole", "RPL");
                    } else {
                        childModel.put("contractRole", "RPA");
                    }
                }
                this.object = ContractModel.parse(childModel);
                break;
            case CID:
                this.object = ((Map<String,String>)attributes.get("object")).get("contractIdentifier");
                break;
            case MOC:
                this.object = ((Map<String,String>)attributes.get("object")).get("marketObjectCode");
                break;
            case EID:
                this.object = attributes.get("object");
                break;
            case CST:
                break;
            default:
                break;

        }
    }

    public Object getObject() {
        return object;
    }

    public String getContractAttribute(String contractAttribute){
        String attributeVal = null;
        if("MarketObjectCode".equals(contractAttribute)) {
            if (ReferenceType.MOC.equals(this.referenceType)) {
                attributeVal = (String) this.object;
            }
        }else if(ReferenceType.CNT.equals(this.referenceType)) {
            attributeVal = (((ContractModel) this.object).getAs(contractAttribute)).toString();
        }else if(ReferenceType.CID.equals(this.referenceType)){
            attributeVal = this.object.toString();
        }
        return attributeVal;
    }

    public StateSpace getStateSpaceAtTimepoint(LocalDateTime time, RiskFactorModelProvider observer){
        List<ContractEvent> events;
        if(ReferenceType.CNT.equals(this.referenceType)){
            events = ContractType.apply(ContractType.schedule(null,(ContractModel)this.object),(ContractModel)this.object,observer);
            events = events.stream().filter(e -> e.eventTime().equals(time)).collect(Collectors.toList());
            Collections.sort(events);
            return events.get(events.size()-1).states();
        }
        return new StateSpace();
    }

    public ContractEvent getEvent(EventType eventType, LocalDateTime time, RiskFactorModelProvider observer){
        List<ContractEvent> events = new ArrayList<>();
        if(ReferenceType.CNT.equals(this.referenceType)){
            events = ContractType.apply(ContractType.schedule(null,(ContractModel)this.object),(ContractModel)this.object,observer);
            events = events.stream().filter(e -> eventType.equals(e.eventType())).collect(Collectors.toList());
        }
        return events.get(0);
    }
}
