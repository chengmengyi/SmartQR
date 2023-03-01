package com.demo.smartqr.conf

object LocalConf {
    const val email=""
    const val url=""

    const val OPEN="smartqr_open"
    const val HOME="smartqr_n_home"
    const val SCAN_RESULT="smartqr_n_scan"
    const val CREATE_RESULT="smartqr_n_creat"
    const val CLICK_FUNC="smartqr_i_2fun"

    const val localAd="""{
    "click_limit":15,
    "show_num":50,
    "smartqr_open": [
        {
            "smartqr_from": "admob",
            "smartqr_id": "ca-app-pub-3940256099942544/3419835294",
            "smartqr_source": "open",
            "smartqr_prio": 2
        },
        {
            "smartqr_from": "admob",
            "smartqr_id": "ca-app-pub-3940256099942544/3419835294A",
            "smartqr_source": "open",
            "smartqr_prio": 3
        },
          {
            "smartqr_from": "admob",
            "smartqr_id": "ca-app-pub-3940256099942544/3419835294A",
            "smartqr_source": "open",
            "smartqr_prio": 1
        }
    ],
    "smartqr_n_home": [
        {
            "smartqr_from": "admob",
            "smartqr_id": "ca-app-pub-3940256099942544/2247696110",
            "smartqr_source": "native",
            "smartqr_prio": 2
        },
        {
            "smartqr_from": "admob",
            "smartqr_id": "ca-app-pub-3940256099942544/2247696110A",
            "smartqr_source": "native",
            "smartqr_prio": 3
        }
    ],
   "smartqr_n_scan": [
        {
            "smartqr_from": "admob",
            "smartqr_id": "ca-app-pub-3940256099942544/2247696110",
            "smartqr_source": "native",
            "smartqr_prio": 2
        },
        {
            "smartqr_from": "admob",
            "smartqr_id": "ca-app-pub-3940256099942544/2247696110A",
            "smartqr_source": "native",
            "smartqr_prio": 3
        }
    ],
      "smartqr_n_creat": [
        {
            "smartqr_from": "admob",
            "smartqr_id": "ca-app-pub-3940256099942544/2247696110",
            "smartqr_source": "native",
            "smartqr_prio": 2
        },
        {
            "smartqr_from": "admob",
            "smartqr_id": "ca-app-pub-3940256099942544/2247696110A",
            "smartqr_source": "native",
            "smartqr_prio": 3
        }
    ],
     "smartqr_i_2fun": [
        {
            "smartqr_from": "admob",
            "smartqr_id": "ca-app-pub-3940256099942544/8691691433",
            "smartqr_source": "inter",
            "smartqr_prio": 2
        }
    ]
}"""
}