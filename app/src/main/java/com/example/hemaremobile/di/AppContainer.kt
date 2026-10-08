package com.example.hemaremobile.di

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hemaremobile.AutenticacaoViewModel
import com.example.hemaremobile.BuildConfig
import com.example.hemaremobile.ConfiguracaoViewModel
import com.example.hemaremobile.OndeDoarViewModel
import com.example.hemaremobile.PainelHospitalViewModel
import com.example.hemaremobile.data.local.HemareDatabase
import com.example.hemaremobile.data.remote.ClienteApi
import com.example.hemaremobile.data.remote.Sessao
import com.example.hemaremobile.data.repository.AutenticacaoRepository
import com.example.hemaremobile.data.repository.AutenticacaoRepositoryImpl
import com.example.hemaremobile.data.repository.HemocentroRepository
import com.example.hemaremobile.data.repository.HemocentroRepositoryImpl
import com.example.hemaremobile.data.repository.HospitalRepository
import com.example.hemaremobile.data.repository.HospitalRepositoryImpl
import com.example.hemaremobile.data.repository.PreferenciasRepository
import com.example.hemaremobile.data.repository.PreferenciasRepositoryImpl

private val Context.dataStorePreferencias by preferencesDataStore(name = "preferencias")

/** Injeção de dependências manual: cria uma vez cada repositório e entrega aos ViewModels. */
class AppContainer(context: Context) {
    private val banco = HemareDatabase.obter(context)
    private val api = ClienteApi.criar(BuildConfig.API_BASE_URL, logs = BuildConfig.DEBUG)
    private val sessao = Sessao()

    val autenticacaoRepository: AutenticacaoRepository = AutenticacaoRepositoryImpl(api, banco.contaDao(), sessao)
    val hemocentroRepository: HemocentroRepository = HemocentroRepositoryImpl(api, banco.hemocentroDao())
    val hospitalRepository: HospitalRepository =
        HospitalRepositoryImpl(api, banco.hospitalDao(), banco.doadorDao(), sessao)
    val preferenciasRepository: PreferenciasRepository =
        PreferenciasRepositoryImpl(context.applicationContext.dataStorePreferencias)

    val fabricaAutenticacao: ViewModelProvider.Factory = viewModelFactory {
        initializer { AutenticacaoViewModel(autenticacaoRepository) }
    }
    val fabricaConfiguracao: ViewModelProvider.Factory = viewModelFactory {
        initializer { ConfiguracaoViewModel(preferenciasRepository) }
    }
    val fabricaOndeDoar: ViewModelProvider.Factory = viewModelFactory {
        initializer { OndeDoarViewModel(hemocentroRepository) }
    }

    fun fabricaPainelHospital(emailHospital: String): ViewModelProvider.Factory = viewModelFactory {
        initializer { PainelHospitalViewModel(hospitalRepository, emailHospital) }
    }
}

class HemareApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
