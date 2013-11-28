package se.vgregion.service.pdl;

import org.springframework.stereotype.Service;
import se.vgregion.domain.pdl.CareSystem;
import se.vgregion.domain.pdl.InformationType;
import se.vgregion.domain.pdl.PdlContext;
import se.vgregion.domain.pdl.decorators.WithInfoType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class CareSystemsImpl implements CareSystems {

    @Override
    public List<WithInfoType<CareSystem>> byPatientId(PdlContext ctx, String patientId) {
        //noinspection unchecked
        return Collections
                .unmodifiableList(
                        Arrays
                            .asList(
                                    new WithInfoType<CareSystem>(
                                        InformationType.LAK,
                                        new CareSystem(
                                            "Bild- och funktionsregistret",
                                            "SE2321000131-E000000000001",
                                            "VGR",
                                            "SE2321000131-S000000010252",
                                            "Sahlgrenska, Radiologi"
                                        )
                                    ),
                                    new WithInfoType<CareSystem>(
                                        InformationType.UPP,
                                        new CareSystem(
                                            "Bild- och funktionsregistret",
                                            "SE2321000131-E000000000001",
                                            "VGR",
                                            "SE2321000131-S000000010252",
                                            "Östra Sjukhuset, Hud"
                                        )
                                    ),
                                    new WithInfoType<CareSystem>(
                                        InformationType.LAK,
                                        new CareSystem(
                                            "System X",
                                            "SE2321000131-E000000000001",
                                            "VGR",
                                            "SE2321000131-S000000010252",
                                            "NU-Sjukvården, Psykiatri"
                                        )
                                    ),
                                    new WithInfoType<CareSystem>(
                                        InformationType.LAK,
                                        new CareSystem(
                                            "Other Care Unit",
                                            "SE2321000131-E000000000001",
                                            "VGR",
                                            "SE2321000131-S000000010254",
                                            "Östra Sjukhuset, Barn- och Ungdomspsykiatri"
                                        )
                                    ),
                                    new WithInfoType<CareSystem>(
                                        InformationType.UPP,
                                        new CareSystem(
                                            "Other Care Giver System",
                                            "SE2321000132-E000000000001",
                                            "Capio Axess",
                                            "SE2321000132-S000000010254",
                                            "Lundby Sjukhus, Radiologi"
                                        )
                                    )
                            )
                );
    }
}

