package com.example.hemaremobile.data

import com.example.hemaremobile.data.local.ContaDao
import com.example.hemaremobile.data.local.ContaEntity
import com.example.hemaremobile.data.local.DoacaoConfirmadaEntity
import com.example.hemaremobile.data.local.DoadorDao
import com.example.hemaremobile.data.local.DoadorEntity
import com.example.hemaremobile.data.local.EstoqueEntity
import com.example.hemaremobile.data.local.HashSenha
import com.example.hemaremobile.data.local.HemocentroDao
import com.example.hemaremobile.data.local.HemocentroEntity
import com.example.hemaremobile.data.local.HospitalDao
import com.example.hemaremobile.data.local.NecessidadeEntity
import com.example.hemaremobile.data.remote.CadastroRequest
import com.example.hemaremobile.data.remote.CadastroResponse
import com.example.hemaremobile.data.remote.ConfirmarDoacaoRequest
import com.example.hemaremobile.data.remote.DoadorDto
import com.example.hemaremobile.data.remote.EstoqueDto
import com.example.hemaremobile.data.remote.HemareApi
import com.example.hemaremobile.data.remote.LocalDto
import com.example.hemaremobile.data.remote.LoginRequest
import com.example.hemaremobile.data.remote.LoginResponse
import com.example.hemaremobile.data.remote.MatchResponse
import com.example.hemaremobile.data.remote.MensagemResponse
import com.example.hemaremobile.data.remote.NecessidadeDto
import com.example.hemaremobile.data.remote.NecessidadeRequest
import com.example.hemaremobile.data.remote.PerfilHospitalRequest
import com.example.hemaremobile.data.remote.Sessao
import com.example.hemaremobile.data.remote.UsuarioDto
import com.example.hemaremobile.data.repository.AutenticacaoRepositoryImpl
import com.example.hemaremobile.data.repository.HemocentroRepositoryImpl
import com.example.hemaremobile.data.repository.HospitalRepositoryImpl
import com.example.hemaremobile.domain.DoadorCompativel
import com.example.hemaremobile.domain.Necessidade
import com.example.hemaremobile.domain.PerfilHospital
import com.example.hemaremobile.domain.Resultado
import com.example.hemaremobile.domain.TipoConta
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Simula o backend do Hemare: no ar, fora do ar (IOException) ou respondendo com erro HTTP. */
private class FakeApi : HemareApi {
    var foraDoAr = false
    var codigoErro: Int? = null
    val usuarios = mutableMapOf("hospital@hemare.com" to Triple("Hospital Remoto", "hospital123", "hospital"))
    val locais = listOf(LocalDto(10, "HEMORIO", "Rio de Janeiro", "RJ", "R. Frei Caneca, 8", null))
    val estoqueEnviado = mutableListOf<EstoqueDto>()
    val necessidadesEnviadas = mutableListOf<NecessidadeRequest>()
    val doacoesConfirmadas = mutableListOf<Int>()
    val perfisEnviados = mutableListOf<PerfilHospitalRequest>()

    private fun conferir() {
        if (foraDoAr) throw IOException("sem conexão")
        codigoErro?.let { throw HttpException(Response.error<Any>(it, "{}".toResponseBody(null))) }
    }

    override suspend fun login(corpo: LoginRequest): LoginResponse {
        conferir()
        val u = usuarios[corpo.email]
        if (u == null || u.second != corpo.senha) throw HttpException(Response.error<Any>(401, "{}".toResponseBody(null)))
        return LoginResponse("token-123", UsuarioDto(1, u.first, corpo.email, u.third))
    }

    override suspend fun cadastrar(corpo: CadastroRequest): CadastroResponse {
        conferir()
        if (usuarios.containsKey(corpo.email)) throw HttpException(Response.error<Any>(409, "{}".toResponseBody(null)))
        usuarios[corpo.email] = Triple(corpo.nome, corpo.senha, corpo.tipo)
        return CadastroResponse("ok", UsuarioDto(2, corpo.nome, corpo.email, corpo.tipo))
    }

    override suspend fun listarLocais(cidade: String?): List<LocalDto> {
        conferir()
        return locais.filter { cidade == null || it.cidade.contains(cidade, true) }
    }

    override suspend fun salvarPerfilHospital(token: String, corpo: PerfilHospitalRequest): MensagemResponse {
        conferir(); perfisEnviados += corpo; return MensagemResponse("ok")
    }

    override suspend fun estoque(token: String): List<EstoqueDto> = emptyList()
    override suspend fun salvarEstoque(token: String, corpo: EstoqueDto): MensagemResponse {
        conferir(); estoqueEnviado += corpo; return MensagemResponse("ok")
    }

    override suspend fun publicarNecessidade(token: String, corpo: NecessidadeRequest): MensagemResponse {
        conferir(); necessidadesEnviadas += corpo; return MensagemResponse("ok")
    }

    override suspend fun necessidades(token: String): List<NecessidadeDto> =
        necessidadesEnviadas.reversed().mapIndexed { i, n -> NecessidadeDto(100 + i, n.tipoSanguineo, n.urgencia) }

    override suspend fun match(token: String, necessidadeId: Int): MatchResponse {
        conferir()
        return MatchResponse("A+", listOf("O-", "O+", "A-", "A+"), listOf(
            DoadorDto(55, "Doadora Remota", "O-", "Recife", "(81) 9999-0000", true),
            DoadorDto(null, "Doador anônimo", "A+", "Recife", null, false)
        ))
    }

    override suspend fun confirmarDoacao(token: String, corpo: ConfirmarDoacaoRequest): MensagemResponse {
        conferir(); doacoesConfirmadas += corpo.doadorId; return MensagemResponse("ok")
    }
}

private class FakeContaDao : ContaDao {
    val contas = mutableMapOf<String, ContaEntity>()
    override suspend fun buscar(email: String) = contas[email]
    override suspend fun salvar(conta: ContaEntity) { contas[conta.email] = conta }
}

private class FakeHemocentroDao(iniciais: List<HemocentroEntity>) : HemocentroDao {
    val salvos = iniciais.toMutableList()
    override suspend fun listar() = salvos.sortedWith(compareBy({ it.estado }, { it.nome }))
    override suspend fun buscar(termo: String): List<HemocentroEntity> {
        val t = termo.trim('%')
        return listar().filter { it.nome.contains(t, true) || it.cidade.contains(t, true) || it.estado.contains(t, true) }
    }
    override suspend fun salvarTodos(hemocentros: List<HemocentroEntity>) {
        hemocentros.forEach { novo -> salvos.removeAll { it.id == novo.id }; salvos += novo }
    }
}

private class FakeHospitalDao : HospitalDao {
    val estoque = MutableStateFlow<List<EstoqueEntity>>(emptyList())
    val necessidades = MutableStateFlow<List<NecessidadeEntity>>(emptyList())
    val confirmadas = MutableStateFlow<List<DoacaoConfirmadaEntity>>(emptyList())

    override fun observarEstoque(hospital: String): Flow<List<EstoqueEntity>> = estoque.map { l -> l.filter { it.hospitalEmail == hospital } }
    override suspend fun salvarEstoque(estoque: EstoqueEntity) {
        this.estoque.value = this.estoque.value.filterNot { it.hospitalEmail == estoque.hospitalEmail && it.tipoSanguineo == estoque.tipoSanguineo } + estoque
    }
    override fun observarNecessidades(hospital: String): Flow<List<NecessidadeEntity>> = necessidades.map { l -> l.filter { it.hospitalEmail == hospital } }
    override suspend fun inserirNecessidade(necessidade: NecessidadeEntity): Long {
        val id = necessidades.value.size + 1
        necessidades.value = necessidades.value + necessidade.copy(id = id)
        return id.toLong()
    }
    override fun observarDoacoes(hospital: String): Flow<List<DoacaoConfirmadaEntity>> = confirmadas.map { l -> l.filter { it.hospitalEmail == hospital } }
    override suspend fun confirmarDoacao(doacao: DoacaoConfirmadaEntity) { confirmadas.value = confirmadas.value + doacao }
}

private class FakeDoadorDao : DoadorDao {
    private val todos = listOf(
        DoadorEntity(1, "Ana Beatriz Souza", "O-", "São Paulo, SP", "(11) 91234-5678", true),
        DoadorEntity(3, "Fernanda Costa", "A+", "Campinas, SP", "(19) 99876-5432", true),
        DoadorEntity(5, "Mariana Oliveira", "B+", "Guarulhos, SP", "(11) 98765-4321", true)
    )
    override suspend fun porTipos(tipos: List<String>) = todos.filter { it.tipoSanguineo in tipos }
}

class AutenticacaoRepositoryImplTest {
    private val api = FakeApi()
    private val contas = FakeContaDao().apply {
        contas["doador@hemare.com"] = ContaEntity("doador@hemare.com", "Maria Doadora", HashSenha.gerar("doador@hemare.com", "doador123"), "DOADOR")
    }
    private val sessao = Sessao()
    private val repositorio = AutenticacaoRepositoryImpl(api, contas, sessao)

    @Test
    fun `login pela API guarda o token e a conta no aparelho`() = runTest {
        val r = repositorio.entrar(" Hospital@Hemare.com ", "hospital123")
        assertTrue(r is Resultado.Sucesso)
        assertEquals(TipoConta.HOSPITAL, (r as Resultado.Sucesso).valor.tipo)
        assertEquals("token-123", sessao.token)
        assertNotNull(contas.contas["hospital@hemare.com"])
    }

    @Test
    fun `sem servidor entra com a conta salva no aparelho`() = runTest {
        api.foraDoAr = true
        val r = repositorio.entrar("doador@hemare.com", "doador123")
        assertEquals("Maria Doadora", (r as Resultado.Sucesso).valor.nome)
        assertNull(sessao.token)
    }

    @Test
    fun `senha errada falha mesmo sem servidor`() = runTest {
        api.foraDoAr = true
        assertEquals(Resultado.Falha("❌ Email ou senha inválidos."), repositorio.entrar("doador@hemare.com", "errada"))
    }

    @Test
    fun `campos vazios nao chegam na API`() = runTest {
        assertEquals(Resultado.Falha("❌ Preencha email e senha."), repositorio.entrar("", ""))
    }

    @Test
    fun `cadastro com email ja existente no servidor e recusado`() = runTest {
        val r = repositorio.cadastrar("Outro", "hospital@hemare.com", "senha1234", TipoConta.HOSPITAL)
        assertEquals(Resultado.Falha("❌ Esse email já está cadastrado."), r)
    }

    @Test
    fun `cadastro sem servidor cria a conta so no aparelho e permite entrar depois`() = runTest {
        api.foraDoAr = true
        val r = repositorio.cadastrar("Joana", "joana@email.com", "senha1234", TipoConta.DOADOR)
        assertTrue(r is Resultado.Sucesso)
        assertTrue(repositorio.entrar("joana@email.com", "senha1234") is Resultado.Sucesso)
    }

    @Test
    fun `cadastro de hospital guarda o perfil no aparelho e envia ao servidor`() = runTest {
        val perfil = PerfilHospital("11222333000181", "1234567", "50000-000", "Rua A", "10", "Boa Vista", "", "Recife", "PE")
        val r = repositorio.cadastrar("Hospital Novo", "novo@hospital.com", "senha1234", TipoConta.HOSPITAL, perfil)
        val conta = (r as Resultado.Sucesso).valor
        assertEquals("Recife", conta.perfilHospital?.cidade)
        assertEquals("11222333000181", contas.contas["novo@hospital.com"]?.cnpj)
        assertEquals(listOf("11222333000181"), api.perfisEnviados.map { it.cnpj })
    }

    @Test
    fun `sair apaga o token`() = runTest {
        repositorio.entrar("hospital@hemare.com", "hospital123")
        repositorio.sair()
        assertNull(sessao.token)
    }

    @Test
    fun `hash da senha nao guarda a senha em texto`() {
        val hash = HashSenha.gerar("a@b.com", "segredo123")
        assertEquals(64, hash.length)
        assertTrue("segredo123" !in hash)
        assertEquals(hash, HashSenha.gerar(" A@B.com ", "segredo123"))
    }
}

class HemocentroRepositoryImplTest {
    private val api = FakeApi()
    private val dao = FakeHemocentroDao(listOf(HemocentroEntity(1, "Fundação Hemope", "Recife", "PE", "R. Joaquim Nabuco, 171", "(81) 3182-4600")))
    private val repositorio = HemocentroRepositoryImpl(api, dao)

    @Test
    fun `com servidor usa a API e atualiza o cache`() = runTest {
        val lista = repositorio.buscar("")
        assertEquals(listOf("HEMORIO"), lista.map { it.nome })
        assertTrue(dao.salvos.any { it.nome == "HEMORIO" })
    }

    @Test
    fun `sem servidor usa o banco do aparelho`() = runTest {
        api.foraDoAr = true
        assertEquals(listOf("Fundação Hemope"), repositorio.buscar("recife").map { it.nome })
        assertTrue(repositorio.buscar("manaus").isEmpty())
    }
}

class HospitalRepositoryImplTest {
    private val api = FakeApi()
    private val dao = FakeHospitalDao()
    private val sessao = Sessao()
    private val repositorio = HospitalRepositoryImpl(api, dao, FakeDoadorDao(), sessao, relogio = { 0L })
    private val hospital = "hospital@hemare.com"

    @Test
    fun `offline publica so no aparelho e o match usa o diretorio local`() = runTest {
        val r = repositorio.publicar(hospital, "A+", "alerta")
        assertEquals(Resultado.Sucesso("✅ Necessidade publicada com sucesso."), r)
        val necessidade = repositorio.observarNecessidades(hospital).first().single()
        assertNull(necessidade.idRemoto)
        assertEquals(listOf("Ana Beatriz Souza", "Fernanda Costa"), repositorio.doadoresCompativeis(necessidade).map { it.nome })
        assertTrue(api.necessidadesEnviadas.isEmpty())
    }

    @Test
    fun `com sessao envia ao servidor e o match vem da API`() = runTest {
        sessao.token = "token-123"
        repositorio.publicar(hospital, "A+", "critico")
        val necessidade = repositorio.observarNecessidades(hospital).first().single()
        assertEquals(100, necessidade.idRemoto)
        assertEquals(listOf(NecessidadeRequest("A+", "critico")), api.necessidadesEnviadas)

        val doadores = repositorio.doadoresCompativeis(necessidade)
        assertEquals(listOf("Doadora Remota", "Doador anônimo"), doadores.map { it.nome })

        repositorio.confirmarDoacao(hospital, doadores.first())
        assertEquals(listOf(55), api.doacoesConfirmadas)
        assertEquals(setOf(55), repositorio.observarConfirmados(hospital).first())
    }

    @Test
    fun `servidor fora do ar nao perde a necessidade`() = runTest {
        sessao.token = "token-123"
        api.foraDoAr = true
        val r = repositorio.publicar(hospital, "O-", "emergencia")
        assertEquals(Resultado.Sucesso("✅ Necessidade salva no aparelho (servidor indisponível)."), r)
        assertEquals(1, repositorio.observarNecessidades(hospital).first().size)
    }

    @Test
    fun `doacao confirmada entra no historico com nome e tipo`() = runTest {
        val ana = DoadorCompativel(1, "Ana Beatriz Souza", "O-", "São Paulo, SP", "(11) 91234-5678", true)
        repositorio.confirmarDoacao(hospital, ana)
        val historico = repositorio.observarHistorico(hospital).first()
        assertEquals(listOf("Ana Beatriz Souza"), historico.map { it.doadorNome })
        assertEquals("O-", historico.single().tipoSanguineo)
        assertTrue(historico.single().dataHora.contains("às"))
    }

    @Test
    fun `publicar sem tipo falha`() = runTest {
        assertEquals(Resultado.Falha("❌ Escolha o tipo sanguíneo."), repositorio.publicar(hospital, "", "alerta"))
    }

    @Test
    fun `estoque fica salvo e vai para a API quando ha sessao`() = runTest {
        sessao.token = "token-123"
        repositorio.definirEstoque(hospital, "O-", "critico")
        repositorio.definirEstoque(hospital, "O-", "alerta")
        assertEquals(mapOf("O-" to "alerta"), repositorio.observarEstoque(hospital).first())
        assertEquals(2, api.estoqueEnviado.size)
    }

    @Test
    fun `doador local nao e confirmado na API`() = runTest {
        sessao.token = "token-123"
        repositorio.confirmarDoacao(hospital, DoadorCompativel(1, "Ana Beatriz Souza", "O-", "São Paulo, SP", "(11) 91234-5678", true))
        assertTrue(api.doacoesConfirmadas.isEmpty())
        assertEquals(setOf(1), repositorio.observarConfirmados(hospital).first())
    }

    @Test
    fun `doador anonimo do servidor nao tem id publico`() = runTest {
        sessao.token = "token-123"
        repositorio.publicar(hospital, "A+", "alerta")
        val necessidade = repositorio.observarNecessidades(hospital).first().single()
        val anonimo = repositorio.doadoresCompativeis(necessidade).last()
        assertTrue(anonimo.id < 0)
        assertEquals(false, anonimo.identificado)
        assertEquals(Necessidade(1, "A+", "alerta", 100), necessidade)
    }
}
