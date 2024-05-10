package com.futo.platformplayer.api.media.structures
import com.futo.platformplayer.api.media.Serializer
import com.futo.platformplayer.serializers.OffsetDateTimeNullableSerializer
import com.futo.platformplayer.stores.v2.IStoreItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.OffsetDateTime

/**
 * `IStoreItem` created and deleted at a specific time
 *
 *  Delegates equality comparisons to the inner object.
 *  Bubbles `onDelete` to the inner `IStoreItem`
 *
 *  Add an item to the storage
 *  @sample TemporalItem.now("Hello Mom");
 *
 *  Remove an items from the storage
 *  @sample TemporalItem.undefined("Hello Mom");
 */
@kotlinx.serialization.Serializable
class TemporalItem<T: Any> private constructor(
    val inner: T,
    @kotlinx.serialization.Serializable(with = OffsetDateTimeNullableSerializer::class)
    val createdAt: OffsetDateTime?,
): IStoreItem {
    @kotlinx.serialization.Serializable(with = OffsetDateTimeNullableSerializer::class)
    var deletedAt: OffsetDateTime? = null;
    val isDeleted: Boolean get() { return deletedAt != null };
    val isUndefined: Boolean get() { return createdAt == null };

    fun toJson(): String {
        return Json.encodeToString(this);
    }
    fun fromJson(str: String): TemporalItem<T> {
        return Serializer.json.decodeFromString(str);
    }

    override fun onDelete() {
        if (isDeleted) {
            return;
        }
        if (inner is IStoreItem) {
            inner.onDelete()
        }
        deletedAt = OffsetDateTime.now()
    }

    override fun hashCode(): Int {
        return inner.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return inner == other
    }

    companion object {
        fun<T: Any> now(data: T): TemporalItem<T> {
            return TemporalItem(data, OffsetDateTime.now())
        }

        fun<T: Any> undefined(data: T) : TemporalItem<T> {
            return TemporalItem(data, null)
        }
    }
}