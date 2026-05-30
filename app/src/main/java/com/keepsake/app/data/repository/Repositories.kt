package com.keepsake.app.data.repository

import com.keepsake.app.data.local.dao.*
import com.keepsake.app.data.local.entity.*
import com.keepsake.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val roomDao: RoomDao,
    private val areaDao: AreaDao,
    private val itemDao: ItemDao,
    private val kvDao: KvDao
) {
    fun observeAll(): Flow<List<Room>> = roomDao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getAll(): List<Room> = roomDao.getAll().map { it.toDomain() }

    suspend fun getById(id: String): Room? = roomDao.getById(id)?.toDomain()

    suspend fun create(
        name: String,
        icon: String? = null,
        note: String? = null,
        deviceId: String = "android"
    ): Room {
        val now = System.currentTimeMillis()
        val entity = RoomEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            icon = icon,
            note = note,
            updatedAt = now,
            updatedBy = deviceId
        )
        roomDao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun rename(id: String, name: String) {
        roomDao.rename(id, name, System.currentTimeMillis())
    }

    suspend fun softDelete(id: String) {
        val now = System.currentTimeMillis()
        roomDao.softDelete(id, now)
        areaDao.softDeleteByRoom(id, now)
        itemDao.softDeleteByRoom(id, now)
    }

    suspend fun count(): Int = roomDao.count()

    private suspend fun RoomEntity.toDomain(): Room {
        val count = areaDao.countByRoom(id)
        return Room(
            id = id, name = name, icon = icon,
            photoIds = parseJsonArray(photoIds),
            note = note, updatedAt = updatedAt, updatedBy = updatedBy,
            areaCount = count
        )
    }
}

@Singleton
class AreaRepository @Inject constructor(
    private val areaDao: AreaDao,
    private val itemDao: ItemDao
) {
    fun observeByRoom(roomId: String): Flow<List<Area>> = areaDao.observeByRoom(roomId).map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getByRoom(roomId: String): List<Area> = areaDao.getByRoom(roomId).map { it.toDomain() }

    suspend fun getById(id: String): Area? = areaDao.getById(id)?.toDomain()

    suspend fun create(
        roomId: String,
        name: String,
        note: String? = null,
        deviceId: String = "android"
    ): Area {
        val now = System.currentTimeMillis()
        val entity = AreaEntity(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            name = name,
            note = note,
            updatedAt = now,
            updatedBy = deviceId
        )
        areaDao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun rename(id: String, name: String) {
        areaDao.rename(id, name, System.currentTimeMillis())
    }

    suspend fun softDelete(id: String) {
        val now = System.currentTimeMillis()
        areaDao.softDelete(id, now)
        itemDao.softDeleteByArea(id, now)
    }

    private suspend fun AreaEntity.toDomain(): Area {
        val count = itemDao.countByArea(id)
        return Area(
            id = id, roomId = roomId, name = name,
            photoIds = parseJsonArray(photoIds),
            note = note, updatedAt = updatedAt, itemCount = count
        )
    }
}

@Singleton
class ItemRepository @Inject constructor(
    private val itemDao: ItemDao,
    private val reminderRuleDao: ReminderRuleDao
) {
    fun observeByArea(areaId: String): Flow<List<Item>> = itemDao.observeByArea(areaId).map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getByArea(areaId: String): List<Item> = itemDao.getByArea(areaId).map { it.toDomain() }

    suspend fun getById(id: String): Item? = itemDao.getById(id)?.toDomain()

    suspend fun search(query: String): List<Item> = itemDao.search(query).map { it.toDomain() }

    suspend fun create(
        areaId: String,
        name: String,
        qty: Int = 0,
        unit: String? = null,
        tags: List<String> = emptyList(),
        notes: String? = null,
        expiresAt: Long? = null,
        source: String = "manual",
        confidence: Double? = null,
        deviceId: String = "android"
    ): Item {
        val now = System.currentTimeMillis()
        val entity = ItemEntity(
            id = UUID.randomUUID().toString(),
            areaId = areaId,
            name = name,
            qty = qty,
            unit = unit,
            tags = tags.joinToString(",") { "[$it]" },
            notes = notes,
            expiresAt = expiresAt,
            source = source,
            confidence = confidence,
            createdAt = now,
            updatedAt = now,
            updatedBy = deviceId
        )
        itemDao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun update(item: Item, deviceId: String = "android") {
        val now = System.currentTimeMillis()
        itemDao.upsert(
            ItemEntity(
                id = item.id,
                areaId = item.areaId,
                name = item.name,
                qty = item.qty,
                unit = item.unit,
                tags = item.tags.joinToString(",") { "\"$it\"" },
                photoIds = item.photoIds.joinToString(",") { "\"$it\"" },
                notes = item.notes,
                expiresAt = item.expiresAt,
                source = item.source,
                confidence = item.confidence,
                createdAt = item.createdAt,
                updatedAt = now,
                updatedBy = deviceId,
                version = 0
            )
        )
    }

    suspend fun qtyDelta(id: String, delta: Int, deviceId: String = "android") {
        itemDao.qtyDelta(id, delta, System.currentTimeMillis(), deviceId)
    }

    suspend fun softDelete(id: String) {
        val now = System.currentTimeMillis()
        itemDao.softDelete(id, now)
        reminderRuleDao.softDeleteByItem(id, now)
    }

    suspend fun renameInArea(id: String, name: String) {
        itemDao.rename(id, name, System.currentTimeMillis())
    }

    suspend fun count(): Int = itemDao.count()

    private fun ItemEntity.toDomain() = Item(
        id = id, areaId = areaId, name = name, qty = qty, unit = unit,
        tags = parseJsonArray(tags),
        photoIds = parseJsonArray(photoIds),
        expiresAt = expiresAt, source = source, confidence = confidence,
        notes = notes, createdAt = createdAt, updatedAt = updatedAt, updatedBy = updatedBy
    )
}

@Singleton
class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao
) {
    fun observeByParent(parentType: String, parentId: String): Flow<List<Photo>> =
        photoDao.observeByParent(parentType, parentId).map { list -> list.map { it.toDomain() } }

    suspend fun getByParent(parentType: String, parentId: String): List<Photo> =
        photoDao.getByParent(parentType, parentId).map { it.toDomain() }

    suspend fun create(
        parentType: String,
        parentId: String,
        blobUri: String,
        deviceId: String = "android"
    ): Photo {
        val now = System.currentTimeMillis()
        val entity = PhotoEntity(
            id = UUID.randomUUID().toString(),
            parentType = parentType,
            parentId = parentId,
            takenAt = now,
            blobUri = blobUri,
            updatedAt = now,
            updatedBy = deviceId
        )
        photoDao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun softDelete(id: String) {
        photoDao.softDelete(id, System.currentTimeMillis())
    }

    private fun PhotoEntity.toDomain() = Photo(
        id = id, parentType = parentType, parentId = parentId,
        takenAt = takenAt, blobUri = blobUri, recognitionStatus = recognitionStatus
    )
}

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderRuleDao: ReminderRuleDao,
    private val itemDao: ItemDao
) {
    fun observeAll(): Flow<List<ReminderRule>> =
        reminderRuleDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getByItem(itemId: String): List<ReminderRule> =
        reminderRuleDao.getByItem(itemId).map { it.toDomain() }

    suspend fun getAll(): List<ReminderRule> = reminderRuleDao.getAll().map { it.toDomain() }

    suspend fun create(
        itemId: String,
        kind: String,
        thresholdAt: Long? = null,
        thresholdQty: Int? = null,
        note: String? = null,
        deviceId: String = "android"
    ): ReminderRule {
        val now = System.currentTimeMillis()
        val entity = ReminderRuleEntity(
            id = UUID.randomUUID().toString(),
            itemId = itemId,
            kind = kind,
            thresholdAt = thresholdAt,
            thresholdQty = thresholdQty,
            note = note,
            updatedAt = now,
            updatedBy = deviceId
        )
        reminderRuleDao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun updateFired(id: String) {
        reminderRuleDao.updateFired(id, System.currentTimeMillis(), System.currentTimeMillis())
    }

    suspend fun softDelete(id: String) {
        reminderRuleDao.softDelete(id, System.currentTimeMillis())
    }

    private fun ReminderRuleEntity.toDomain() = ReminderRule(
        id = id, itemId = itemId, kind = kind,
        thresholdAt = thresholdAt, thresholdQty = thresholdQty,
        note = note, lastFiredAt = lastFiredAt
    )
}

fun parseJsonArray(str: String): List<String> {
    if (str.isBlank() || str == "[]") return emptyList()
    return try {
        str.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    } catch (e: Exception) {
        emptyList()
    }
}
