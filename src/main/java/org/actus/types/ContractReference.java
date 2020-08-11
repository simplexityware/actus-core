package org.actus.types;

import org.actus.attributes.ContractModel;
import org.actus.types.ContractRole;
import org.actus.types.ReferenceRole;
import org.actus.types.ReferenceType;

import java.util.Map;

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
            case MOC:
            case EID:
                this.object = attributes.get("object");
                break;
            case CST:
                break;
                default:
                    break;

        }
    }

    public ReferenceRole getReferenceRole() {
        return referenceRole;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public Object getObject() {
        return object;
    }
}
