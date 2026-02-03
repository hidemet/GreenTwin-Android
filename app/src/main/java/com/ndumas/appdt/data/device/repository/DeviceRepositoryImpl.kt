package com.ndumas.appdt.data.device.repository

import android.graphics.Color
import com.ndumas.appdt.core.common.Result
import com.ndumas.appdt.data.device.mapper.toDeviceDetail
import com.ndumas.appdt.data.device.mapper.toDomain
import com.ndumas.appdt.data.device.remote.DeviceApiService
import com.ndumas.appdt.data.device.remote.dto.DeviceDto
import com.ndumas.appdt.data.entity.remote.EntityApiService
import com.ndumas.appdt.domain.device.model.Device
import com.ndumas.appdt.domain.device.model.DeviceDetail
import com.ndumas.appdt.domain.device.repository.DeviceRepository
import com.ndumas.appdt.domain.error.DataError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class DeviceRepositoryImpl
    @Inject
    constructor(
        private val deviceApi: DeviceApiService,
        private val entityApi: EntityApiService,
    ) : DeviceRepository {
        private val devicesCache = MutableStateFlow<List<DeviceDto>>(emptyList())

        override fun getDevices(): Flow<Result<List<Device>, DataError>> =
            devicesCache
                .map { dtoList ->

                    val domainList = dtoList.map { it.toDomain() }
                    Result.Success(domainList)
                }.onStart {
                    refreshDevices()
                }

        private suspend fun refreshDevices() {
            try {
                val remoteList = deviceApi.getDevices()
                devicesCache.value = remoteList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getDeviceDetail(deviceId: String): Flow<Result<DeviceDetail, DataError>> =
            devicesCache
                .map { list -> list.find { it.deviceId == deviceId } }
                .distinctUntilChanged()
                .transformLatest { cachedDevice ->
                    if (cachedDevice != null) {
                        val baseModel = cachedDevice.toDeviceDetail()
                        emit(Result.Success(baseModel))

                        if (baseModel is DeviceDetail.Light && cachedDevice.stateEntityId != null) {
                            val enriched = fetchEnrichedDetails(baseModel, cachedDevice.stateEntityId)

                            if (enriched != null) {
                                emit(Result.Success(enriched))
                            }
                        }
                    } else {
                        if (devicesCache.value.isNotEmpty()) {
                            emit(Result.Error(DataError.Auth.USER_NOT_FOUND))
                        }
                    }
                }

        private suspend fun fetchEnrichedDetails(
            baseModel: DeviceDetail.Light,
            entityId: String,
        ): DeviceDetail? =
            try {
                val detailedEntity = entityApi.getEntityDetails(entityId)
                val attrs = detailedEntity.attributes

                baseModel.copy(
                    brightness = attrs?.brightness?.let { (it.toFloat() / 255f * 100f).roundToInt() } ?: baseModel.brightness,
                    rgbColor = attrs?.rgbColor?.let { if (it.size >= 3) Color.rgb(it[0], it[1], it[2]) else null } ?: baseModel.rgbColor,
                    colorTemp = attrs?.colorTempKelvin ?: baseModel.colorTemp,
                    supportsColor = attrs?.rgbColor != null || baseModel.supportsColor,
                )
            } catch (e: Exception) {
                null
            }
    }
