package com.example.hemaremobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContaDao {
    @Query("SELECT * FROM contas WHERE email = :email LIMIT 1")
    suspend fun buscar(email: String): ContaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvar(conta: ContaEntity)
}

@Dao
interface HemocentroDao {
    @Query("SELECT * FROM hemocentros ORDER BY estado, nome")
    suspend fun listar(): List<HemocentroEntity>

    @Query("SELECT * FROM hemocentros WHERE nome LIKE :termo OR cidade LIKE :termo OR estado LIKE :termo ORDER BY estado, nome")
    suspend fun buscar(termo: String): List<HemocentroEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarTodos(hemocentros: List<HemocentroEntity>)
}

@Dao
interface HospitalDao {
    @Query("SELECT * FROM estoque WHERE hospitalEmail = :hospital")
    fun observarEstoque(hospital: String): Flow<List<EstoqueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarEstoque(estoque: EstoqueEntity)

    @Query("SELECT * FROM necessidades WHERE hospitalEmail = :hospital ORDER BY id")
    fun observarNecessidades(hospital: String): Flow<List<NecessidadeEntity>>

    @Insert
    suspend fun inserirNecessidade(necessidade: NecessidadeEntity): Long

    @Query("SELECT * FROM doacoes_confirmadas WHERE hospitalEmail = :hospital ORDER BY confirmadaEm")
    fun observarDoacoes(hospital: String): Flow<List<DoacaoConfirmadaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun confirmarDoacao(doacao: DoacaoConfirmadaEntity)
}

@Dao
interface DoadorDao {
    @Query("SELECT * FROM doadores WHERE tipoSanguineo IN (:tipos) ORDER BY id")
    suspend fun porTipos(tipos: List<String>): List<DoadorEntity>
}
