package se.vgregion.service.pdl;


import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.riv.ehr.blocking.accesscontrol.checkblocks.v3.rivtabp21.CheckBlocksResponderInterface;
import se.riv.ehr.blocking.accesscontrol.checkblocks.v3.rivtabp21.CheckBlocksResponderService;
import se.riv.ehr.blocking.accesscontrol.checkblocksresponder.v3.CheckBlocksRequestType;
import se.riv.ehr.patientconsent.accesscontrol.checkconsent.v1.rivtabp21.CheckConsentResponderInterface;
import se.riv.ehr.patientconsent.accesscontrol.checkconsent.v1.rivtabp21.CheckConsentResponderService;
import se.riv.ehr.patientconsent.accesscontrol.checkconsentresponder.v1.CheckConsentRequestType;
import se.riv.ehr.patientrelationship.accesscontrol.checkpatientrelation.v1.rivtabp21.CheckPatientRelationResponderInterface;
import se.riv.ehr.patientrelationship.accesscontrol.checkpatientrelation.v1.rivtabp21.CheckPatientRelationResponderService;
import se.riv.ehr.patientrelationship.accesscontrol.checkpatientrelationresponder.v1.CheckPatientRelationRequestType;
import se.vgregion.domain.pdl.PatientEngagement;
import se.vgregion.domain.pdl.PdlContext;
import se.vgregion.domain.pdl.PdlReport;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

public class PdlServiceSpecification {


    @Mock
    private CheckBlocksResponderService blocksForPatient;
    @Mock
    private CheckBlocksResponderInterface blocksInterface;
    @Mock
    private CheckConsentResponderService consentForPatient;
    @Mock
    private CheckConsentResponderInterface consentInterface;
    @Mock
    private CheckPatientRelationResponderService relationshipWithPatient;
    @Mock
    private CheckPatientRelationResponderInterface relationshipInterface;

    @InjectMocks
    private PdlServiceImpl service = new PdlServiceImpl();

    private PdlContext ctx;

    @Before
    public void setuUp() {
        MockitoAnnotations.initMocks(this);

        List<PatientEngagement> engagements = Arrays.asList(
                new PatientEngagement(
                        "careProviderHsaId",
                        "careUnitHsaId",
                        "employeeHsaId",
                        PatientEngagement.InformationType.OTHR
                )
        );

        ctx = new PdlContext(
                "patientHsaId",
                engagements,
                "careProviderHsaId",
                "careUnitHsaId",
                "employeeHsaId"
        );

        // Setup interfaces
        when(blocksForPatient.getCheckBlocksResponderPort()).thenReturn(blocksInterface);
        when(consentForPatient.getCheckConsentResponderPort()).thenReturn(consentInterface);
        when(relationshipWithPatient.getCheckPatientRelationResponderPort()).thenReturn(relationshipInterface);
    }

    @Test
    public void reportHasBlocks() throws Exception {

        when(blocksInterface.checkBlocks(eq(ctx.careProviderHsaId), isA(CheckBlocksRequestType.class))).
                thenAnswer(BlockingSpec.blockingRequestAndRespond(ctx, 0, true));

        when(consentInterface.checkConsent(anyString(), isA(CheckConsentRequestType.class))).
                thenReturn(ConsentSpec.consentResult(false, false));  // Not under test, avoid null pointer

        when(relationshipInterface.checkPatientRelation(anyString(), isA(CheckPatientRelationRequestType.class))).
            thenReturn(RelationshipSpec.relationshipResult(false));  // Not under test, avoid null pointer

        PdlReport pdlReport = service.pdlReport(ctx);

        assertTrue(pdlReport.hasBlocks);
        assertEquals(1, pdlReport.blocks.size());
    }

    @Test
    public void reportWithoutBlocks() throws Exception {
        when(blocksInterface.checkBlocks(eq(ctx.careProviderHsaId), isA(CheckBlocksRequestType.class))).
                thenReturn(BlockingSpec.blockedResult(0, false));

        when(consentInterface.checkConsent(anyString(), isA(CheckConsentRequestType.class))).
                thenReturn(ConsentSpec.consentResult(false, false)); // Not under test, avoid null pointer

        when(relationshipInterface.checkPatientRelation(anyString(), isA(CheckPatientRelationRequestType.class))).
                thenReturn(RelationshipSpec.relationshipResult(false));  // Not under test, avoid null pointer

        PdlReport pdlReport = service.pdlReport(ctx);

        assertFalse(pdlReport.hasBlocks);
        assertEquals(1, pdlReport.blocks.size());
    }


    @Test
    public void reportHasConsent() throws Exception {
        when(blocksInterface.checkBlocks(anyString(), isA(CheckBlocksRequestType.class))).
                thenReturn(BlockingSpec.blockedResult(0, true)); // Not under test, avoid null pointer

        when(consentInterface.checkConsent(eq(ctx.careProviderHsaId), isA(CheckConsentRequestType.class))).
                thenAnswer(ConsentSpec.consentRequestAndRespond(ctx, true, false));

        when(relationshipInterface.checkPatientRelation(anyString(), isA(CheckPatientRelationRequestType.class))).
                thenReturn(RelationshipSpec.relationshipResult(false));  // Not under test, avoid null pointer

        PdlReport pdlReport = service.pdlReport(ctx);

        assertTrue(pdlReport.hasConsent);
        assertEquals(PdlReport.ConsentType.CONSENT, pdlReport.consentType);
    }

    @Test
    public void reportWithEmergencyConsent() throws Exception {
        when(blocksInterface.checkBlocks(anyString(), isA(CheckBlocksRequestType.class))).
                thenReturn(BlockingSpec.blockedResult(0, true)); // Not under test, avoid null pointer

        when(consentInterface.checkConsent(eq(ctx.careProviderHsaId), isA(CheckConsentRequestType.class))).
                thenAnswer(ConsentSpec.consentRequestAndRespond(ctx, true, true));

        when(relationshipInterface.checkPatientRelation(anyString(), isA(CheckPatientRelationRequestType.class))).
                thenReturn(RelationshipSpec.relationshipResult(false));  // Not under test, avoid null pointer

        PdlReport pdlReport = service.pdlReport(ctx);

        assertTrue(pdlReport.hasConsent);
        assertEquals(PdlReport.ConsentType.EMERGENCY, pdlReport.consentType);
    }

    @Test
    public void reportWithoutConsent() throws Exception {
        when(blocksInterface.checkBlocks(anyString(), isA(CheckBlocksRequestType.class))).
                thenReturn(BlockingSpec.blockedResult(0, true)); // Not under test, avoid null pointer

        when(consentInterface.checkConsent(eq(ctx.careProviderHsaId), isA(CheckConsentRequestType.class))).
                thenAnswer(ConsentSpec.consentRequestAndRespond(ctx, false, true));

        when(relationshipInterface.checkPatientRelation(anyString(), isA(CheckPatientRelationRequestType.class))).
                thenReturn(RelationshipSpec.relationshipResult(false));  // Not under test, avoid null pointer

        PdlReport pdlReport = service.pdlReport(ctx);

        assertFalse(pdlReport.hasConsent);
    }

    @Test
    public void reportHasRelationship() throws Exception {
        when(blocksInterface.checkBlocks(anyString(), isA(CheckBlocksRequestType.class))).
                thenReturn(BlockingSpec.blockedResult(0, true)); // Not under test, avoid null pointer

        when(consentInterface.checkConsent(anyString(), isA(CheckConsentRequestType.class))).
                thenReturn(ConsentSpec.consentResult(false, true)); // Not under test, avoid null pointer

        when(relationshipInterface.checkPatientRelation(eq(ctx.careProviderHsaId), isA(CheckPatientRelationRequestType.class))).
                thenAnswer(RelationshipSpec.relationshipRequestAndResponse(ctx, true));

        PdlReport pdlReport = service.pdlReport(ctx);

        assertTrue(pdlReport.hasRelationship);
    }


    @Test
    public void reportNoRelationship() throws Exception {
        when(blocksInterface.checkBlocks(anyString(), isA(CheckBlocksRequestType.class))).
                thenReturn(BlockingSpec.blockedResult(0, true)); // Not under test, avoid null pointer

        when(consentInterface.checkConsent(anyString(), isA(CheckConsentRequestType.class))).
                thenReturn(ConsentSpec.consentResult(false, true)); // Not under test, avoid null pointer

        when(relationshipInterface.checkPatientRelation(eq(ctx.careProviderHsaId), isA(CheckPatientRelationRequestType.class))).
                thenAnswer(RelationshipSpec.relationshipRequestAndResponse(ctx, false));

        PdlReport pdlReport = service.pdlReport(ctx);

        assertFalse(pdlReport.hasRelationship);
    }



}
