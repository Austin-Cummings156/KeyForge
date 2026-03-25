package com.example.keyforge.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.keyforge.data.model.Credential
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialDao {

    @Query("SELECT * FROM credentials ORDER BY siteName ASC")
    fun getAllCredentials(): Flow<List<Credential>>

    @Query("SELECT * FROM credentials WHERE id = :id")
    suspend fun getCredentialById(id: Int): Credential?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(credential: Credential)

    @Update
    suspend fun updateCredential(credential: Credential)

    @Delete
    suspend fun deleteCredential(credential: Credential)
}