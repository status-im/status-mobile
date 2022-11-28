(ns status-im2.contexts.quo-preview.qr.qr
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [status-im.utils.fx :as fx]
            [quo2.components.buttons.button :as quo2-button]
            [taoensso.timbre :as log]))

(def qr-final-url (reagent/atom "https://media.qrtiger.com/blog/2021/01/imgpsh_fullsize_anim_800.jpeg"))

(defn playground-view []
  [:<>
  [rn/view
   [quo2-button/button
    {:style {:margin-vertical 8}
     :on-press #(re-frame/dispatch [:get-qr-from-media-server])}
    "hey"
    ]
   ]
   [rn/view
       [rn/image {:source {:uri           @qr-final-url}
                  :style  {:width         256
                           :height        256
                           :margin-top    50
                           :border-radius 4
                           :margin-right  4}}
        ]
   ]

   ]
)

(fx/defn get-qr-from-media-server
  {:events [:get-qr-from-media-server]}
  [{:keys [db]}]
  (let [port (get db :mediaserver/port)
        url "cs2:5vd6SL:KFC:26gAouU6D6A4dCs9LK7jHmXZ3gjVdPczvX7yeusZRHTeR:3HxJ9Qr4H351dPoXjQYsdPX4tK6tV6TkdsHk1xMZEZmL:3"
        all-accounts (get db :multiaccounts/multiaccounts)
        all-key-uids (keys all-accounts)
        all-vals (vals all-accounts)
;        second-account (nth all-vals 1)
        first-account (nth all-vals 0)
        keyuid (get first-account :key-uid)
        qr-hardcoded-url (str "https://localhost:" port "/QRImagesWithLogo?qrurl=" (js/btoa url) "&keyUid=" keyuid "&imageName=thumbnail")
        ]
    (log/info "all :multiaccounts/multiaccounts account =====>" all-accounts)
    (log/info "all-vals =====>" all-vals)
;    (log/info "second multiaccount/accounts account =====>" second-account)
    (log/info "first multiaccount/accounts account =====>" first-account)
    (reset! qr-final-url qr-hardcoded-url)
    (log/info "qr-url from media server " qr-hardcoded-url)
    )
)




