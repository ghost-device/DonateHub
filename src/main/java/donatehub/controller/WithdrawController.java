package donatehub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import donatehub.domain.enums.WithdrawStatus;
import donatehub.domain.model.PagedRes;
import donatehub.domain.projection.WithdrawInfo;
import donatehub.service.withdraw.WithdrawService;

/**
 * WithdrawController - Pul chiqarish (withdraw) ma'lumotlarini boshqarish uchun REST API.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/withdraw")
@Tag(name = "Withdraw")
public class WithdrawController {
    private final WithdrawService withdrawService;
    private Logger log = LoggerFactory.getLogger("CUSTOM_LOGGER");;

    @Operation(
            summary = "Foydalanuvchilar uchun pul chiqarish so'rovini yaratadi",
            description = "Ushbu metod berilgan streamer identifikatori asosida pul chiqarish so'rovini yaratadi.",
            parameters = {
                    @Parameter(name = "streamerId", description = "Streamer identifikatori", required = true),
                    @Parameter(name = "amount", description = "Chiqariladigan summa", required = true),
                    @Parameter(name = "cardNumber", description = "Kredit kartasi raqami", required = true)
            }
    )
    @PostMapping("/{streamerId}")
    public void create(@PathVariable Long streamerId,
                       @RequestParam Float amount,
                       @RequestParam String cardNumber){
        withdrawService.create(streamerId, amount, cardNumber);
    }

    @Operation(
            summary = "Admin uchun withdraw statusini complete'ga o'zgartiradi",
            description = "Ushbu metod withdraw so'rovining statusini yangilaydi.",
            parameters = {
                    @Parameter(name = "withdrawId", description = "Withdraw identifikatori", required = true),
            }
    )
    @PutMapping("/complete/{withdrawId}")
    public void setStatusToComplete(@PathVariable Long withdrawId){
        log.info("Complete uchun so'rov {}", withdrawId);
        withdrawService.setStatus(withdrawId, WithdrawStatus.COMPLETED);
    }

    @Operation(
            summary = "Admin uchun withdraw statusini cancel'ga o'zgartiradi",
            description = "Ushbu metod withdraw so'rovining statusini yangilaydi.",
            parameters = {
                    @Parameter(name = "withdrawId", description = "Withdraw identifikatori", required = true),
            }
    )
    @PutMapping("/cancel/{withdrawId}")
    public void setStatusCanceled(@PathVariable Long withdrawId){
        log.info("Cancel uchun so'rov {}", withdrawId);
        withdrawService.setStatus(withdrawId, WithdrawStatus.CANCELED);
    }

    @Operation(
            summary = "Admin uchun withdraw so'rovlarini statusiga qarab qaytaradi",
            description = "Ushbu metod berilgan parametrlar asosida withdraw so'rovlarini qaytaradi.",
            parameters = {
                    @Parameter(name = "page", description = "Sahifa raqami", required = true),
                    @Parameter(name = "size", description = "Sahifa o'lchami", required = true),
                    @Parameter(name = "status", description = "Withdraw statusi", required = true)
            }
    )
    @GetMapping
    public PagedRes<WithdrawInfo> getWithdrawsByStatus(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam WithdrawStatus status){
        return new PagedRes<>(withdrawService.getWithdrawsByStatus(page, size, status));
    }

    @Operation(
            summary = "Streamer uchun withdraw so'rovlarini statusiga qarab qaytaradi",
            description = "Ushbu metod berilgan streamer identifikatori asosida withdraw so'rovlarini qaytaradi.",
            parameters = {
                    @Parameter(name = "streamerId", description = "Streamer identifikatori", required = true),
                    @Parameter(name = "page", description = "Sahifa raqami", required = true),
                    @Parameter(name = "size", description = "Sahifa o'lchami", required = true),
                    @Parameter(name = "status", description = "Withdraw statusi", required = true)
            }
    )
    @GetMapping("/{streamerId}")
    public PagedRes<WithdrawInfo> getWithdrawsOfStreamerByStatus(
            @PathVariable Long streamerId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam WithdrawStatus status){
        return new PagedRes<>(withdrawService.getWithdrawsOfStreamerByStatus(streamerId, page, size, status));
    }
}