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
    "get QR from status-go"
    ]
   ]
   [rn/view {:style {:flex-direction :row
                     :justify-content :center}}
       [rn/image {:source {:uri           @qr-final-url}
                  :style  {:width         303
                           :height        303
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
           url "https://github.com/yeqown/go-qrcode/"
           multiaccount (get db :multiaccount)
           keyuid (get multiaccount :key-uid)
           qr-hardcoded-url (str "https://localhost:" port "/QRImagesWithLogo?qrurl=" (js/btoa url) "&keyUid=" keyuid "&imageName=thumbnail")
           ]

       (reset! qr-final-url qr-hardcoded-url)
       (log/info "qr-url from media server " qr-hardcoded-url)
       )

)




