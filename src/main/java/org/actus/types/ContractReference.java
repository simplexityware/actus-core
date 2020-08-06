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
                Map<String, String> childModel = (Map<String, String>)attributes.get("object");
                if(contractRole.equals(ContractRole.RFL)){
                    if(ReferenceRole.FIL.equals(referenceRole)){
                        childModel.put("ContractRole", "RPA");
                    } else {
                        childModel.put("ContractRole", "RPL");
                    }
                } else{
                    if(ReferenceRole.FIL.equals(referenceRole)){
                        childModel.put("ContractRole", "RPL");
                    } else {
                        childModel.put("ContractRole", "RPA");
                    }
                }
                attributes.replace("object", childModel);
                this.object = ContractModel.parse((Map<String, Object>)attributes.get("object"));
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
