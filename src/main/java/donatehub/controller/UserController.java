package donatehub.controller;

import donatehub.domain.entity.UserEntity;
import donatehub.domain.response.UserProfileRes;
import donatehub.domain.response.UserStatisticRes;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import donatehub.domain.projection.UserInfoForDonate;
import donatehub.domain.model.PagedRes;
import donatehub.domain.projection.UserInfo;
import donatehub.domain.request.UserUpdateReq;
import donatehub.domain.model.ExceptionRes;
import donatehub.service.user.UserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * UserController - Foydalanuvchilarni boshqarish va foydalanuvchi ma'lumotlarini olish uchun REST API.
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Foydalanuvchi ma'lumotlarini olish",
            description = "Berilgan foydalanuvchi identifikatori asosida foydalanuvchi ma'lumotlarini qaytaradi.",
            parameters = @Parameter(name = "userId", description = "Foydalanuvchi identifikatori", required = true),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Streamer topilsa data qaytariladi", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserInfo.class))),
                    @ApiResponse(responseCode = "404", description = "Noto'g'ri so'rov ma'lumotlari, Streamer topilmasa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionRes.class)))
            }
    )
    @GetMapping("/user-info/{userId}")
    public UserInfo getUserInfo(@PathVariable Long userId){
        return userService.getById(userId);
    }

    @Hidden
    @GetMapping("/s/{api}")
    public ModelAndView showDonationPage(@PathVariable UUID api) {
        ModelAndView modelAndView = new ModelAndView("donation");
        modelAndView.addObject("api", api);
        return modelAndView;
    }

    @Operation(
            summary = "Kanal nomi asosida streamer ma'lumotlarini olish",
            description = "Berilgan kanal nomi asosida streamer ma'lumotlarini qaytaradi.",
            parameters = @Parameter(name = "channelName", description = "Kanal nomi", required = true),
            responses = {
                    @ApiResponse(responseCode = "404", description = "Noto'g'ri so'rov ma'lumotlari, Streamer topilmasa", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExceptionRes.class)))
            }
    )
    @GetMapping("/{channelName}")
    public UserInfoForDonate getStreamerByChannelName(@PathVariable String channelName){
        return userService.findByChannelName(channelName);
    }

    @Operation(
            summary = "Tasdiqlangan foydalanuvchilarni olish",
            description = "Sahifa raqami va sahifada ko'rsatiladigan elementlar soniga asoslanib tasdiqlangan foydalanuvchilarni qaytaradi.",
            parameters = {
                    @Parameter(name = "page", description = "Sahifa raqami", required = true),
                    @Parameter(name = "size", description = "Sahifada ko'rsatiladigan elementlar soni", required = true)
            }
    )
    @GetMapping("/verified")
    public PagedRes<UserInfo> getApprovedUsers(@RequestParam int page, @RequestParam int size){
        return new PagedRes<>(userService.getUsersByEnableState(true, page, size));
    }

    @Operation(
            summary = "Tasdiqlanmagan foydalanuvchilarni olish",
            description = "Sahifa raqami va sahifada ko'rsatiladigan elementlar soniga asoslanib tasdiqlanmagan foydalanuvchilarni qaytaradi.",
            parameters = {
                    @Parameter(name = "page", description = "Sahifa raqami", required = true),
                    @Parameter(name = "size", description = "Sahifada ko'rsatiladigan elementlar soni", required = true)
            }
    )
    @GetMapping("/not-verified")
    public PagedRes<UserInfo> getNotApproved(@RequestParam int page, @RequestParam int size){
        return new PagedRes<>(userService.getUsersByEnableState(false, page, size));
    }

    @Operation(
            summary = "Foydalanuvchilarni qidirish",
            description = "Berilgan matn asosida foydalanuvchilarni qidiradi.",
            parameters = {
                    @Parameter(name = "text", description = "Qidiruv matni", required = true),
                    @Parameter(name = "page", description = "Sahifa raqami", required = true),
                    @Parameter(name = "size", description = "Sahifada ko'rsatiladigan elementlar soni", required = true),
                    @Parameter(name = "enable", description = "Status", required = true),
            }
    )
    @GetMapping("/search")
    public PagedRes<UserInfo> searchEnables(@RequestParam String text, @RequestParam boolean enable, @RequestParam int page, @RequestParam int size){
        return new PagedRes<>(userService.searchUsers(text, enable, page, size));
    }

    @Operation(
            summary = "Foydalanuvchi ma'lumotlarini yangilash",
            description = "Foydalanuvchi ma'lumotlarini yangilaydi va profil va banner rasmlarini yuklaydi.",
            parameters = @Parameter(name = "userId", description = "Foydalanuvchi identifikatori", required = true),
            requestBody = @RequestBody(
                    description = "Foydalanuvchi yangilash so'rovi ma'lumotlari",
                    content = @Content(mediaType = "multipart/form-data")
            )
    )
    @PutMapping("/{userId}")
    public void update(@PathVariable Long userId,
                       @RequestPart(value = "update", required = false) UserUpdateReq updateReq,
                       @RequestPart("profileImg") MultipartFile profileImg,
                       @RequestPart("bannerImg") MultipartFile bannerImg) {
        System.out.println(updateReq.getChannelName());
        userService.update(userId, updateReq, profileImg, bannerImg);
    }

    @Operation(
            summary = "Streamerni faollashtirish",
            description = "Berilgan streamer identifikatori asosida streamer statusini faollashtiradi.",
            parameters = @Parameter(name = "streamerId", description = "Streamer identifikatori", required = true)
    )
    @PutMapping("/enable/{streamerId}")
    public void enable(@PathVariable @NotNull(message = "User id null bo'lishi mumkin emas") Long streamerId){
        userService.enable(streamerId);
    }

    @Operation(
            summary = "Streamerni o'chirish",
            description = "Berilgan streamer identifikatori asosida streamer statusini o'chiradi.",
            parameters = @Parameter(name = "streamerId", description = "Streamer identifikatori", required = true)
    )
    @PutMapping("/disable/{streamerId}")
    public void disable(@PathVariable @NotNull(message = "User id null bo'lishi mumkin emas") Long streamerId){
        userService.disable(streamerId);
    }

    @PutMapping("/offline/{streamerId}")
    public void offline(@PathVariable Long streamerId){
        userService.offline(streamerId);
    }

    @PutMapping("/online/{streamerId}")
    public void online(@PathVariable Long streamerId){
        userService.online(streamerId);
    }

    @GetMapping("/statistic/register")
    public List<UserStatisticRes> getStatisticsOfRegister(@RequestParam int days){
        return userService.getStatisticsOfRegister(days);
    }

    @GetMapping("/statistic/last-online")
    public List<UserStatisticRes> getStatisticOfLastOnline(int days){
        return userService.getStatisticOfLastOnline(days);
    }

    @Operation(
            summary = "Streamerni to'liq register qilish"
    )
    @PutMapping("/register/{streamerId}")
    public void fullRegister(@PathVariable Long streamerId,
                             @RequestPart UserUpdateReq updateReq,
                             @RequestPart("profileImg") MultipartFile profileImg,
                             @RequestPart("bannerImg") MultipartFile bannerImg){
        userService.fullRegister(streamerId, updateReq, profileImg, bannerImg);
    }

    @Operation(
            summary = "User haqidagi ma'lumotlarni olish"
    )
    @GetMapping("/me")
    public UserProfileRes getMe(@AuthenticationPrincipal UserEntity user){
        return new UserProfileRes(user);
    }
}
