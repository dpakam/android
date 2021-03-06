package mega.privacy.android.app.psa

import androidx.lifecycle.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import mega.privacy.android.app.MegaApplication
import mega.privacy.android.app.listeners.BaseListener
import mega.privacy.android.app.utils.RxUtil
import nz.mega.sdk.MegaApiJava
import nz.mega.sdk.MegaError
import nz.mega.sdk.MegaRequest
import java.util.concurrent.TimeUnit

/**
 * The ViewModel for PSA logic.
 */
object PsaManager : LifecycleObserver {

    /**
     * The minimum interval in milliseconds that we should keep between two calls to
     * SDK to get PSA from server.
     */
    private const val GET_PSA_INTERVAL_MS = 3600_000L

    private val application = MegaApplication.getInstance()
    private val megaApi = application.megaApi

    private var getPsaDisposable: Disposable? = null
    private var processLifecycleObserved = false
    private var startCheckingOnForeground = false

    /**
     * LiveData for PSA, mutable, used to emit value.
     */
    private val mutablePsa = MutableLiveData<Psa?>()

    /**
     * LiveData for PSA, not mutable, only used to observe value.
     */
    val psa: LiveData<Psa?> = mutablePsa

    /**
     * Start checking PSA periodically.
     */
    fun startChecking() {
        doStartChecking()
        startCheckingOnForeground = true

        if (!processLifecycleObserved) {
            processLifecycleObserved = true
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    /**
     * Stop checking PSA periodically.
     */
    fun stopChecking() {
        startCheckingOnForeground = false
        doStopChecking()
    }

    private fun doStartChecking() {
        if (getPsaDisposable != null) {
            return
        }

        getPsaDisposable = Observable.interval(0L, GET_PSA_INTERVAL_MS, TimeUnit.MILLISECONDS)
            .subscribe({
                megaApi.getPSAWithUrl(object : BaseListener(application) {
                    override fun onRequestFinish(
                        api: MegaApiJava,
                        request: MegaRequest,
                        e: MegaError
                    ) {
                        super.onRequestFinish(api, request, e)

                        if (e.errorCode == MegaError.API_OK) {
                            mutablePsa.value = Psa(
                                request.number.toInt(), request.name, request.text, request.file,
                                request.password, request.link, request.email
                            )
                        }
                    }
                })
            }, RxUtil.logErr("PsaManager getPSA"))
    }

    private fun doStopChecking() {
        getPsaDisposable?.dispose()
        getPsaDisposable = null
    }

    /**
     * Display the pending PSA (if exists) immediately.
     *
     * Activity will display PSA if it's resumed, but when switching activity, there will be a
     * time window that no activity is resumed, and if we get PSA result from API server in this
     * window, this PSA won't be displayed. So we need check if there is a PSA when activity is
     * resumed, if so, we'll display it immediately.
     *
     * And since activity's lifecycle state is updated after onResume return, we need post this
     * value.
     */
    fun displayPendingPsa() {
        val value = mutablePsa.value
        if (value != null) {
            mutablePsa.postValue(value)
        }
    }

    /**
     * Dismiss the PSA.
     *
     * @param id the id of the PSA
     */
    fun dismissPsa(id: Int) {
        megaApi.setPSA(id)
        mutablePsa.value = null
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        if (startCheckingOnForeground) {
            doStartChecking()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        doStopChecking()
    }
}
