(ns status-im2.contexts.quo-preview.qr.qr
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [quo2.components.buttons.button :as quo2-button]
            [taoensso.timbre :as log]))

(defn playground-view []
  (let [port @(re-frame/subscribe [:mediaserver/port])
        url "https://github.com/yeqown/go-qrcode/"
        multiaccount @(re-frame/subscribe [:multiaccount])
        keyuid (get multiaccount :key-uid)
        media-server-url (str "https://localhost:"
                              port
                              "/GenerateQRCode?qrurl="
                              (js/btoa url)
                              "&keyUid="
                              keyuid
                              "&imageName=thumbnail")
        ]
  [:<>
  [rn/view {:style {:padding 20}}
   [rn/text (str "Displaying QR code for " url)]
   ]
   [rn/view {:style {:flex-direction :row
                     :justify-content :center}}
       [rn/image {:source {:uri           media-server-url}
                  :style  {:width         303
                           :height        303
                           :margin-top    30
                           :border-radius 4
                           :margin-right  4}}
        ]

   ]
   [rn/view {:style {:padding 20}}
    [rn/text (str "Fetched from media server url of " media-server-url)]
    ]
   ])
)





