package kr.co.fitview.api.app

import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.fitview.api.app.domain.address.controller.AddressController
import kr.co.fitview.api.app.domain.address.service.AddressService
import kr.co.fitview.api.app.domain.app_feedback.controller.AdminAppFeedbackController
import kr.co.fitview.api.app.domain.app_feedback.controller.AppFeedbackController
import kr.co.fitview.api.app.domain.app_feedback.service.AppFeedbackQueryService
import kr.co.fitview.api.app.domain.app_feedback.service.AppFeedbackService
import kr.co.fitview.api.app.domain.app_feedback.service.RecommendationAppFeedbackDismissLogService
import kr.co.fitview.api.app.domain.auth.controller.AuthController
import kr.co.fitview.api.app.domain.auth.service.AuthService
import kr.co.fitview.api.app.domain.auth.service.RefreshTokenService
import kr.co.fitview.api.app.domain.auth.service.TestAuthService
import kr.co.fitview.api.app.domain.banner.controller.BannerController
import kr.co.fitview.api.app.domain.banner.service.BannerDismissLogService
import kr.co.fitview.api.app.domain.banner.service.BannerQueryService
import kr.co.fitview.api.app.domain.banner.service.BannerService
import kr.co.fitview.api.app.domain.chat.controller.ChatController
import kr.co.fitview.api.app.domain.chat.service.*
import kr.co.fitview.api.app.domain.dashboard.controller.AdminDashboardController
import kr.co.fitview.api.app.domain.dashboard.service.DashboardQueryService
import kr.co.fitview.api.app.domain.favorite.controller.FavoriteController
import kr.co.fitview.api.app.domain.favorite.service.FavoriteQueryService
import kr.co.fitview.api.app.domain.favorite.service.FavoriteService
import kr.co.fitview.api.app.domain.fcm.controller.FcmController
import kr.co.fitview.api.app.domain.fcm.service.FcmTokenService
import kr.co.fitview.api.app.domain.image.controller.ImageController
import kr.co.fitview.api.app.domain.image.service.S3Service
import kr.co.fitview.api.app.domain.invitation.service.InvitationService
import kr.co.fitview.api.app.domain.member.controller.AdminMemberController
import kr.co.fitview.api.app.domain.member.controller.MemberController
import kr.co.fitview.api.app.domain.member.service.MemberQueryService
import kr.co.fitview.api.app.domain.member.service.MemberService
import kr.co.fitview.api.app.domain.member.service.MemberWithdrawReasonQueryService
import kr.co.fitview.api.app.domain.member.service.MemberWithdrawReasonService
import kr.co.fitview.api.app.domain.notification.controller.NotificationController
import kr.co.fitview.api.app.domain.notification.service.NotificationQueryService
import kr.co.fitview.api.app.domain.notification.service.NotificationService
import kr.co.fitview.api.app.global.stomp.service.StompPublishService
import kr.co.fitview.api.app.domain.oauth2.controller.OAuth2Controller
import kr.co.fitview.api.app.domain.oauth2.service.OAuth2Service
import kr.co.fitview.api.app.domain.report.controller.AdminReportController
import kr.co.fitview.api.app.domain.report.controller.ReportController
import kr.co.fitview.api.app.domain.report.service.ChatRoomReportService
import kr.co.fitview.api.app.domain.report.service.ReportQueryService
import kr.co.fitview.api.app.domain.report.service.ReportReasonQueryService
import kr.co.fitview.api.app.domain.review.controller.AdminReviewController
import kr.co.fitview.api.app.domain.review.controller.ReviewController
import kr.co.fitview.api.app.domain.review.service.ReviewQueryService
import kr.co.fitview.api.app.domain.review.service.ReviewService
import kr.co.fitview.api.app.domain.review.service.ReviewTagCountQueryService
import kr.co.fitview.api.app.domain.workout.controller.AdminWorkoutController
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestQueryService
import kr.co.fitview.api.app.domain.workout.service.WorkoutRequestService
import kr.co.fitview.api.app.domain.workout_history.controller.WorkoutHistoryController
import kr.co.fitview.api.app.domain.workout_history.service.WorkoutHistoryQueryService
import kr.co.fitview.api.app.domain.workout_partner.controller.AdminWorkoutPartnerController
import kr.co.fitview.api.app.domain.workout_partner.controller.WorkoutPartnerController
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestQueryService
import kr.co.fitview.api.app.domain.workout_partner.service.WorkoutPartnerRequestService
import kr.co.fitview.api.app.domain.workout_reward.controller.AdminWorkoutRewardController
import kr.co.fitview.api.app.domain.workout_reward.controller.WorkoutRewardController
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardQueryService
import kr.co.fitview.api.app.domain.workout_reward.service.WorkoutRewardService
import kr.co.fitview.api.app.global.config.JacksonConfig
import kr.co.fitview.api.app.global.config.SecurityConfig
import kr.co.fitview.api.app.global.config.TestSecurityConfig
import kr.co.fitview.api.app.global.exception.GlobalExceptionHandler
import kr.co.fitview.api.app.global.filter.JwtAuthenticationFilter
import kr.co.fitview.api.app.global.health.HealthController
import kr.co.fitview.api.app.global.security.JwtAuthenticationEntryPoint
import kr.co.fitview.api.app.global.sqids.service.SqidsService
import kr.co.fitview.api.app.global.util.SecurityUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc


@Import(
    TestSecurityConfig::class,
    JacksonConfig::class
//    SecurityUtil::class,
//    TestWebConfig::class,
//    RequestEnumConverter::class,
//    WebConverterConfig::class
)
@WebMvcTest(controllers = [
    AuthController::class,
    MemberController::class,
    OAuth2Controller::class,
    ImageController::class,
    AddressController::class,
    WorkoutPartnerController::class,
    ChatController::class,
    FcmController::class,
    ReviewController::class,
    WorkoutHistoryController::class,
    NotificationController::class,
    ReportController::class,
    FavoriteController::class,
    AppFeedbackController::class,
    BannerController::class,
    WorkoutRewardController::class,

    AdminDashboardController::class,
    AdminMemberController::class,
    AdminWorkoutPartnerController::class,
    AdminWorkoutController::class,
    AdminReviewController::class,
    AdminReportController::class,
    AdminAppFeedbackController::class,
    AdminWorkoutRewardController::class,

    HealthController::class,
    GlobalExceptionHandler::class,
],
excludeFilters = [
    ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [SecurityConfig::class]
    ),
    ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [JwtAuthenticationFilter::class]
    ),
    ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = [JwtAuthenticationEntryPoint::class]
    )
])
abstract class ControllerTestSupport {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @MockitoBean
    protected lateinit var authService: AuthService

    @MockitoBean
    protected lateinit var memberService: MemberService

    @MockitoBean
    protected lateinit var refreshTokenService: RefreshTokenService

    @MockitoBean
    protected lateinit var oAuth2Service: OAuth2Service

    @MockitoBean
    protected lateinit var addressService: AddressService

    @MockitoBean
    protected lateinit var workoutPartnerRequestService: WorkoutPartnerRequestService

    @MockitoBean
    protected lateinit var workoutPartnerRequestQueryService: WorkoutPartnerRequestQueryService

    @MockitoBean
    protected lateinit var testAuthService: TestAuthService

    @MockitoBean
    protected lateinit var s3Service: S3Service

    @MockitoBean
    protected lateinit var securityUtil: SecurityUtil

    @MockitoBean
    protected lateinit var chatService: ChatService

    @MockitoBean
    protected lateinit var chatRoomService: ChatRoomService

    @MockitoBean
    protected lateinit var chatParticipantService: ChatParticipantService

    @MockitoBean
    protected lateinit var chatMessageService: ChatMessageService

    @MockitoBean
    protected lateinit var chatMessageQueryService: ChatMessageQueryService

    @MockitoBean
    protected lateinit var workoutRequestService: WorkoutRequestService

    @MockitoBean
    protected lateinit var workoutRequestQueryService: WorkoutRequestQueryService

    @MockitoBean
    protected lateinit var messageReadStatusService: MessageReadStatusService

    @MockitoBean
    protected lateinit var stompPublishService: StompPublishService

    @MockitoBean
    protected lateinit var fcmTokenService: FcmTokenService

    @MockitoBean
    protected lateinit var memberQueryService: MemberQueryService

    @MockitoBean
    protected lateinit var reviewService: ReviewService

    @MockitoBean
    protected lateinit var workoutHistoryQueryService: WorkoutHistoryQueryService

    @MockitoBean
    protected lateinit var reviewTagCountQueryService: ReviewTagCountQueryService

    @MockitoBean
    protected lateinit var reviewQueryService: ReviewQueryService

    @MockitoBean
    protected lateinit var notificationQueryService : NotificationQueryService

    @MockitoBean
    protected lateinit var notificationService : NotificationService

    @MockitoBean
    protected lateinit var reportReasonQueryService : ReportReasonQueryService

    @MockitoBean
    protected lateinit var chatRoomReportService : ChatRoomReportService

    @MockitoBean
    protected lateinit var dashboardQueryService: DashboardQueryService

    @MockitoBean
    protected lateinit var reportQueryService: ReportQueryService

    @MockitoBean
    protected lateinit var memberWithdrawReasonQueryService: MemberWithdrawReasonQueryService

    @MockitoBean
    protected lateinit var memberWithdrawReasonService : MemberWithdrawReasonService

    @MockitoBean
    protected lateinit var favoriteService : FavoriteService

    @MockitoBean
    protected lateinit var favoriteQueryService : FavoriteQueryService

    @MockitoBean
    protected lateinit var bannerQueryService : BannerQueryService

    @MockitoBean
    protected lateinit var bannerService : BannerService

    @MockitoBean
    protected lateinit var appFeedbackService : AppFeedbackService

    @MockitoBean
    protected lateinit var appFeedbackQueryService : AppFeedbackQueryService

    @MockitoBean
    protected lateinit var bannerDismissLogService : BannerDismissLogService

    @MockitoBean
    protected lateinit var recommendationAppFeedbackDismissLogService : RecommendationAppFeedbackDismissLogService

    @MockitoBean
    protected lateinit var sqidsService: SqidsService

    @MockitoBean
    protected lateinit var invitationService: InvitationService

    @MockitoBean
    protected lateinit var workoutRewardService: WorkoutRewardService

    @MockitoBean
    protected lateinit var workoutRewardQueryService: WorkoutRewardQueryService

}