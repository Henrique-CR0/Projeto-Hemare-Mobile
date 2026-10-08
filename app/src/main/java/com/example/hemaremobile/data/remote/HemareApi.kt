package com.example.hemaremobile.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Rotas da API REST do Hemare (backend Node/Express do site — repositório Henrique-CR0/Hemare).
 * Rotas protegidas recebem o cabeçalho "Authorization: Bearer <token JWT>".
 */
interface HemareApi {
    @POST("auth/login")
    suspend fun login(@Body corpo: LoginRequest): LoginResponse

    @POST("auth/cadastro")
    suspend fun cadastrar(@Body corpo: CadastroRequest): CadastroResponse

    @GET("locais")
    suspend fun listarLocais(@Query("cidade") cidade: String? = null): List<LocalDto>

    @POST("hospital/perfil")
    suspend fun salvarPerfilHospital(@Header("Authorization") token: String, @Body corpo: PerfilHospitalRequest): MensagemResponse

    @GET("hospital/estoque")
    suspend fun estoque(@Header("Authorization") token: String): List<EstoqueDto>

    @POST("hospital/estoque")
    suspend fun salvarEstoque(@Header("Authorization") token: String, @Body corpo: EstoqueDto): MensagemResponse

    @POST("hospital/necessidade")
    suspend fun publicarNecessidade(@Header("Authorization") token: String, @Body corpo: NecessidadeRequest): MensagemResponse

    @GET("hospital/necessidades")
    suspend fun necessidades(@Header("Authorization") token: String): List<NecessidadeDto>

    @GET("hospital/match/{id}")
    suspend fun match(@Header("Authorization") token: String, @Path("id") necessidadeId: Int): MatchResponse

    @POST("hospital/confirmar-doacao")
    suspend fun confirmarDoacao(@Header("Authorization") token: String, @Body corpo: ConfirmarDoacaoRequest): MensagemResponse
}

data class LoginRequest(val email: String, val senha: String)
data class LoginResponse(val token: String, val usuario: UsuarioDto)
data class CadastroRequest(val nome: String, val email: String, val senha: String, val tipo: String)
data class CadastroResponse(val mensagem: String?, val usuario: UsuarioDto?)
data class UsuarioDto(val id: Int, val nome: String, val email: String, val tipo: String)
data class MensagemResponse(val mensagem: String?)

data class LocalDto(
    val id: Int,
    val nome: String,
    val cidade: String,
    val estado: String,
    val endereco: String?,
    val telefone: String?
)

data class EstoqueDto(
    @SerializedName(value = "tipoSanguineo", alternate = ["tipo_sanguineo"]) val tipoSanguineo: String,
    val nivel: String
)

data class NecessidadeRequest(val tipoSanguineo: String, val urgencia: String, val tipoDoacao: String = "sangue")

data class NecessidadeDto(
    val id: Int,
    @SerializedName("tipo_sanguineo") val tipoSanguineo: String,
    val urgencia: String?
)

data class MatchResponse(val tipoReceptor: String?, val tiposCompativeis: List<String>, val doadores: List<DoadorDto>)

data class DoadorDto(
    @SerializedName("doador_id") val doadorId: Int?,
    val nome: String,
    @SerializedName("tipo_sanguineo") val tipoSanguineo: String,
    val cidade: String?,
    val telefone: String?,
    val identificado: Boolean
)

data class ConfirmarDoacaoRequest(val doadorId: Int)

data class PerfilHospitalRequest(
    val cnpj: String,
    val cnes: String,
    val cep: String,
    val endereco: String,
    val numero: String,
    val bairro: String,
    val complemento: String,
    val cidade: String,
    val estado: String
)

object ClienteApi {
    /** Timeouts curtos: sem servidor, o app cai rápido para o banco local. */
    fun criar(baseUrl: String, logs: Boolean = false): HemareApi {
        val http = OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .apply {
                if (logs) addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HemareApi::class.java)
    }
}

/** Token JWT da sessão atual (null = sessão local/offline). */
class Sessao {
    @Volatile var token: String? = null
    val cabecalho: String? get() = token?.let { "Bearer $it" }
}
