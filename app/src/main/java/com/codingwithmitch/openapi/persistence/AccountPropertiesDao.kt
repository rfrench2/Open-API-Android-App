package com.codingwithmitch.openapi.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codingwithmitch.openapi.models.AccountProperties
import com.codingwithmitch.openapi.models.AuthToken

@Dao
interface AccountPropertiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(accountProperties: AccountProperties): Long

    @Query("DELETE FROM account_properties")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM account_properties WHERE email = :email")
    suspend fun searchByEmail(email: String): AccountProperties

    @Query("SELECT * FROM account_properties")
    suspend fun selectAll(): List<AccountProperties>
}















