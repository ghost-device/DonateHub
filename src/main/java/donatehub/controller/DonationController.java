package donatehub.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import donatehub.domain.entities.UserEntity;
import donatehub.domain.constants.PaymentMethod;
import donatehub.domain.response.PagedResponse;
import donatehub.domain.projections.DonationInfo;
import donatehub.domain.request.DonationCreateRequest;
import donatehub.domain.response.ExceptionResponse;
import donatehub.domain.response.CreateDonateResponse;
import donatehub.domain.response.DonationStatisticResponse;
import donatehub.service.donation.DonationService;

import java.util.List;

/**
 * DonationController - Donatsiyalarni boshqarish va statistikalarni olish uchun REST API.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/donation")
@Tag(name = "Donation")
public class DonationController {
    private final DonationService donationService;
    private final Logger log;

    @Hidden
    @PostMapping("/complete/{method}")
    @Operation(
            summary = "Donatsiyani to'liq amalga oshirish",
            description = "Ushbu metod to'lov usuliga qarab donatsiyani to'liq amalga oshirish uchun ishlatiladi.",
            parameters = {
                    @Parameter(name = "method", description = "To'lov usuli (CLICK yoki MIRPAY)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "To'liq amalga oshirilgan donatsiya")
            }
    )
    public void complete(
            @RequestBody String body,
            @PathVariable PaymentMethod method
    ) {
        log.info("To'liq amalga oshirish so'rovi: {}", body);
        donationService.complete(body, method);
    }

    @PostMapping("/{streamerId}")
    @Operation(
            summary = "Donatsiya yaratish",
            description = "Ushbu metod yangi donatsiya yaratadi va uni ko'rsatilgan streamerga bog'laydi.",
            parameters = {
                    @Parameter(name = "streamerId", description = "Streamer identifikatori", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Donatsiya yaratish uchun so'rov ma'lumotlari",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DonationCreateRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Muvaffaqiyatli donatsiya", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateDonateResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Streamer topilmasa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Noto'g'ri so'rov ma'lumotlari", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    public ResponseEntity<CreateDonateResponse> donate(
            @PathVariable Long streamerId,
            @RequestBody @Valid DonationCreateRequest donationCreateRequest
    ) {
        CreateDonateResponse donate = donationService.donate(donationCreateRequest, streamerId);
        return new ResponseEntity<>(donate, HttpStatus.CREATED);
    }

    @GetMapping("/{streamerId}")
    @Operation(
            summary = "Streamer uchun donatsiyalarni olish",
            description = "Ushbu metod berilgan streamer uchun donatsiyalar ro'yxatini sahifalab qaytaradi.",
            parameters = {
                    @Parameter(name = "streamerId", description = "Streamer identifikatori", required = true),
                    @Parameter(name = "page", description = "Sahifa raqami", required = true),
                    @Parameter(name = "size", description = "Sahifada ko'rsatiladigan elementlar soni", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Streamer uchun donatsiyalar", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Streamer topilmasa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    public PagedResponse<DonationInfo> getDonationsOfStreamer(
            @PathVariable Long streamerId,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return new PagedResponse<>(donationService.getDonationsOfStreamer(streamerId, page, size));
    }

    @GetMapping
    @Operation(
            summary = "Barcha donatsiyalarni olish",
            description = "Ushbu metod barcha donatsiyalarni sahifalab qaytaradi.",
            parameters = {
                    @Parameter(name = "page", description = "Sahifa raqami", required = true),
                    @Parameter(name = "size", description = "Sahifada ko'rsatiladigan elementlar soni", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Barcha donatsiyalar", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class)))
            }
    )
    public PagedResponse<DonationInfo> getAllDonations(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return new PagedResponse<>(donationService.getAllDonations(page, size));
    }

    @GetMapping("/statistics")
    @Operation(
            summary = "Ma'mur uchun statistikalarni olish",
            description = "Ushbu metod ma'mur uchun umumiy statistikalar ro'yxatini qaytaradi.",
            parameters = {
                    @Parameter(name = "days", description = "So'rov qilinayotgan statistikalar uchun kunlar soni", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Umumiy statistikalar", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DonationStatisticResponse.class)))
            }
    )
    public List<DonationStatisticResponse> getAllStatistics(@RequestParam int days) {
        return donationService.getStatisticsForAdmin(days);
    }

    @GetMapping("/statistics/{streamerId}")
    @Operation(
            summary = "Streamer uchun statistikalarni olish",
            description = "Ushbu metod berilgan streamer uchun statistikalarni ro'yxatini qaytaradi.",
            parameters = {
                    @Parameter(name = "streamerId", description = "Streamer identifikatori", required = true),
                    @Parameter(name = "days", description = "So'rov qilinayotgan statistikalar uchun kunlar soni", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Streamer uchun statistikalar", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DonationStatisticResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Streamer topilmasa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    public List<DonationStatisticResponse> getStatisticsOfStreamer(
            @PathVariable Long streamerId,
            @RequestParam int days
    ) {
        return donationService.getStatisticsForStreamer(streamerId, days);
    }

    @PostMapping("/test/{streamerId}")
    @Operation(
            summary = "Test donatsiya yaratish",
            description = "Ushbu metod test maqsadlari uchun donatsiya yaratadi. Bu metod ishlab chiqish jarayonida foydalanish uchun mo'ljallangan.",
            parameters = {
                    @Parameter(name = "streamerId", description = "Streamer identifikatori", required = true),
                    @Parameter(name = "user", description = "Autentifikatsiya qilingan foydalanuvchi", required = true)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Donatsiya yaratish uchun test so'rov ma'lumotlari",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DonationCreateRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test donatsiya muvaffaqiyatli yaratildi"),
                    @ApiResponse(responseCode = "404", description = "Streamer topilmasa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    public void createTestDonation(
            @PathVariable Long streamerId,
            @AuthenticationPrincipal UserEntity user,
            @RequestBody DonationCreateRequest donationCreateRequest
    ) {
        donationService.testDonate(donationCreateRequest, streamerId, user);
    }
}