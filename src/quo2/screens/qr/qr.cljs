(ns quo2.screens.qr.qr
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [status-im.utils.fx :as fx]
            [quo2.components.buttons.button :as quo2-button]
            [taoensso.timbre :as log]))

(def qr-final-url (atom "https://media.qrtiger.com/blog/2021/01/imgpsh_fullsize_anim_800.jpeg"))
(def qr-code-set (atom false))
(def user-account-thumbnail (atom ""))

(defn playground-view []
  [:<>
   (let  [qr-final-url  @(re-frame/subscribe [:qr-hardcoded-url])]
  [rn/view

   [quo2-button/button
    {:style {:margin-vertical 8}
     :on-press #(re-frame/dispatch [:get-qr-from-media-server])}
    "hey"
    ]
   ]

   (cond @qr-code-set
         [rn/view
             [rn/image {:source {:uri           qr-final-url}
                        :style  {:width         56
                                 :height        56
                                 :margin-top    50
                                 :border-radius 4
                                 :margin-right  4}}
              ]
         ]
   )
     )
   ]
)

;(re-frame/reg-fx :show-wallet-connect-app-management-sheet
;                 (fn [] ))

(fx/defn get-qr-from-media-server
  {:events [:get-qr-from-media-server]}
  [{:keys [db]}]
  (let [port (get db :mediaserver/port)
        url "cs2:5vd6SL:KFC:26gAouU6D6A4dCs9LK7jHmXZ3gjVdPczvX7yeusZRHTeR:3HxJ9Qr4H351dPoXjQYsdPX4tK6tV6TkdsHk1xMZEZmL:3"
        qr-hardcoded-url (str "https://localhost:" port "/QRImages?hash=someshit")
        profile-images  @(re-frame/subscribe [:multiaccount/images])

        ]
    (reset! qr-final-url qr-hardcoded-url)
    (reset! qr-code-set true)
    (log/debug "qr-url from media server " qr-hardcoded-url)
     {:db (assoc db :qr-hardcoded-url qr-hardcoded-url)}
    ))




