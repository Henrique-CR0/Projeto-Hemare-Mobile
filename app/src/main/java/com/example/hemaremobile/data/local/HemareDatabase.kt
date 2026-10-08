package com.example.hemaremobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/** Banco local (SQLite via Room): funciona sem internet e serve de cache da API do Hemare. */
@Database(
    entities = [
        ContaEntity::class,
        HemocentroEntity::class,
        EstoqueEntity::class,
        NecessidadeEntity::class,
        DoadorEntity::class,
        DoacaoConfirmadaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HemareDatabase : RoomDatabase() {
    abstract fun contaDao(): ContaDao
    abstract fun hemocentroDao(): HemocentroDao
    abstract fun hospitalDao(): HospitalDao
    abstract fun doadorDao(): DoadorDao

    companion object {
        @Volatile private var instancia: HemareDatabase? = null

        fun obter(context: Context): HemareDatabase = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(context.applicationContext, HemareDatabase::class.java, "hemare.db")
                .addCallback(DadosIniciais)
                .build()
                .also { instancia = it }
        }
    }
}

/**
 * Popula o banco na primeira abertura: hemocentros oficiais e o diretório de doadores do match.
 * Sem contas de exemplo: para entrar é preciso se cadastrar (como doador ou hospital).
 */
private object DadosIniciais : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        HEMOCENTROS_OFICIAIS.forEachIndexed { indice, h ->
            db.execSQL(
                "INSERT INTO hemocentros (id, nome, cidade, estado, endereco, telefone) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(indice + 1, h[0], h[1], h[2], h[3], h[4])
            )
        }
        for (d in DOADORES_EXEMPLO) {
            db.execSQL(
                "INSERT INTO doadores (id, nome, tipoSanguineo, cidade, telefone, identificado) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(d.id, d.nome, d.tipoSanguineo, d.cidade, d.telefone, if (d.identificado) 1 else 0)
            )
        }
    }
}

/** Mesma lista oficial do backend do site (backend/db/popular-locais.js). */
val HEMOCENTROS_OFICIAIS = listOf(
    listOf("Fundação Hemope", "Recife", "PE", "R. Joaquim Nabuco, 171 - Graças", "(81) 3182-4600"),
    listOf("Fundação Pró-Sangue - Hemocentro SP", "São Paulo", "SP", "Av. Dr. Enéas C. de Aguiar, 155 - Cerqueira César", "(11) 4573-7800"),
    listOf("HEMORIO", "Rio de Janeiro", "RJ", "R. Frei Caneca, 8 - Centro", "(21) 3916-8300"),
    listOf("Fundação Hemominas", "Belo Horizonte", "MG", "Alameda Ezequiel Dias, 321 - Santa Efigênia", "(31) 3768-7400"),
    listOf("HEMOAM", "Manaus", "AM", "Av. Constantino Nery, 4397 - Chapada", "(92) 3655-0100"),
    listOf("HEMOBA", "Salvador", "BA", "Ladeira do Hospital Geral, s/n - Brotas", "(71) 3116-5652"),
    listOf("HEMOCE", "Fortaleza", "CE", "Av. José Bastos, 3390 - Rodolfo Teófilo", "(85) 3101-2296"),
    listOf("Hemocentro de Brasília - FHB", "Brasília", "DF", "SMHN Quadra 03, Conj. A, Bl. 3 - Asa Norte", "(61) 3327-4413"),
    listOf("HEMORGS", "Porto Alegre", "RS", "Av. Bento Gonçalves, 3722 - Partenon", "(51) 3288-4069"),
    listOf("HEMEPAR", "Curitiba", "PR", "Trav. João Prosdocimo, 145 - Alto da XV", "(41) 3281-4000"),
    listOf("HEMOSC", "Florianópolis", "SC", "Av. Othon Gama D'Eça, 756 - Centro", "(48) 3251-9711"),
    listOf("HEMOAL", "Maceió", "AL", "Av. Jorge de Lima, 58 - Trapiche da Barra", "(82) 3315-2107"),
    listOf("HEMONORTE", "Natal", "RN", "Av. Alexandrino de Alencar, 1800 - Tirol", "(84) 3232-6767"),
    listOf("HEMOPI", "Teresina", "PI", "R. 1º de Maio, 235 - Centro", "(86) 3221-8319"),
    listOf("HEMOPA", "Belém", "PA", "Trav. Padre Eutíquio, 2109 - Batista Campos", "(91) 3110-6300"),
    listOf("HEMOSE", "Aracaju", "SE", "Av. Tancredo Neves, s/n - Capucho", "(79) 3225-8500"),
    listOf("HEMOMAR", "São Luís", "MA", "R. 5 de Janeiro, s/n - Jordoa", "(98) 3218-7100"),
    listOf("MT Hemocentro", "Cuiabá", "MT", "R. 13 de Junho, 1055 - Porto", "(65) 3623-0044"),
    listOf("HEMOSUL", "Campo Grande", "MS", "Av. Fernando Corrêa da Costa, 1304 - Centro", "(67) 3312-1500"),
    listOf("HEMOGO", "Goiânia", "GO", "Av. Anhanguera, 5195 - Setor Coimbra", "(62) 3201-4560")
)

private val DOADORES_EXEMPLO = listOf(
    DoadorEntity(1, "Ana Beatriz Souza", "O-", "São Paulo, SP", "(11) 91234-5678", identificado = true),
    DoadorEntity(2, "Carlos Eduardo Lima", "O+", "São Paulo, SP", "", identificado = false),
    DoadorEntity(3, "Fernanda Costa", "A+", "Campinas, SP", "(19) 99876-5432", identificado = true),
    DoadorEntity(4, "João Pedro Alves", "A-", "São Paulo, SP", "", identificado = false),
    DoadorEntity(5, "Mariana Oliveira", "B+", "Guarulhos, SP", "(11) 98765-4321", identificado = true),
    DoadorEntity(6, "Rafael Santos", "B-", "São Paulo, SP", "", identificado = false),
    DoadorEntity(7, "Beatriz Fernandes", "AB+", "São Paulo, SP", "(11) 97654-3210", identificado = true),
    DoadorEntity(8, "Lucas Martins", "AB-", "Osasco, SP", "", identificado = false)
)
