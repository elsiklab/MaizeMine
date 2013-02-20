package org.intermine.api.bag.operations;

import java.util.Collection;

import org.intermine.api.profile.InterMineBag;
import org.intermine.api.profile.Profile;
import org.intermine.metadata.DescriptorUtils;
import org.intermine.metadata.MetaDataException;
import org.intermine.metadata.Model;
import org.intermine.objectstore.query.ObjectStoreBagCombination;

public class Union extends BagOperation {

    public Union(Model model, Profile profile, Collection<InterMineBag> bags) {
        super(model, profile, bags);
    }

    @Override
    protected String getNewBagType() throws IncompatibleTypes {
        try {
            return DescriptorUtils.findSumType(getClasses()).getUnqualifiedName();
        } catch (MetaDataException e) {
            throw new IncompatibleTypes(e);
        }
    }

    @Override
    protected int getOperationCode() {
        return ObjectStoreBagCombination.UNION;
    }

}
