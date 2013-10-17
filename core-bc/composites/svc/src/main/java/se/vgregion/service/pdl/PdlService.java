package se.vgregion.service.pdl;

import se.vgregion.domain.pdl.*;

import java.util.List;

public interface PdlService {
    enum UnblockType {
        CONSENT, EMERGENCY
    }

    PdlReport pdlReport(PdlContext ctx, PatientWithEngagements patientEngagements);

    PdlReport patientConsent(PdlContext ctx);

    PdlReport patientRelationship(PdlContext ctx);

    PdlReport unblockInformation(PdlContext ctx, String blockId, UnblockType unblockType, String unblockComment);

    PdlAssertion chooseInformation(PdlContext ctx, PdlReport report, List<Engagement> engagements);
}
