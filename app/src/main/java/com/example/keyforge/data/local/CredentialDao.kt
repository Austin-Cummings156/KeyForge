package com.example.keyforge.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.keyforge.data.model.CredentialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialDao {

    @Query("SELECT * FROM credentials ORDER BY updatedAt DESC")
    fun getAllCredentials(): Flow<List<CredentialEntity>>

    @Query("SELECT * FROM credentials WHERE id = :id LIMIT 1")
    suspend fun getCredentialById(id: Int): CredentialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: CredentialEntity)

    @Update
    suspend fun updateCredential(credential: CredentialEntity)

    @Query("DELETE FROM credentials WHERE id = :id")
    suspend fun deleteCredentialById(id: Int)
}