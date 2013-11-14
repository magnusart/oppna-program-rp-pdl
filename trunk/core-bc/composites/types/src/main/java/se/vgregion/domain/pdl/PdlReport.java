package se.vgregion.domain.pdl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PdlReport implements Serializable {
    private static final long serialVersionUID = -597284170511725549L;
    public final WithFallback<Boolean> hasBlocks;
    public final List<CheckedBlock> blocks;
    public final WithFallback<Boolean> hasConsent;
    public final ConsentType consentType;
    public final WithFallback<Boolean> hasRelationship;

    public PdlReport(
            WithFallback<ArrayList<CheckedBlock>> checkedBlocks,
            WithFallback<CheckedConsent> checkedConsent,
            WithFallback<Boolean> hasRelationship
    ) {
        this.hasRelationship = hasRelationship;
        hasBlocks = containsBlocked(checkedBlocks);
        blocks = checkedBlocks.value;
        this.hasConsent = isConsentWithFallback(checkedConsent);
        this.consentType = checkedConsent.value.consentType;
    }

    // Private, for copy only
    private PdlReport(
            WithFallback<Boolean> hasBlocks,
            List<CheckedBlock> blocks,
            WithFallback<Boolean> hasConsent,
            ConsentType consentType,
            WithFallback<Boolean> hasRelationship
    ) {
        this.hasBlocks = hasBlocks;
        this.blocks = blocks;
        this.hasConsent = hasConsent;
        this.consentType = consentType;
        this.hasRelationship = hasRelationship;
    }

    private WithFallback<Boolean> isConsentWithFallback(WithFallback<CheckedConsent> checkedConsent) {
        if (checkedConsent.fallback) {
            return WithFallback.fallback(checkedConsent.value.hasConsent);
        } else {
            return WithFallback.success(checkedConsent.value.hasConsent);
        }
    }

    private WithFallback<Boolean> containsBlocked(WithFallback<ArrayList<CheckedBlock>> checkedBlocks) {

        for (CheckedBlock b : checkedBlocks.value) {
            if (b.blocked == CheckedBlock.BlockStatus.BLOCKED) {
                return new WithFallback<Boolean>(true, checkedBlocks.fallback);
            }
        }
        return new WithFallback<Boolean>(false, checkedBlocks.fallback);
    }

    public PdlReport withRelationship(WithFallback<Boolean> newHasRelationship) {
        return new PdlReport(
                hasBlocks,
                blocks,
                hasConsent,
                consentType,
                newHasRelationship);
    }

    public PdlReport withConsent(WithFallback<Boolean> newConsentStatus, ConsentType newConsentType) {
        return new PdlReport(
                hasBlocks,
                blocks,
                newConsentStatus,
                newConsentType,
                hasRelationship);
    }

    public WithFallback<Boolean> getHasBlocks() {
        return hasBlocks;
    }

    public List<CheckedBlock> getBlocks() {
        return blocks;
    }

    public WithFallback<Boolean> getHasConsent() {
        return hasConsent;
    }

    public ConsentType getConsentType() {
        return consentType;
    }

    public WithFallback<Boolean> getHasRelationship() {
        return hasRelationship;
    }

    @Override
    public String toString() {
        return "PdlReport{" +
                "hasBlocks=" + hasBlocks +
                ", blocks=" + blocks +
                ", hasConsent=" + hasConsent +
                ", consentType=" + consentType +
                ", hasRelationship=" + hasRelationship +
                '}';
    }

    public enum ConsentType {
        Consent, Emergency
    }
}