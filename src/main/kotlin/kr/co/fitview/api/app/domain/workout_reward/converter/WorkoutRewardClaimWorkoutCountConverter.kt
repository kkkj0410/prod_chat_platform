package kr.co.fitview.api.app.domain.workout_reward.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kr.co.fitview.api.app.domain.workout_reward.entity.enums.WorkoutRewardClaimWorkoutCount

@Converter
class WorkoutRewardClaimWorkoutCountConverter : AttributeConverter<WorkoutRewardClaimWorkoutCount, Int> {
    
    override fun convertToDatabaseColumn(attribute: WorkoutRewardClaimWorkoutCount?): Int? {
        return attribute?.value
    }

    override fun convertToEntityAttribute(dbData: Int?): WorkoutRewardClaimWorkoutCount? {
        return dbData?.let { WorkoutRewardClaimWorkoutCount.from(it) }
    }
}